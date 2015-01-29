CFILES-ECONAIS-PLAT = kaa/platform-impl/Econais/EC19D/__ashldi3.c kaa/platform-impl/Econais/EC19D/logger.c kaa/platform-impl/Econais/EC19D/sha.c
CFILES-PLAT-IMPL = kaa/platform-impl/ext_log_storage_memory.c kaa/platform-impl/ext_log_upload_strategy_by_volume.c
CFILES-AVRO = kaa/avro_src/io.c kaa/avro_src/encoding_binary.c
CFILES-COLLECTIONS = kaa/collections/kaa_deque.c kaa/collections/kaa_list.c
CFILES-UTIL = kaa/utilities/kaa_log.c kaa/utilities/kaa_mem.c
CFILES-GEN = kaa/gen/kaa_logging_gen.c kaa/gen/kaa_profile_gen.c

CFILES-KAA = $(CFILES-ECONAIS-PLAT) $(CFILES-PLAT-IMPL) $(CFILES-AVRO) $(CFILES-COLLECTIONS) $(CFILES-GEN) $(CFILES-UTIL) kaa/kaa.c kaa/kaa_common_schema.c kaa/kaa_logging.c kaa/kaa_status.c kaa/kaa_channel_manager.c kaa/kaa_platform_utils.c kaa/kaa_bootstrap_manager.c kaa/kaa_event.c kaa/kaa_platform_protocol.c kaa/kaa_profile.c kaa/kaa_user.c
