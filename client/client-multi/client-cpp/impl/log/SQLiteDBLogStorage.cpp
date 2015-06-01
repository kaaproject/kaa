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

#include "kaa/log/SQLiteDBLogStorage.hpp"


#include "kaa/logging/Log.hpp"
#include "kaa/log/LogRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"

#define KAA_LOG_TABLE_NAME "KAA_LOGS"

#define KAA_RECORD_ID_FIELD_NAME   "RECORD_ID"
#define KAA_BUCKET_ID_FIELD_NAME   "BUCKET_ID"
#define KAA_LOG_DATA_FIELD_NAME    "LOG_DATA"

#define KAA_BUCKET_ID_INDEX_NAME "BUCKET_ID_INDEX"

#define KAA_CREATE_LOG_TABLE \
         "CREATE TABLE IF NOT EXISTS " KAA_LOG_TABLE_NAME " ("  \
         KAA_RECORD_ID_FIELD_NAME"    INT    PRIMARY KEY," \
         KAA_BUCKET_ID_FIELD_NAME"    INT," \
         KAA_LOG_DATA_FIELD_NAME"     BLOB);"

#define KAA_CREATE_BUCKET_ID_INDEX \
    "CREATE INDEX IF NOT EXISTS " KAA_BUCKET_ID_INDEX_NAME " ON " KAA_LOG_TABLE_NAME " (" KAA_BUCKET_ID_FIELD_NAME ");"

#define KAA_INSERT_NEW_RECORD \
    "INSERT INTO " KAA_LOG_TABLE_NAME " " \
    "(" KAA_LOG_DATA_FIELD_NAME ") VALUES (?); "

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
    "SELECT COUNT(*) FROM " KAA_LOG_TABLE_NAME ";"

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
            KAA_LOG_ERROR("Failed to create sqlite3 statement: bad data");
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

SQLiteDBLogStorage::SQLiteDBLogStorage(const std::string& dbName)
    : dbName_(dbName), db_(nullptr), unmarkedRecordCount_(0), totalRecordCount_(0), consumedMemory_(0)
{
    openDBConnection();
    initLogTable();

    SQLiteStatement stmt(db_, KAA_HOW_MANY_LOGS_IN_DB);
    int errorCode = sqlite3_step(stmt.getStatement());
    if (errorCode == SQLITE_ROW) {
        totalRecordCount_ = unmarkedRecordCount_ = sqlite3_column_int64(stmt.getStatement(), 0);
        KAA_LOG_INFO(boost::format("%d log records in database") % totalRecordCount_);
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

        KAA_LOG_INFO("'" KAA_LOG_TABLE_NAME "' table created");

        SQLiteStatement createIndexStmt(db_, KAA_CREATE_BUCKET_ID_INDEX);
        errorCode = sqlite3_step(createIndexStmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE,
                (boost::format("Failed to create '" KAA_BUCKET_ID_INDEX_NAME "' index (error %d)") % errorCode).str());

        KAA_LOG_INFO("'" KAA_BUCKET_ID_INDEX_NAME "' index created");
    } catch (std::exception& e) {
        KAA_LOG_FATAL(boost::format("Failed to init log table: %s") % e.what());
        throw;
    }
}

void SQLiteDBLogStorage::resetBucketID()
{
    try {
        SQLiteStatement stmt(db_, KAA_RESET_BUCKET_ID_ON_START);
        int errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("(error %d)") % errorCode).str());

        KAA_LOG_TRACE("Bucket id reseted");
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


    int updatedRecordCount = sqlite3_changes(db_);
    unmarkedRecordCount_ -= updatedRecordCount;

    KAA_LOG_TRACE(boost::format("Update bucket id to %d for records with %s id-s") % id % strIdList);
}

void SQLiteDBLogStorage::openDBConnection()
{
    KAA_LOG_FTRACE(boost::format("Going to connect to '%s' log database") % dbName_);

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

    KAA_MUTEX_LOCKING("storageGuard");
    KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, storageGuard);
    KAA_MUTEX_LOCKED("storageGuard");

    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_INSERT_NEW_RECORD);

        errorCode = sqlite3_bind_blob(stmt.getStatement(), 1, record->getLogEntry().data.data(), record->getSize(), SQLITE_STATIC);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind record data (error %d)") % errorCode).str());

        errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute 'INSERT' query (error %d)") % errorCode).str());

        ++unmarkedRecordCount_;
        ++totalRecordCount_;

        KAA_LOG_TRACE(boost::format("Added log record (%u bytes). Total: %u, unmarked: %u")
                            % record->getSize() % unmarkedRecordCount_ % totalRecordCount_);
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to add log record: %s") % e.what());
    }
}

