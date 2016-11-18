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

ALTER TABLE base_schems ALTER COLUMN description TYPE varchar(1024);
ALTER TABLE plugin ALTER COLUMN description TYPE varchar(1024);
ALTER TABLE topic ALTER COLUMN description TYPE varchar(1024);
ALTER TABLE endpoint_group ALTER COLUMN description TYPE varchar(1024);
ALTER TABLE events_class_family ALTER COLUMN description TYPE varchar(1024);

