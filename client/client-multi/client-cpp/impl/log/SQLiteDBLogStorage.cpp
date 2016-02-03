/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifdef KAA_USE_SQLITE_LOG_STORAGE

#include <kaa/log/SQLiteDBLogStorage.hpp>

#include <kaa/logging/Log.hpp>
#include <kaa/log/LogRecord.hpp>
#include <kaa/common/exception/KaaException.hpp>
#include <kaa/KaaClientProperties.hpp>

#define KAA_LOG_TABLE_NAME "KAA_LOGS"

#define KAA_RECORD_ID_FIELD_NAME   "RECORD_ID"
#define KAA_BUCKET_ID_FIELD_NAME   "BUCKET_ID"
#define KAA_LOG_DATA_FIELD_NAME    "LOG_DATA"

#define KAA_BUCKET_ID_INDEX_NAME "KAA_BUCKET_ID_INDEX"

#define KAA_CREATE_LOG_TABLE \
         "CREATE TABLE IF NOT EXISTS " KAA_LOG_TABLE_NAME " ("  \
         KAA_RECORD_ID_FIELD_NAME"    INTEGER    PRIMARY KEY    AUTOINCREMENT," \
         KAA_BUCKET_ID_FIELD_NAME"    INTEGER," \
         KAA_LOG_DATA_FIELD_NAME"     BLOB);"

#define KAA_CREATE_BUCKET_ID_INDEX \
    "CREATE INDEX IF NOT EXISTS " KAA_BUCKET_ID_INDEX_NAME " " \
    "ON " KAA_LOG_TABLE_NAME " (" KAA_BUCKET_ID_FIELD_NAME ");"

#define KAA_INSERT_NEW_RECORD \
    "INSERT INTO " KAA_LOG_TABLE_NAME " (" KAA_LOG_DATA_FIELD_NAME ")" \
    "VALUES (?);"

#define KAA_SELECT_UNMARKED_RECORDS \
    "SELECT " KAA_RECORD_ID_FIELD_NAME ", " KAA_LOG_DATA_FIELD_NAME " "\
    "FROM " KAA_LOG_TABLE_NAME " "\
    "WHERE " KAA_BUCKET_ID_FIELD_NAME " IS NULL " \
    "ORDER BY " KAA_RECORD_ID_FIELD_NAME " ASC;"

#define KAA_RESET_BUCKET_ID_ON_START \
    "UPDATE " KAA_LOG_TABLE_NAME " " \
    "SET " KAA_BUCKET_ID_FIELD_NAME " = NULL " \
    "WHERE " KAA_BUCKET_ID_FIELD_NAME " IS NOT NULL;"

#define KAA_RESET_BY_BUCKET_ID \
    "UPDATE " KAA_LOG_TABLE_NAME " " \
    "SET " KAA_BUCKET_ID_FIELD_NAME " = NULL " \
    "WHERE " KAA_BUCKET_ID_FIELD_NAME " = ?;"

#define KAA_DELETE_BY_BUCKET_ID \
    "DELETE FROM " KAA_LOG_TABLE_NAME " " \
    "WHERE " KAA_BUCKET_ID_FIELD_NAME " = ?;"

#define KAA_DELETE_BY_RECORD_ID \
    "DELETE FROM " KAA_LOG_TABLE_NAME " " \
    "WHERE " KAA_RECORD_ID_FIELD_NAME " = ?;"

#define KAA_UPDATE_BUCKET_ID \
    "UPDATE " KAA_LOG_TABLE_NAME " " \
    "SET " KAA_BUCKET_ID_FIELD_NAME " = ? " \
    "WHERE " KAA_RECORD_ID_FIELD_NAME " IN (?);"

#define KAA_HOW_MANY_LOGS_IN_DB \
    "SELECT COUNT(*), SUM(LENGTH(" KAA_LOG_DATA_FIELD_NAME ")) FROM " KAA_LOG_TABLE_NAME ";"

/*
 * OPTIMIZATION OPTIONS.
 */
#define KAA_SYNCHRONIZATION_OPTION        "PRAGMA synchronous=OFF"
#define KAA_COUNT_CHANGES_OPTION          "PRAGMA count_changes=OFF"
#define KAA_MEMORY_JOURNAL_MODE_OPTION    "PRAGMA journal_mode=MEMORY"
#define KAA_MEMORY_TEMP_STORE_OPTION      "PRAGMA temp_store=MEMORY"

namespace kaa {

static void throwIfError(int errorCode, int expectedErrorCode, const std::string& errorMessage)
{
    if (errorCode != expectedErrorCode) {
        throw KaaException(errorMessage);
    }
}

class SQLiteStatement {
public:
    SQLiteStatement(sqlite3 *db, const char* sql) : stmt_(nullptr)
    {
        if (!db || !sql) {
            throw KaaException("Failed to create sqlite3 statement: bad data");
        }

        const char *pzTail;
        int errorCode = sqlite3_prepare_v2(db, sql, -1, &stmt_, &pzTail);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to create sql statement (error %d)") % errorCode).str());
    }

