#
# Copyright 2014-2015 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


CFILES-ECONAIS-PLAT = kaa/platform-impl/Econais/EC19D/econais_ec19d_kaa_client.c kaa/platform-impl/Econais/EC19D/__ashldi3.c kaa/platform-impl/Econais/EC19D/logger.c kaa/platform-impl/Econais/EC19D/sha.c kaa/platform-impl/Econais/EC19D/econais_ec19d_tcp_utils.c kaa/platform-impl/Econais/EC19D/econais_ec19d_file_utils.c kaa/platform-impl/Econais/EC19D/econais_ec19d_configuration_persistence.c
CFILES-PLAT-IMPL = kaa/platform-impl/common/ext_log_storage_memory.c kaa/platform-impl/common/ext_log_upload_strategies.c kaa/platform-impl/common/kaa_failover_strategy.c kaa/platform-impl/common/kaa_tcp_channel.c kaa/platform-impl/common/kaa_htonll.c
CFILES-PROTO = kaa/kaa_protocols/kaa_tcp/kaatcp_parser.c kaa/kaa_protocols/kaa_tcp/kaatcp_request.c
CFILES-AVRO = kaa/avro_src/io.c kaa/avro_src/encoding_binary.c
CFILES-COLLECTIONS = kaa/collections/kaa_list.c
CFILES-UTIL = kaa/utilities/kaa_log.c kaa/utilities/kaa_mem.c kaa/utilities/kaa_buffer.c
CFILES-GEN = kaa/gen/kaa_logging_gen.c kaa/gen/kaa_profile_gen.c kaa/gen/kaa_configuration_gen.c kaa/gen/kaa_notification_gen.c

CFILES-KAA = $(CFILES-ECONAIS-PLAT) $(CFILES-PLAT-IMPL) $(CFILES-PROTO) $(CFILES-AVRO) $(CFILES-COLLECTIONS) $(CFILES-GEN) $(CFILES-UTIL) kaa/kaa.c kaa/kaa_common_schema.c kaa/kaa_logging.c kaa/kaa_status.c kaa/kaa_channel_manager.c kaa/kaa_platform_utils.c kaa/kaa_bootstrap_manager.c kaa/kaa_event.c kaa/kaa_platform_protocol.c kaa/kaa_profile.c kaa/kaa_user.c kaa/kaa_configuration_manager.c kaa/kaa_notification_manager.c
