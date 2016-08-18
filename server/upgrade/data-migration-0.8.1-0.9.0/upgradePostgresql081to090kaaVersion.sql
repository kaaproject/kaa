/*
 * Copyright 2014 CyberVision, Inc.
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

ALTER TABLE schema RENAME TO schems;
ALTER TABLE base_schema RENAME TO base_schems;
ALTER TABLE change RENAME TO changes;
ALTER TABLE configuration_schema RENAME TO configuration_schems;
ALTER TABLE event_schema_version RENAME TO event_schems_versions;
ALTER TABLE log_schema RENAME TO log_schems;
ALTER TABLE notification_schema RENAME TO notification_schems;
ALTER TABLE profile_schema RENAME TO profile_schems;
ALTER TABLE server_profile_schema RENAME TO server_profile_schems;
ALTER TABLE event_class RENAME TO events_class;
ALTER TABLE event_class_family RENAME TO events_class_family;
ALTER TABLE application_event_family_map RENAME COLUMN event_class_family_id TO events_class_family_id;
ALTER TABLE application_event_map RENAME COLUMN event_class_id TO events_class_id;
ALTER TABLE configuration RENAME COLUMN configuration_schema_id TO configuration_schems_id;
ALTER TABLE configuration RENAME COLUMN configuration_schema_version TO configuration_schems_version;
ALTER TABLE configuration_schems RENAME COLUMN base_schema TO base_schems;
ALTER TABLE configuration_schems RENAME COLUMN protocol_schema TO protocol_schems;
ALTER TABLE configuration_schems RENAME COLUMN override_schema TO override_schems;
ALTER TABLE events_class RENAME COLUMN schema TO schems;
ALTER TABLE events_class RENAME COLUMN event_class_family_id TO events_class_family_id;
ALTER TABLE event_schems_versions RENAME COLUMN schema TO schems;
ALTER TABLE event_schems_versions RENAME COLUMN event_class_family_id TO events_class_family_id;
ALTER TABLE log_appender RENAME COLUMN max_log_schema_version TO max_log_schems_version;
ALTER TABLE log_appender RENAME COLUMN min_log_schema_version TO min_log_schems_version;
ALTER TABLE profile_filter RENAME COLUMN endpoint_schema_id TO endpoint_schems_id;
ALTER TABLE profile_filter RENAME COLUMN server_schema_id TO server_schems_id;
ALTER TABLE schems RENAME COLUMN schema TO schems;
ALTER TABLE sdk_token RENAME COLUMN configuration_schema_version TO configuration_schems_version;
ALTER TABLE sdk_token RENAME COLUMN log_schema_version TO log_schems_version;
ALTER TABLE sdk_token RENAME COLUMN notification_schema_version TO notification_schems_version;
ALTER TABLE sdk_token RENAME COLUMN profile_schema_version TO profile_schems_version;
ALTER TABLE history RENAME COLUMN change_id TO changes_id;
