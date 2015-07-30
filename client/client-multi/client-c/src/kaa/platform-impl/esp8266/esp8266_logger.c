//#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

#include "../../platform/ext_system_logger.h"

#include <stdio.h>
#include <time.h>

kaa_time_t ext_get_systime() {
    return system_get_rtc_time()*((system_rtc_clock_cali_proc()*1000)>>12)/1000; 
};

void ext_write_log(FILE *sink, const char *buffer, size_t message_size) {
//    if(!buffer)
//        return;
    printf("%s", buffer);
}

int ext_logger_sprintf(char *buffer,size_t buffer_size, const char *format, va_list args) {
    return vsnprintf(buffer, buffer_size, format, args);
}

/*int ext_snpintf(char *buffer, size_t buffer_size, const char *format, ...) {
    va_list args;
    va_start(args, format);
    int res_len = vsnprintf(buffer, buffer_size, format, args);
    va_end(args);
    return res_len;
}*/

int ext_format_sprintf(char * buffer, size_t buffer_size, const char * format,
        const char * log_level_name, const char * truncated_name, int lineno, 
        kaa_error_t error_code) {

  //  time_t t = ext_get_systime(NULL);
//    struct tm* tp = gmtime(&t);

    return snprintf(buffer, buffer_size, format, 0,
            0, 0, 0, 0, 0,
            log_level_name, truncated_name, lineno, error_code);
}



