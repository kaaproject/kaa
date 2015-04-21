include ../../../../../applications/$(SDK_APP)/src/kaa/platform-impl/Econais/EC19D/filelist.mak
CFILES-SDK_APP = application.c kaa_client.c $(CFILES-KAA)

OBJS-$(SDK_APP) := $(CFILES-SDK_APP:.c=.o) $(SFILES-SDK_APP:.s=.o)
CFLAGS-SDK_APP = 
CFLAGS += "-D ECONAIS_PLATFORM"
# CFLAGS += "-D KAA_TRACE_MEMORY_ALLOCATIONS"

lint.sdk_app : $(CFILES-SDK_APP:.c=.lnt)
$(SDK_APP).a : $(OBJS-$(SDK_APP))