    ~SQLiteStatement()
    {
        if (stmt_) {
            sqlite3_finalize(stmt_);
        }
    }

    sqlite3_stmt *getStatement() { return stmt_; }

private:
    sqlite3_stmt *stmt_;
};

SQLiteDBLogStorage::SQLiteDBLogStorage(IKaaClientContext &context, int optimizationMask)
  : dbName_(context.getProperties().getLogsDatabaseFileName()), db_(nullptr), unmarkedRecordCount_(0), totalRecordCount_(0), consumedMemory_(0), context_(context)
{
    openDBConnection();
    initLogTable();

    if ((SQLiteOptimizationOptions)optimizationMask != SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS) {
        applyOptimization(optimizationMask);
    } else {
        KAA_LOG_INFO("No database optimization is switched on");
    }

    SQLiteStatement stmt(db_, KAA_HOW_MANY_LOGS_IN_DB);
    int errorCode = sqlite3_step(stmt.getStatement());
    if (errorCode == SQLITE_ROW) {
        totalRecordCount_ = unmarkedRecordCount_ = sqlite3_column_int64(stmt.getStatement(), 0);
        consumedMemory_ = sqlite3_column_int64(stmt.getStatement(), 1);
        KAA_LOG_INFO(boost::format("%li log records in database (%li bytes total size)") % totalRecordCount_ % consumedMemory_);
    } else {
        KAA_LOG_ERROR("Failed to count log records in database");
    }

    if (totalRecordCount_ > 0) {
        resetBucketID();
    }
}

SQLiteDBLogStorage::~SQLiteDBLogStorage()
{
    closeDBConnection();
}

void SQLiteDBLogStorage::initLogTable()
{
    try {
        int errorCode = 0;

        SQLiteStatement createTableStmt(db_, KAA_CREATE_LOG_TABLE);
        errorCode = sqlite3_step(createTableStmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE,
                (boost::format("Failed to create '" KAA_LOG_TABLE_NAME "' table (error %d)") % errorCode).str());

        KAA_LOG_TRACE("'" KAA_LOG_TABLE_NAME "' table created");

        SQLiteStatement createIndexStmt(db_, KAA_CREATE_BUCKET_ID_INDEX);
        errorCode = sqlite3_step(createIndexStmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE,
                (boost::format("Failed to create '" KAA_BUCKET_ID_INDEX_NAME "' index (error %d)") % errorCode).str());

        KAA_LOG_TRACE("'" KAA_BUCKET_ID_INDEX_NAME "' index created");
    } catch (std::exception& e) {
        KAA_LOG_FATAL(boost::format("Failed to init log table: %s") % e.what());
        throw;
    }
}

void SQLiteDBLogStorage::applyOptimization(int mask)
{
    char* errorMessage;

    if (mask & SQLiteOptimizationOptions::SQLITE_SYNCHRONOUS_OFF) {
        sqlite3_exec(db_, KAA_SYNCHRONIZATION_OPTION, NULL, NULL, &errorMessage);
        KAA_LOG_INFO(boost::format("Applied '%s' optimization") % KAA_SYNCHRONIZATION_OPTION);
    }
    if (mask & SQLiteOptimizationOptions::SQLITE_MEMORY_JOURNAL_MODE) {
        sqlite3_exec(db_, KAA_MEMORY_JOURNAL_MODE_OPTION, NULL, NULL, &errorMessage);
        KAA_LOG_INFO(boost::format("Applied '%s' optimization") % KAA_MEMORY_JOURNAL_MODE_OPTION);
    }
    if (mask & SQLiteOptimizationOptions::SQLITE_MEMORY_TEMP_STORE) {
        sqlite3_exec(db_, KAA_MEMORY_TEMP_STORE_OPTION, NULL, NULL, &errorMessage);
        KAA_LOG_INFO(boost::format("Applied '%s' optimization") % KAA_MEMORY_TEMP_STORE_OPTION);
    }
    if (mask & SQLiteOptimizationOptions::SQLITE_COUNT_CHANGES_OFF) {
        sqlite3_exec(db_, KAA_COUNT_CHANGES_OPTION, NULL, NULL, &errorMessage);
        KAA_LOG_INFO(boost::format("Applied '%s' optimization") % KAA_COUNT_CHANGES_OPTION);
    }
}

void SQLiteDBLogStorage::resetBucketID()
{
    try {
        SQLiteStatement stmt(db_, KAA_RESET_BUCKET_ID_ON_START);
        int errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("(error %d)") % errorCode).str());