ILogStorage::RecordPack SQLiteDBLogStorage::getRecordBlock(std::size_t bucketSize)
{
    static std::int32_t id = 0;
    ILogStorage::RecordPack pack;

    KAA_LOG_TRACE(boost::format("Requested logs, bucket size %u") % bucketSize);

    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_SELECT_UNMARKED_RECORDS);

        std::list<int> unmarkedRecordIds;

        std::size_t leftBucketSize = bucketSize;
        while (SQLITE_ROW == (errorCode = sqlite3_step(stmt.getStatement()))) {
            int recordNumber = sqlite3_column_int(stmt.getStatement(), 0);
            const void *bytes = sqlite3_column_blob(stmt.getStatement(), 1);
            int bytesSize = sqlite3_column_bytes(stmt.getStatement(), 1);

            if (bytes && bytesSize > 0) {
                if (leftBucketSize < (std::size_t)bytesSize) {
                    errorCode = SQLITE_DONE;
                    break;
                }

                pack.second.push_back(LogRecordPtr(new LogRecord((const std::uint8_t *)bytes, bytesSize)));
                unmarkedRecordIds.push_back(recordNumber);
                leftBucketSize -= bytesSize;

                KAA_LOG_TRACE(boost::format("Find unmarked record (id %d, size %d, left bucket size %u)")
                                                                    % recordNumber % bytesSize % leftBucketSize);
            } else {
                KAA_LOG_WARN("Find unmarked record with null data");
                //TODO: remove
            }
        }

        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute 'SELECT' query (error %d)") % errorCode).str());

        if (!pack.second.empty()) {
            pack.first = id++;
            updateBucketIDForRecords(pack.first, unmarkedRecordIds);
//
//            std::size_t realBucketSize = bucketSize - leftBucketSize;
//            unmarkedRecordCount_ -= pack.second.size();
//            consumedMemory_ -= realBucketSize;
//            consumedMemoryStorage_.insert(std::make_pair(pack.first, realBucketSize));
//
//            KAA_LOG_TRACE(boost::format("Create log bucket: id %d, size %u, %u logs. Total: %u, unmarked: %u")
//                            % pack.first % realBucketSize % pack.second.size() % unmarkedRecordCount_ % totalRecordCount_);
        }
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to get unmarked records from database (total: %u, unmarked: %u): %s")
                                                                % totalRecordCount_ % unmarkedRecordCount_ % e.what());
        pack.second.clear();
    }

    return pack;
}


void SQLiteDBLogStorage::removeRecordBlock(RecordBlockId id)
{
    KAA_MUTEX_LOCKING("storageGuard");
    KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, storageGuard);
    KAA_MUTEX_LOCKED("storageGuard");

    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_DELETE_BY_BUCKET_ID);

        errorCode = sqlite3_bind_int64(stmt.getStatement(), 1, id);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind bucket id (error %d)") % errorCode).str());

        errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute 'REMOVE' query (error %d)") % errorCode).str());

        int removedRecordsCount = sqlite3_changes(db_);
        unmarkedRecordCount_ -= removedRecordsCount;
        totalRecordCount_ -= removedRecordsCount;

        KAA_LOG_INFO(boost::format("Removed %d log records, bucket id %d. Total: %u, unmarked: %u")
                                                % removedRecordsCount % id % unmarkedRecordCount_ % totalRecordCount_);
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to remove log records by bucket id %d (total: %u, unmarked: %u): %s")
                                                        % id % totalRecordCount_ % unmarkedRecordCount_ % e.what());
    }
}

void SQLiteDBLogStorage::notifyUploadFailed(RecordBlockId id)
{
    KAA_MUTEX_LOCKING("storageGuard");
    KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, storageGuard);
    KAA_MUTEX_LOCKED("storageGuard");

    try {
        int errorCode = SQLITE_OK;
        SQLiteStatement stmt(db_, KAA_RESET_BY_BUCKET_ID);

        errorCode = sqlite3_bind_int64(stmt.getStatement(), 1, id);
        throwIfError(errorCode, SQLITE_OK, (boost::format("Failed to bind bucket id (error %d)") % errorCode).str());

        errorCode = sqlite3_step(stmt.getStatement());
        throwIfError(errorCode, SQLITE_DONE, (boost::format("Failed to execute 'UPDATE' query (error %d)") % errorCode).str());

        int unmarkedCount = sqlite3_changes(db_);
        unmarkedRecordCount_ += unmarkedCount;

        KAA_LOG_INFO(boost::format("Failed to upload logs, bucket id %d. %d log records unmarked. Total: %u, unmarked: %u")
                                                            % unmarkedCount % id % totalRecordCount_ % unmarkedRecordCount_);
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Failed to unmark log records by bucket id %d (total: %u, unmarked: %u): %s")
                                                        % id % totalRecordCount_ % unmarkedRecordCount_ % e.what());
    }
}

} /* namespace kaa */

#endif /* KAA_USE_SQLITE_LOG_STORAGE */
