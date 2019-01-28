/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#ifndef KAA_CONFIGURATION_GEN_CONFIGURATIONGEN_HPP_2380542215__H_
#define KAA_CONFIGURATION_GEN_CONFIGURATIONGEN_HPP_2380542215__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa_configuration {
struct _configuration_avsc_Union__0__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__0__();
};

struct opc_metadata_path {
    typedef _configuration_avsc_Union__0__ __uuid_t;
    int32_t namespace_id;
    std::string full_tag_path;
    __uuid_t __uuid;
    opc_metadata_path() :
        namespace_id(int32_t()),
        full_tag_path(std::string()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__1__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__1__();
};

struct sql_metadata_query {
    typedef _configuration_avsc_Union__1__ __uuid_t;
    std::string query;
    __uuid_t __uuid;
    sql_metadata_query() :
        query(std::string()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__2__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__2__();
};

struct value {
    typedef _configuration_avsc_Union__2__ __uuid_t;
    std::string configuration;
    __uuid_t __uuid;
    value() :
        configuration(std::string()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__3__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    opc_metadata_path get_opc_metadata_path() const;
    void set_opc_metadata_path(const opc_metadata_path& v);
    sql_metadata_query get_sql_metadata_query() const;
    void set_sql_metadata_query(const sql_metadata_query& v);
    value get_value() const;
    void set_value(const value& v);
    _configuration_avsc_Union__3__();
};

struct _configuration_avsc_Union__4__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__4__();
};

struct MetaInfoResolving {
    typedef _configuration_avsc_Union__3__ strategy_t;
    typedef _configuration_avsc_Union__4__ __uuid_t;
    strategy_t strategy;
    __uuid_t __uuid;
    MetaInfoResolving() :
        strategy(strategy_t()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__5__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__5__();
};

struct CommonMetadata {
    typedef _configuration_avsc_Union__5__ __uuid_t;
    int32_t agent_state_upload_period_seconds;
    MetaInfoResolving device_model;
    MetaInfoResolving device_serial_number;
    MetaInfoResolving device_software_version;
    __uuid_t __uuid;
    CommonMetadata() :
        agent_state_upload_period_seconds(int32_t()),
        device_model(MetaInfoResolving()),
        device_serial_number(MetaInfoResolving()),
        device_software_version(MetaInfoResolving()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__6__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__6__();
};

struct _configuration_avsc_Union__7__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    double get_double() const;
    void set_double(const double& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__7__();
};

struct _configuration_avsc_Union__8__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__8__();
};

struct OpcUaTag {
    typedef _configuration_avsc_Union__6__ output_tag_t;
    typedef _configuration_avsc_Union__7__ scale_t;
    typedef _configuration_avsc_Union__8__ __uuid_t;
    int32_t namespace_id;
    std::string path;
    output_tag_t output_tag;
    scale_t scale;
    int64_t sampling_interval_ms;
    bool send_only_on_change;
    __uuid_t __uuid;
    OpcUaTag() :
        namespace_id(int32_t()),
        path(std::string()),
        output_tag(output_tag_t()),
        scale(scale_t()),
        sampling_interval_ms(int64_t()),
        send_only_on_change(bool()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__9__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    int32_t get_int() const;
    void set_int(const int32_t& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__9__();
};

struct _configuration_avsc_Union__10__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::string get_string() const;
    void set_string(const std::string& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__10__();
};

struct _configuration_avsc_Union__11__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__11__();
};

struct OpcUaConfiguration {
    typedef _configuration_avsc_Union__9__ browse_namespace_t;
    typedef _configuration_avsc_Union__10__ browse_id_t;
    typedef _configuration_avsc_Union__11__ __uuid_t;
    std::vector<OpcUaTag > tags;
    std::string host;
    int32_t port;
    bool is_connection_secure;
    int32_t reconnect_time_period_seconds;
    std::string config_id;
    browse_namespace_t browse_namespace;
    browse_id_t browse_id;
    __uuid_t __uuid;
    OpcUaConfiguration() :
        tags(std::vector<OpcUaTag >()),
        host(std::string()),
        port(int32_t()),
        is_connection_secure(bool()),
        reconnect_time_period_seconds(int32_t()),
        config_id(std::string()),
        browse_namespace(browse_namespace_t()),
        browse_id(browse_id_t()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__12__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::vector<OpcUaConfiguration > get_array() const;
    void set_array(const std::vector<OpcUaConfiguration >& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__12__();
};

enum CanDataType {
    BOOLEAN,
    BYTE,
    WORD,
    DWORD,
    REAL,
};

enum CanDataByteOrder {
    MSB,
    LSB,
};

struct _configuration_avsc_Union__13__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    int32_t get_int() const;
    void set_int(const int32_t& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__13__();
};

enum CanValueScalingOrder {
    MULTIPLIER,
    OFFSET,
};

struct _configuration_avsc_Union__14__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    float get_float() const;
    void set_float(const float& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__14__();
};

struct _configuration_avsc_Union__15__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    int64_t get_long() const;
    void set_long(const int64_t& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__15__();
};

struct _configuration_avsc_Union__16__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__16__();
};

struct CanValueScaling {
    typedef _configuration_avsc_Union__14__ multiplier_t;
    typedef _configuration_avsc_Union__15__ offset_t;
    typedef _configuration_avsc_Union__16__ __uuid_t;
    CanValueScalingOrder scalingOrder;
    multiplier_t multiplier;
    offset_t offset;
    __uuid_t __uuid;
    CanValueScaling() :
        scalingOrder(CanValueScalingOrder()),
        multiplier(multiplier_t()),
        offset(offset_t()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__17__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    CanValueScaling get_CanValueScaling() const;
    void set_CanValueScaling(const CanValueScaling& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__17__();
};

struct _configuration_avsc_Union__18__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__18__();
};

struct TimerCounterConfig {
    typedef _configuration_avsc_Union__18__ __uuid_t;
    int32_t channel_number;
    int64_t data_velocity_ms;
    __uuid_t __uuid;
    TimerCounterConfig() :
        channel_number(int32_t()),
        data_velocity_ms(int64_t()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__19__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    TimerCounterConfig get_TimerCounterConfig() const;
    void set_TimerCounterConfig(const TimerCounterConfig& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__19__();
};

struct _configuration_avsc_Union__20__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__20__();
};

struct CanVariableConfiguration {
    typedef _configuration_avsc_Union__13__ bitOffset_t;
    typedef _configuration_avsc_Union__17__ scaling_t;
    typedef _configuration_avsc_Union__19__ timer_counter_config_t;
    typedef _configuration_avsc_Union__20__ __uuid_t;
    std::string name;
    CanDataType type;
    CanDataByteOrder byteOrder;
    int32_t byteOffset;
    bitOffset_t bitOffset;
    scaling_t scaling;
    int64_t data_velocity_ms;
    bool send_only_on_change;
    timer_counter_config_t timer_counter_config;
    __uuid_t __uuid;
    CanVariableConfiguration() :
        name(std::string()),
        type(CanDataType()),
        byteOrder(CanDataByteOrder()),
        byteOffset(int32_t()),
        bitOffset(bitOffset_t()),
        scaling(scaling_t()),
        data_velocity_ms(int64_t()),
        send_only_on_change(bool()),
        timer_counter_config(timer_counter_config_t()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__21__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__21__();
};

struct CanMessageConfiguration {
    typedef _configuration_avsc_Union__21__ __uuid_t;
    std::string interface_name;
    int32_t id;
    std::vector<CanVariableConfiguration > variables;
    __uuid_t __uuid;
    CanMessageConfiguration() :
        interface_name(std::string()),
        id(int32_t()),
        variables(std::vector<CanVariableConfiguration >()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__22__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__22__();
};

struct CanConfiguration {
    typedef _configuration_avsc_Union__22__ __uuid_t;
    std::vector<CanMessageConfiguration > messages;
    __uuid_t __uuid;
    CanConfiguration() :
        messages(std::vector<CanMessageConfiguration >()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__23__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__23__();
};

struct ModeConfiguration {
    typedef _configuration_avsc_Union__23__ __uuid_t;
    CanConfiguration can_configuration;
    std::string accelerometer_threshold;
    int32_t accelerometer_motion_detection_query_period_seconds;
    int32_t gps_threshold_meters_per_second;
    int32_t gps_motion_detection_query_period_seconds;
    int32_t gps_location_query_period_seconds;
    int32_t mode;
    __uuid_t __uuid;
    ModeConfiguration() :
        can_configuration(CanConfiguration()),
        accelerometer_threshold(std::string()),
        accelerometer_motion_detection_query_period_seconds(int32_t()),
        gps_threshold_meters_per_second(int32_t()),
        gps_motion_detection_query_period_seconds(int32_t()),
        gps_location_query_period_seconds(int32_t()),
        mode(int32_t()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__24__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__24__();
};

struct ZeusConfiguration {
    typedef _configuration_avsc_Union__24__ __uuid_t;
    std::vector<ModeConfiguration > mode_configuration;
    int32_t storage_size_mbytes;
    __uuid_t __uuid;
    ZeusConfiguration() :
        mode_configuration(std::vector<ModeConfiguration >()),
        storage_size_mbytes(int32_t()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__25__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    ZeusConfiguration get_ZeusConfiguration() const;
    void set_ZeusConfiguration(const ZeusConfiguration& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__25__();
};

enum SqlDatabaseType {
    NOT_SPECIFIED,
    ODBC,
    Oracle,
    MS_SQL,
    InterBase,
    SQLBase,
    DB2,
    Informix,
    Sybase,
    MySQL,
    PostgreSQL,
    SQLite,
    SQLAnywhere,
};

struct _configuration_avsc_Union__26__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__26__();
};

struct sql_query {
    typedef _configuration_avsc_Union__26__ __uuid_t;
    std::string iteration_sql_query;
    std::string start_value;
    bool update_start_value;
    int64_t poll_period_seconds;
    std::string iteration_column_name;
    std::string timestamp_column_name;
    std::string time_zone;
    std::string query_id;
    __uuid_t __uuid;
    sql_query() :
        iteration_sql_query(std::string()),
        start_value(std::string()),
        update_start_value(bool()),
        poll_period_seconds(int64_t()),
        iteration_column_name(std::string()),
        timestamp_column_name(std::string()),
        time_zone(std::string()),
        query_id(std::string()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__27__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__27__();
};

struct SqlConfiguration {
    typedef _configuration_avsc_Union__27__ __uuid_t;
    SqlDatabaseType database_type;
    std::string database_connection_string;
    std::string user_name;
    std::string user_password;
    std::vector<sql_query > sql_queries;
    std::string config_id;
    bool dry_run;
    __uuid_t __uuid;
    SqlConfiguration() :
        database_type(SqlDatabaseType()),
        database_connection_string(std::string()),
        user_name(std::string()),
        user_password(std::string()),
        sql_queries(std::vector<sql_query >()),
        config_id(std::string()),
        dry_run(bool()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__28__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    std::vector<SqlConfiguration > get_array() const;
    void set_array(const std::vector<SqlConfiguration >& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__28__();
};

struct _configuration_avsc_Union__29__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__29__();
};

struct GuiConfiguration {
    typedef _configuration_avsc_Union__29__ __uuid_t;
    std::string small_image_url;
    std::string big_image_url;
    __uuid_t __uuid;
    GuiConfiguration() :
        small_image_url(std::string()),
        big_image_url(std::string()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__30__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__30__();
};

struct AndroidConfiguration {
    typedef _configuration_avsc_Union__30__ __uuid_t;
    CanConfiguration can_configuration;
    GuiConfiguration gui_configuration;
    __uuid_t __uuid;
    AndroidConfiguration() :
        can_configuration(CanConfiguration()),
        gui_configuration(GuiConfiguration()),
        __uuid(__uuid_t())
        { }
};

struct _configuration_avsc_Union__31__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    AndroidConfiguration get_AndroidConfiguration() const;
    void set_AndroidConfiguration(const AndroidConfiguration& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__31__();
};

struct _configuration_avsc_Union__32__ {
private:
    size_t idx_;
    boost::any value_;
public:
    size_t idx() const { return idx_; }
    boost::array<uint8_t, 16> get_uuidT() const;
    void set_uuidT(const boost::array<uint8_t, 16>& v);
    bool is_null() const {
        return (idx_ == 1);
    }
    void set_null() {
        idx_ = 1;
        value_ = boost::any();
    }
    _configuration_avsc_Union__32__();
};

struct Configuration {
    typedef _configuration_avsc_Union__12__ opc_ua_configuration_array_t;
    typedef _configuration_avsc_Union__25__ zeus_configuration_t;
    typedef _configuration_avsc_Union__28__ sql_configuration_array_t;
    typedef _configuration_avsc_Union__31__ android_configuration_t;
    typedef _configuration_avsc_Union__32__ __uuid_t;
    CommonMetadata common_metadata;
    opc_ua_configuration_array_t opc_ua_configuration_array;
    zeus_configuration_t zeus_configuration;
    sql_configuration_array_t sql_configuration_array;
    android_configuration_t android_configuration;
    __uuid_t __uuid;
    Configuration() :
        common_metadata(CommonMetadata()),
        opc_ua_configuration_array(opc_ua_configuration_array_t()),
        zeus_configuration(zeus_configuration_t()),
        sql_configuration_array(sql_configuration_array_t()),
        android_configuration(android_configuration_t()),
        __uuid(__uuid_t())
        { }
};

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__0__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__0__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__1__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__1__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__2__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__2__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
opc_metadata_path _configuration_avsc_Union__3__::get_opc_metadata_path() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<opc_metadata_path >(value_);
}

inline
void _configuration_avsc_Union__3__::set_opc_metadata_path(const opc_metadata_path& v) {
    idx_ = 0;
    value_ = v;
}

inline
sql_metadata_query _configuration_avsc_Union__3__::get_sql_metadata_query() const {
    if (idx_ != 1) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<sql_metadata_query >(value_);
}

inline
void _configuration_avsc_Union__3__::set_sql_metadata_query(const sql_metadata_query& v) {
    idx_ = 1;
    value_ = v;
}

inline
value _configuration_avsc_Union__3__::get_value() const {
    if (idx_ != 2) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<value >(value_);
}

inline
void _configuration_avsc_Union__3__::set_value(const value& v) {
    idx_ = 2;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__4__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__4__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__5__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__5__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
std::string _configuration_avsc_Union__6__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _configuration_avsc_Union__6__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
double _configuration_avsc_Union__7__::get_double() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<double >(value_);
}

inline
void _configuration_avsc_Union__7__::set_double(const double& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__8__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__8__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
int32_t _configuration_avsc_Union__9__::get_int() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int32_t >(value_);
}

inline
void _configuration_avsc_Union__9__::set_int(const int32_t& v) {
    idx_ = 0;
    value_ = v;
}

inline
std::string _configuration_avsc_Union__10__::get_string() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::string >(value_);
}

inline
void _configuration_avsc_Union__10__::set_string(const std::string& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__11__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__11__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
std::vector<OpcUaConfiguration > _configuration_avsc_Union__12__::get_array() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::vector<OpcUaConfiguration > >(value_);
}

inline
void _configuration_avsc_Union__12__::set_array(const std::vector<OpcUaConfiguration >& v) {
    idx_ = 0;
    value_ = v;
}

inline
int32_t _configuration_avsc_Union__13__::get_int() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int32_t >(value_);
}

inline
void _configuration_avsc_Union__13__::set_int(const int32_t& v) {
    idx_ = 0;
    value_ = v;
}

inline
float _configuration_avsc_Union__14__::get_float() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<float >(value_);
}

inline
void _configuration_avsc_Union__14__::set_float(const float& v) {
    idx_ = 0;
    value_ = v;
}

inline
int64_t _configuration_avsc_Union__15__::get_long() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<int64_t >(value_);
}

inline
void _configuration_avsc_Union__15__::set_long(const int64_t& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__16__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__16__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
CanValueScaling _configuration_avsc_Union__17__::get_CanValueScaling() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<CanValueScaling >(value_);
}

inline
void _configuration_avsc_Union__17__::set_CanValueScaling(const CanValueScaling& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__18__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__18__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
TimerCounterConfig _configuration_avsc_Union__19__::get_TimerCounterConfig() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<TimerCounterConfig >(value_);
}

inline
void _configuration_avsc_Union__19__::set_TimerCounterConfig(const TimerCounterConfig& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__20__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__20__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__21__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__21__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__22__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__22__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__23__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__23__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__24__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__24__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
ZeusConfiguration _configuration_avsc_Union__25__::get_ZeusConfiguration() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<ZeusConfiguration >(value_);
}

inline
void _configuration_avsc_Union__25__::set_ZeusConfiguration(const ZeusConfiguration& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__26__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__26__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__27__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__27__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
std::vector<SqlConfiguration > _configuration_avsc_Union__28__::get_array() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<std::vector<SqlConfiguration > >(value_);
}

inline
void _configuration_avsc_Union__28__::set_array(const std::vector<SqlConfiguration >& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__29__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__29__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__30__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__30__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline
AndroidConfiguration _configuration_avsc_Union__31__::get_AndroidConfiguration() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<AndroidConfiguration >(value_);
}

inline
void _configuration_avsc_Union__31__::set_AndroidConfiguration(const AndroidConfiguration& v) {
    idx_ = 0;
    value_ = v;
}

inline
boost::array<uint8_t, 16> _configuration_avsc_Union__32__::get_uuidT() const {
    if (idx_ != 0) {
        throw avro::Exception("Invalid type for union");
    }
    return boost::any_cast<boost::array<uint8_t, 16> >(value_);
}

inline
void _configuration_avsc_Union__32__::set_uuidT(const boost::array<uint8_t, 16>& v) {
    idx_ = 0;
    value_ = v;
}

inline _configuration_avsc_Union__0__::_configuration_avsc_Union__0__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__1__::_configuration_avsc_Union__1__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__2__::_configuration_avsc_Union__2__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__3__::_configuration_avsc_Union__3__() : idx_(0), value_(opc_metadata_path()) { }
inline _configuration_avsc_Union__4__::_configuration_avsc_Union__4__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__5__::_configuration_avsc_Union__5__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__6__::_configuration_avsc_Union__6__() : idx_(0), value_(std::string()) { }
inline _configuration_avsc_Union__7__::_configuration_avsc_Union__7__() : idx_(0), value_(double()) { }
inline _configuration_avsc_Union__8__::_configuration_avsc_Union__8__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__9__::_configuration_avsc_Union__9__() : idx_(0), value_(int32_t()) { }
inline _configuration_avsc_Union__10__::_configuration_avsc_Union__10__() : idx_(0), value_(std::string()) { }
inline _configuration_avsc_Union__11__::_configuration_avsc_Union__11__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__12__::_configuration_avsc_Union__12__() : idx_(0), value_(std::vector<OpcUaConfiguration >()) { }
inline _configuration_avsc_Union__13__::_configuration_avsc_Union__13__() : idx_(0), value_(int32_t()) { }
inline _configuration_avsc_Union__14__::_configuration_avsc_Union__14__() : idx_(0), value_(float()) { }
inline _configuration_avsc_Union__15__::_configuration_avsc_Union__15__() : idx_(0), value_(int64_t()) { }
inline _configuration_avsc_Union__16__::_configuration_avsc_Union__16__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__17__::_configuration_avsc_Union__17__() : idx_(0), value_(CanValueScaling()) { }
inline _configuration_avsc_Union__18__::_configuration_avsc_Union__18__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__19__::_configuration_avsc_Union__19__() : idx_(0), value_(TimerCounterConfig()) { }
inline _configuration_avsc_Union__20__::_configuration_avsc_Union__20__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__21__::_configuration_avsc_Union__21__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__22__::_configuration_avsc_Union__22__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__23__::_configuration_avsc_Union__23__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__24__::_configuration_avsc_Union__24__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__25__::_configuration_avsc_Union__25__() : idx_(0), value_(ZeusConfiguration()) { }
inline _configuration_avsc_Union__26__::_configuration_avsc_Union__26__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__27__::_configuration_avsc_Union__27__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__28__::_configuration_avsc_Union__28__() : idx_(0), value_(std::vector<SqlConfiguration >()) { }
inline _configuration_avsc_Union__29__::_configuration_avsc_Union__29__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__30__::_configuration_avsc_Union__30__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
inline _configuration_avsc_Union__31__::_configuration_avsc_Union__31__() : idx_(0), value_(AndroidConfiguration()) { }
inline _configuration_avsc_Union__32__::_configuration_avsc_Union__32__() : idx_(0), value_(boost::array<uint8_t, 16>()) { }
}
namespace avro {
template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__0__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__0__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__0__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::opc_metadata_path> {
    static void encode(Encoder& e, const kaa_configuration::opc_metadata_path& v) {
        avro::encode(e, v.namespace_id);
        avro::encode(e, v.full_tag_path);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::opc_metadata_path& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.namespace_id);
                    break;
                case 1:
                    avro::decode(d, v.full_tag_path);
                    break;
                case 2:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.namespace_id);
            avro::decode(d, v.full_tag_path);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__1__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__1__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__1__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::sql_metadata_query> {
    static void encode(Encoder& e, const kaa_configuration::sql_metadata_query& v) {
        avro::encode(e, v.query);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::sql_metadata_query& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.query);
                    break;
                case 1:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.query);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__2__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__2__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__2__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::value> {
    static void encode(Encoder& e, const kaa_configuration::value& v) {
        avro::encode(e, v.configuration);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::value& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.configuration);
                    break;
                case 1:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.configuration);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__3__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__3__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_opc_metadata_path());
            break;
        case 1:
            avro::encode(e, v.get_sql_metadata_query());
            break;
        case 2:
            avro::encode(e, v.get_value());
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__3__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 3) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa_configuration::opc_metadata_path vv;
                avro::decode(d, vv);
                v.set_opc_metadata_path(vv);
            }
            break;
        case 1:
            {
                kaa_configuration::sql_metadata_query vv;
                avro::decode(d, vv);
                v.set_sql_metadata_query(vv);
            }
            break;
        case 2:
            {
                kaa_configuration::value vv;
                avro::decode(d, vv);
                v.set_value(vv);
            }
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__4__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__4__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__4__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::MetaInfoResolving> {
    static void encode(Encoder& e, const kaa_configuration::MetaInfoResolving& v) {
        avro::encode(e, v.strategy);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::MetaInfoResolving& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.strategy);
                    break;
                case 1:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.strategy);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__5__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__5__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__5__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::CommonMetadata> {
    static void encode(Encoder& e, const kaa_configuration::CommonMetadata& v) {
        avro::encode(e, v.agent_state_upload_period_seconds);
        avro::encode(e, v.device_model);
        avro::encode(e, v.device_serial_number);
        avro::encode(e, v.device_software_version);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::CommonMetadata& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.agent_state_upload_period_seconds);
                    break;
                case 1:
                    avro::decode(d, v.device_model);
                    break;
                case 2:
                    avro::decode(d, v.device_serial_number);
                    break;
                case 3:
                    avro::decode(d, v.device_software_version);
                    break;
                case 4:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.agent_state_upload_period_seconds);
            avro::decode(d, v.device_model);
            avro::decode(d, v.device_serial_number);
            avro::decode(d, v.device_software_version);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__6__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__6__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__6__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::string vv;
                avro::decode(d, vv);
                v.set_string(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__7__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__7__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_double());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__7__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                double vv;
                avro::decode(d, vv);
                v.set_double(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__8__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__8__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__8__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::OpcUaTag> {
    static void encode(Encoder& e, const kaa_configuration::OpcUaTag& v) {
        avro::encode(e, v.namespace_id);
        avro::encode(e, v.path);
        avro::encode(e, v.output_tag);
        avro::encode(e, v.scale);
        avro::encode(e, v.sampling_interval_ms);
        avro::encode(e, v.send_only_on_change);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::OpcUaTag& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.namespace_id);
                    break;
                case 1:
                    avro::decode(d, v.path);
                    break;
                case 2:
                    avro::decode(d, v.output_tag);
                    break;
                case 3:
                    avro::decode(d, v.scale);
                    break;
                case 4:
                    avro::decode(d, v.sampling_interval_ms);
                    break;
                case 5:
                    avro::decode(d, v.send_only_on_change);
                    break;
                case 6:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.namespace_id);
            avro::decode(d, v.path);
            avro::decode(d, v.output_tag);
            avro::decode(d, v.scale);
            avro::decode(d, v.sampling_interval_ms);
            avro::decode(d, v.send_only_on_change);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__9__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__9__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_int());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__9__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                int32_t vv;
                avro::decode(d, vv);
                v.set_int(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__10__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__10__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_string());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__10__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::string vv;
                avro::decode(d, vv);
                v.set_string(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__11__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__11__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__11__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::OpcUaConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::OpcUaConfiguration& v) {
        avro::encode(e, v.tags);
        avro::encode(e, v.host);
        avro::encode(e, v.port);
        avro::encode(e, v.is_connection_secure);
        avro::encode(e, v.reconnect_time_period_seconds);
        avro::encode(e, v.config_id);
        avro::encode(e, v.browse_namespace);
        avro::encode(e, v.browse_id);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::OpcUaConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.tags);
                    break;
                case 1:
                    avro::decode(d, v.host);
                    break;
                case 2:
                    avro::decode(d, v.port);
                    break;
                case 3:
                    avro::decode(d, v.is_connection_secure);
                    break;
                case 4:
                    avro::decode(d, v.reconnect_time_period_seconds);
                    break;
                case 5:
                    avro::decode(d, v.config_id);
                    break;
                case 6:
                    avro::decode(d, v.browse_namespace);
                    break;
                case 7:
                    avro::decode(d, v.browse_id);
                    break;
                case 8:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.tags);
            avro::decode(d, v.host);
            avro::decode(d, v.port);
            avro::decode(d, v.is_connection_secure);
            avro::decode(d, v.reconnect_time_period_seconds);
            avro::decode(d, v.config_id);
            avro::decode(d, v.browse_namespace);
            avro::decode(d, v.browse_id);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__12__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__12__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_array());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__12__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::vector<kaa_configuration::OpcUaConfiguration > vv;
                avro::decode(d, vv);
                v.set_array(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::CanDataType> {
    static void encode(Encoder& e, kaa_configuration::CanDataType v) {
		if (v < kaa_configuration::BOOLEAN || v > kaa_configuration::REAL)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for kaa_configuration::CanDataType and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, kaa_configuration::CanDataType& v) {
		size_t index = d.decodeEnum();
		if (index < kaa_configuration::BOOLEAN || index > kaa_configuration::REAL)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for kaa_configuration::CanDataType and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<kaa_configuration::CanDataType>(index);
    }
};

template<> struct codec_traits<kaa_configuration::CanDataByteOrder> {
    static void encode(Encoder& e, kaa_configuration::CanDataByteOrder v) {
		if (v < kaa_configuration::MSB || v > kaa_configuration::LSB)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for kaa_configuration::CanDataByteOrder and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, kaa_configuration::CanDataByteOrder& v) {
		size_t index = d.decodeEnum();
		if (index < kaa_configuration::MSB || index > kaa_configuration::LSB)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for kaa_configuration::CanDataByteOrder and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<kaa_configuration::CanDataByteOrder>(index);
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__13__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__13__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_int());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__13__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                int32_t vv;
                avro::decode(d, vv);
                v.set_int(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::CanValueScalingOrder> {
    static void encode(Encoder& e, kaa_configuration::CanValueScalingOrder v) {
		if (v < kaa_configuration::MULTIPLIER || v > kaa_configuration::OFFSET)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for kaa_configuration::CanValueScalingOrder and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, kaa_configuration::CanValueScalingOrder& v) {
		size_t index = d.decodeEnum();
		if (index < kaa_configuration::MULTIPLIER || index > kaa_configuration::OFFSET)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for kaa_configuration::CanValueScalingOrder and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<kaa_configuration::CanValueScalingOrder>(index);
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__14__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__14__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_float());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__14__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                float vv;
                avro::decode(d, vv);
                v.set_float(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__15__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__15__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_long());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__15__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                int64_t vv;
                avro::decode(d, vv);
                v.set_long(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__16__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__16__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__16__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::CanValueScaling> {
    static void encode(Encoder& e, const kaa_configuration::CanValueScaling& v) {
        avro::encode(e, v.scalingOrder);
        avro::encode(e, v.multiplier);
        avro::encode(e, v.offset);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::CanValueScaling& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.scalingOrder);
                    break;
                case 1:
                    avro::decode(d, v.multiplier);
                    break;
                case 2:
                    avro::decode(d, v.offset);
                    break;
                case 3:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.scalingOrder);
            avro::decode(d, v.multiplier);
            avro::decode(d, v.offset);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__17__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__17__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_CanValueScaling());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__17__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa_configuration::CanValueScaling vv;
                avro::decode(d, vv);
                v.set_CanValueScaling(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__18__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__18__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__18__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::TimerCounterConfig> {
    static void encode(Encoder& e, const kaa_configuration::TimerCounterConfig& v) {
        avro::encode(e, v.channel_number);
        avro::encode(e, v.data_velocity_ms);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::TimerCounterConfig& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.channel_number);
                    break;
                case 1:
                    avro::decode(d, v.data_velocity_ms);
                    break;
                case 2:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.channel_number);
            avro::decode(d, v.data_velocity_ms);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__19__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__19__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_TimerCounterConfig());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__19__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa_configuration::TimerCounterConfig vv;
                avro::decode(d, vv);
                v.set_TimerCounterConfig(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__20__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__20__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__20__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::CanVariableConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::CanVariableConfiguration& v) {
        avro::encode(e, v.name);
        avro::encode(e, v.type);
        avro::encode(e, v.byteOrder);
        avro::encode(e, v.byteOffset);
        avro::encode(e, v.bitOffset);
        avro::encode(e, v.scaling);
        avro::encode(e, v.data_velocity_ms);
        avro::encode(e, v.send_only_on_change);
        avro::encode(e, v.timer_counter_config);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::CanVariableConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.name);
                    break;
                case 1:
                    avro::decode(d, v.type);
                    break;
                case 2:
                    avro::decode(d, v.byteOrder);
                    break;
                case 3:
                    avro::decode(d, v.byteOffset);
                    break;
                case 4:
                    avro::decode(d, v.bitOffset);
                    break;
                case 5:
                    avro::decode(d, v.scaling);
                    break;
                case 6:
                    avro::decode(d, v.data_velocity_ms);
                    break;
                case 7:
                    avro::decode(d, v.send_only_on_change);
                    break;
                case 8:
                    avro::decode(d, v.timer_counter_config);
                    break;
                case 9:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.name);
            avro::decode(d, v.type);
            avro::decode(d, v.byteOrder);
            avro::decode(d, v.byteOffset);
            avro::decode(d, v.bitOffset);
            avro::decode(d, v.scaling);
            avro::decode(d, v.data_velocity_ms);
            avro::decode(d, v.send_only_on_change);
            avro::decode(d, v.timer_counter_config);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__21__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__21__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__21__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::CanMessageConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::CanMessageConfiguration& v) {
        avro::encode(e, v.interface_name);
        avro::encode(e, v.id);
        avro::encode(e, v.variables);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::CanMessageConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.interface_name);
                    break;
                case 1:
                    avro::decode(d, v.id);
                    break;
                case 2:
                    avro::decode(d, v.variables);
                    break;
                case 3:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.interface_name);
            avro::decode(d, v.id);
            avro::decode(d, v.variables);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__22__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__22__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__22__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::CanConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::CanConfiguration& v) {
        avro::encode(e, v.messages);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::CanConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.messages);
                    break;
                case 1:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.messages);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__23__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__23__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__23__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::ModeConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::ModeConfiguration& v) {
        avro::encode(e, v.can_configuration);
        avro::encode(e, v.accelerometer_threshold);
        avro::encode(e, v.accelerometer_motion_detection_query_period_seconds);
        avro::encode(e, v.gps_threshold_meters_per_second);
        avro::encode(e, v.gps_motion_detection_query_period_seconds);
        avro::encode(e, v.gps_location_query_period_seconds);
        avro::encode(e, v.mode);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::ModeConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.can_configuration);
                    break;
                case 1:
                    avro::decode(d, v.accelerometer_threshold);
                    break;
                case 2:
                    avro::decode(d, v.accelerometer_motion_detection_query_period_seconds);
                    break;
                case 3:
                    avro::decode(d, v.gps_threshold_meters_per_second);
                    break;
                case 4:
                    avro::decode(d, v.gps_motion_detection_query_period_seconds);
                    break;
                case 5:
                    avro::decode(d, v.gps_location_query_period_seconds);
                    break;
                case 6:
                    avro::decode(d, v.mode);
                    break;
                case 7:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.can_configuration);
            avro::decode(d, v.accelerometer_threshold);
            avro::decode(d, v.accelerometer_motion_detection_query_period_seconds);
            avro::decode(d, v.gps_threshold_meters_per_second);
            avro::decode(d, v.gps_motion_detection_query_period_seconds);
            avro::decode(d, v.gps_location_query_period_seconds);
            avro::decode(d, v.mode);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__24__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__24__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__24__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::ZeusConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::ZeusConfiguration& v) {
        avro::encode(e, v.mode_configuration);
        avro::encode(e, v.storage_size_mbytes);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::ZeusConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.mode_configuration);
                    break;
                case 1:
                    avro::decode(d, v.storage_size_mbytes);
                    break;
                case 2:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.mode_configuration);
            avro::decode(d, v.storage_size_mbytes);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__25__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__25__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_ZeusConfiguration());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__25__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa_configuration::ZeusConfiguration vv;
                avro::decode(d, vv);
                v.set_ZeusConfiguration(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::SqlDatabaseType> {
    static void encode(Encoder& e, kaa_configuration::SqlDatabaseType v) {
		if (v < kaa_configuration::NOT_SPECIFIED || v > kaa_configuration::SQLAnywhere)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for kaa_configuration::SqlDatabaseType and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, kaa_configuration::SqlDatabaseType& v) {
		size_t index = d.decodeEnum();
		if (index < kaa_configuration::NOT_SPECIFIED || index > kaa_configuration::SQLAnywhere)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for kaa_configuration::SqlDatabaseType and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<kaa_configuration::SqlDatabaseType>(index);
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__26__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__26__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__26__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::sql_query> {
    static void encode(Encoder& e, const kaa_configuration::sql_query& v) {
        avro::encode(e, v.iteration_sql_query);
        avro::encode(e, v.start_value);
        avro::encode(e, v.update_start_value);
        avro::encode(e, v.poll_period_seconds);
        avro::encode(e, v.iteration_column_name);
        avro::encode(e, v.timestamp_column_name);
        avro::encode(e, v.time_zone);
        avro::encode(e, v.query_id);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::sql_query& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.iteration_sql_query);
                    break;
                case 1:
                    avro::decode(d, v.start_value);
                    break;
                case 2:
                    avro::decode(d, v.update_start_value);
                    break;
                case 3:
                    avro::decode(d, v.poll_period_seconds);
                    break;
                case 4:
                    avro::decode(d, v.iteration_column_name);
                    break;
                case 5:
                    avro::decode(d, v.timestamp_column_name);
                    break;
                case 6:
                    avro::decode(d, v.time_zone);
                    break;
                case 7:
                    avro::decode(d, v.query_id);
                    break;
                case 8:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.iteration_sql_query);
            avro::decode(d, v.start_value);
            avro::decode(d, v.update_start_value);
            avro::decode(d, v.poll_period_seconds);
            avro::decode(d, v.iteration_column_name);
            avro::decode(d, v.timestamp_column_name);
            avro::decode(d, v.time_zone);
            avro::decode(d, v.query_id);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__27__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__27__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__27__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::SqlConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::SqlConfiguration& v) {
        avro::encode(e, v.database_type);
        avro::encode(e, v.database_connection_string);
        avro::encode(e, v.user_name);
        avro::encode(e, v.user_password);
        avro::encode(e, v.sql_queries);
        avro::encode(e, v.config_id);
        avro::encode(e, v.dry_run);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::SqlConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.database_type);
                    break;
                case 1:
                    avro::decode(d, v.database_connection_string);
                    break;
                case 2:
                    avro::decode(d, v.user_name);
                    break;
                case 3:
                    avro::decode(d, v.user_password);
                    break;
                case 4:
                    avro::decode(d, v.sql_queries);
                    break;
                case 5:
                    avro::decode(d, v.config_id);
                    break;
                case 6:
                    avro::decode(d, v.dry_run);
                    break;
                case 7:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.database_type);
            avro::decode(d, v.database_connection_string);
            avro::decode(d, v.user_name);
            avro::decode(d, v.user_password);
            avro::decode(d, v.sql_queries);
            avro::decode(d, v.config_id);
            avro::decode(d, v.dry_run);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__28__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__28__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_array());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__28__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                std::vector<kaa_configuration::SqlConfiguration > vv;
                avro::decode(d, vv);
                v.set_array(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__29__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__29__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__29__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::GuiConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::GuiConfiguration& v) {
        avro::encode(e, v.small_image_url);
        avro::encode(e, v.big_image_url);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::GuiConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.small_image_url);
                    break;
                case 1:
                    avro::decode(d, v.big_image_url);
                    break;
                case 2:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.small_image_url);
            avro::decode(d, v.big_image_url);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__30__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__30__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__30__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::AndroidConfiguration> {
    static void encode(Encoder& e, const kaa_configuration::AndroidConfiguration& v) {
        avro::encode(e, v.can_configuration);
        avro::encode(e, v.gui_configuration);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::AndroidConfiguration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.can_configuration);
                    break;
                case 1:
                    avro::decode(d, v.gui_configuration);
                    break;
                case 2:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.can_configuration);
            avro::decode(d, v.gui_configuration);
            avro::decode(d, v.__uuid);
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__31__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__31__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_AndroidConfiguration());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__31__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                kaa_configuration::AndroidConfiguration vv;
                avro::decode(d, vv);
                v.set_AndroidConfiguration(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::_configuration_avsc_Union__32__> {
    static void encode(Encoder& e, kaa_configuration::_configuration_avsc_Union__32__ v) {
        e.encodeUnionIndex(v.idx());
        switch (v.idx()) {
        case 0:
            avro::encode(e, v.get_uuidT());
            break;
        case 1:
            e.encodeNull();
            break;
        }
    }
    static void decode(Decoder& d, kaa_configuration::_configuration_avsc_Union__32__& v) {
        size_t n = d.decodeUnionIndex();
        if (n >= 2) { throw avro::Exception("Union index too big"); }
        switch (n) {
        case 0:
            {
                boost::array<uint8_t, 16> vv;
                avro::decode(d, vv);
                v.set_uuidT(vv);
            }
            break;
        case 1:
            d.decodeNull();
            v.set_null();
            break;
        }
    }
};

template<> struct codec_traits<kaa_configuration::Configuration> {
    static void encode(Encoder& e, const kaa_configuration::Configuration& v) {
        avro::encode(e, v.common_metadata);
        avro::encode(e, v.opc_ua_configuration_array);
        avro::encode(e, v.zeus_configuration);
        avro::encode(e, v.sql_configuration_array);
        avro::encode(e, v.android_configuration);
        avro::encode(e, v.__uuid);
    }
    static void decode(Decoder& d, kaa_configuration::Configuration& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.common_metadata);
                    break;
                case 1:
                    avro::decode(d, v.opc_ua_configuration_array);
                    break;
                case 2:
                    avro::decode(d, v.zeus_configuration);
                    break;
                case 3:
                    avro::decode(d, v.sql_configuration_array);
                    break;
                case 4:
                    avro::decode(d, v.android_configuration);
                    break;
                case 5:
                    avro::decode(d, v.__uuid);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.common_metadata);
            avro::decode(d, v.opc_ua_configuration_array);
            avro::decode(d, v.zeus_configuration);
            avro::decode(d, v.sql_configuration_array);
            avro::decode(d, v.android_configuration);
            avro::decode(d, v.__uuid);
        }
    }
};

}
#endif