        KAA_LOG_INFO(boost::format("Bucket id reseted for %d record(s)") % sqlite3_changes(db_));
    } catch (std::exception& e) {
        KAA_LOG_FATAL(boost::format("Failed to reset bucket id: %s") % e.what());
        throw;
    }
}

void SQLiteDBLogStorage::updateBucketIDForRecords(std::int32_t id, std::list<int>& idList)
{
    if (idList.empty()) {
        return;
    }

    std::ostringstream oss;
    for (auto it = idList.cbegin(); it != idList.end();) {
        oss << *it;
        if (++it != idList.end()) {
            oss << ",";
        }
    }

    std::string strIdList = oss.str();
    std::string sql(KAA_UPDATE_BUCKET_ID);

    int errorCode = SQLITE_OK;
    SQLiteStatement stmt(db_, (sql.replace(sql.rfind('?'), 1, strIdList)).c_str());

    errorCode = sqlite3_bind_int64(stmt.getStatement(), 1, id);
    throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind bucket id (error %d)") % errorCode).str());

    errorCode = sqlite3_step(stmt.getStatement());
    throwIfError(errorCode, SQLITE_DONE, (boost::format("(error %d)") % errorCode).str());

    KAA_LOG_TRACE(boost::format("Update bucket id to %d for %d records with %s id-s") % id % sqlite3_changes(db_) % strIdList);
}

void SQLiteDBLogStorage::removeRecordById(sqlite3_int64 id)
{
    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_DELETE_BY_RECORD_ID);

        errorCode = sqlite3_bind_int64(stmt.getStatement(), 1, id);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind record id (error %d)") % errorCode).str());

        errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute sql delete query (error %d)") % errorCode).str());

        --totalRecordCount_;

        KAA_LOG_INFO(boost::format("Remove record, id %li. Total: %u, unmarked: %u")
                                        % id % unmarkedRecordCount_ % totalRecordCount_);
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to remove log record ,id %lu: %s") % id % e.what());
    }
}

void SQLiteDBLogStorage::openDBConnection()
{
    KAA_LOG_TRACE(boost::format("Going to connect to '%s' log database") % dbName_);

    int errorCode = sqlite3_open(dbName_.c_str(), &db_);
    throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to connect to '%s' log database (error %d)") % dbName_ % errorCode).str());
    KAA_LOG_INFO(boost::format("Connected to '%s' log database") % dbName_);
}

void SQLiteDBLogStorage::closeDBConnection()
{
    if (db_) {
        sqlite3_close(db_);
        db_ = nullptr;
    }
}

void SQLiteDBLogStorage::addLogRecord(LogRecordPtr record)
{
    if (!record || (record && !record->getSize())) {
        KAA_LOG_WARN("Failed to add log record: null data");
        return;
    }

    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_INSERT_NEW_RECORD);

        errorCode = sqlite3_bind_blob(stmt.getStatement(), 1, record->getLogEntry().data.data(), record->getSize(), SQLITE_STATIC);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind record data (error %d)") % errorCode).str());

        KAA_MUTEX_LOCKING("sqliteLogStorageGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, sqliteLogStorageGuard_);
        KAA_MUTEX_LOCKED("sqliteLogStorageGuard_");

        errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute sql insert query (error %d)") % errorCode).str());

        ++unmarkedRecordCount_;
        ++totalRecordCount_;
        consumedMemory_ += record->getSize();

        KAA_LOG_TRACE(boost::format("Added log record (%u bytes). Total: %u, unmarked: %u")
                            % record->getSize() % totalRecordCount_ % unmarkedRecordCount_);
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to add log record: %s") % e.what());
    }
}

ILogStorage::RecordPack SQLiteDBLogStorage::getRecordBlock(std::size_t bucketSize, std::size_t recordsBlockCount)
{
    static std::int32_t id = 0;
    ILogStorage::RecordPack pack;

    KAA_LOG_TRACE(boost::format("Creating new log bucket, size %u") % bucketSize);

    try {
        int errorCode = SQLITE_OK;
        std::list<int> unmarkedRecordIds;
        SQLiteStatement stmt(db_, KAA_SELECT_UNMARKED_RECORDS);

        KAA_MUTEX_LOCKING("sqliteLogStorageGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, sqliteLogStorageGuard_);
        KAA_MUTEX_LOCKED("sqliteLogStorageGuard_");

        std::size_t leftBucketSize = bucketSize;
        std::size_t leftRecordsCount = recordsBlockCount;

        while (SQLITE_ROW == (errorCode = sqlite3_step(stmt.getStatement()))) {
            auto recordId = sqlite3_column_int64(stmt.getStatement(), 0);
            const void *recordData = sqlite3_column_blob(stmt.getStatement(), 1);
            int recordDataSize = sqlite3_column_bytes(stmt.getStatement(), 1);

            if (recordData && recordDataSize > 0) {
                if (leftRecordsCount == 0 || leftBucketSize < (std::size_t)recordDataSize) {
                    errorCode = SQLITE_DONE;
                    break;
                }

                pack.second.push_back(LogRecordPtr(new LogRecord((const std::uint8_t *)recordData, recordDataSize)));
                unmarkedRecordIds.push_back(recordId);
                leftBucketSize -= recordDataSize;
                leftRecordsCount--;

                KAA_LOG_TRACE(boost::format("Find unmarked record (id %d, size %d, left bucket size %u)")
                                                                    % recordId % recordDataSize % leftBucketSize);
            } else {
                KAA_LOG_WARN("Find unmarked record with null data. Deleting it...");
                removeRecordById(recordId);
                --unmarkedRecordCount_;
            }
        }

        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute sql select query (error %d)") % errorCode).str());

        if (!pack.second.empty()) {
            pack.first = id++;
            updateBucketIDForRecords(pack.first, unmarkedRecordIds);

            std::size_t realBucketSize = bucketSize - leftBucketSize;

            unmarkedRecordCount_ -= pack.second.size();
            consumedMemory_ -= realBucketSize;
            consumedMemoryStorage_.insert(std::make_pair(pack.first, realBucketSize));

            KAA_LOG_INFO(boost::format("Create log bucket: id %d, size %u, %u record(s). Total: %u, unmarked: %u")
                            % pack.first % realBucketSize % pack.second.size() % totalRecordCount_ % unmarkedRecordCount_);
        } else {
            KAA_LOG_INFO("No unmarked logs found");
        }
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to create log bucket (total: %u, unmarked: %u): %s")
                                            % totalRecordCount_ % unmarkedRecordCount_ % e.what());
        pack.second.clear();
    }

    return pack;
}


void SQLiteDBLogStorage::removeRecordBlock(RecordBlockId id)
{
    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_DELETE_BY_BUCKET_ID);

        errorCode = sqlite3_bind_int64(stmt.getStatement(), 1, id);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind bucket id (error %d)") % errorCode).str());

        KAA_MUTEX_LOCKING("sqliteLogStorageGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, sqliteLogStorageGuard_);
        KAA_MUTEX_LOCKED("sqliteLogStorageGuard_");

        errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute sql delete query (error %d)") % errorCode).str());

        int removedRecordsCount = sqlite3_changes(db_);
        totalRecordCount_ -= removedRecordsCount;
        consumedMemoryStorage_.erase(id);

        KAA_LOG_INFO(boost::format("Removed %d log records, bucket id %d. Total: %u, unmarked: %u")
                                                % removedRecordsCount % id % totalRecordCount_ % unmarkedRecordCount_);
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to remove log records by bucket id %d (total: %u, unmarked: %u): %s")
                                                        % id % totalRecordCount_ % unmarkedRecordCount_ % e.what());
    }
}

void SQLiteDBLogStorage::notifyUploadFailed(RecordBlockId id)
{
    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_RESET_BY_BUCKET_ID);

        errorCode = sqlite3_bind_int64(stmt.getStatement(), 1, id);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind bucket id (error %d)") % errorCode).str());

        KAA_MUTEX_LOCKING("sqliteLogStorageGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, sqliteLogStorageGuard_);
        KAA_MUTEX_LOCKED("sqliteLogStorageGuard_");

        errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute sql update query (error %d)") % errorCode).str());

        int unmarkedCount = sqlite3_changes(db_);
        unmarkedRecordCount_ += unmarkedCount;

        auto it = consumedMemoryStorage_.find(id);
        if (it != consumedMemoryStorage_.end()) {
            consumedMemory_ += it->second;
            consumedMemoryStorage_.erase(it);
        }

        KAA_LOG_INFO(boost::format("%d record(s) will resend later: failed to upload logs, bucket id %d. Total: %u, unmarked: %u")
                                                                    % id % unmarkedCount % totalRecordCount_ % unmarkedRecordCount_);
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to unmark log records by bucket id %d (total: %u, unmarked: %u): %s")
                                                        % id % totalRecordCount_ % unmarkedRecordCount_ % e.what());
    }
}


std::size_t SQLiteDBLogStorage::getRecordsCount()
{
    KAA_MUTEX_LOCKING("sqliteLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, sqliteLogStorageGuard_);
    KAA_MUTEX_LOCKED("sqliteLogStorageGuard_");
    return unmarkedRecordCount_;
}

std::size_t SQLiteDBLogStorage::getConsumedVolume()
{
    KAA_MUTEX_LOCKING("sqliteLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, sqliteLogStorageGuard_);
    KAA_MUTEX_LOCKED("sqliteLogStorageGuard_");
    return consumedMemory_;
}

} /* namespace kaa */

#endif /* KAA_USE_SQLITE_LOG_STORAGE */
