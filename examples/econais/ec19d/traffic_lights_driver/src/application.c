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

/*
 *@file application.c
*/

#include "application.h"
#include "sndc_sdk_api.h"
typedef long long int64_t;
#include "kaa/kaa.h"
#include "kaa/kaa_profile.h"
#include "kaa/kaa_configuration_manager.h"
#include "kaa/kaa_context.h"
#include "kaa/platform/ext_tcp_utils.h"
#include "kaa/utilities/kaa_log.h"
#include "kaa/utilities/kaa_mem.h"

#include "kaa/gen/kaa_logging_definitions.h"
#include "kaa_client.h"


/* API level that the application is using */
#define SDK_APP_API_LEVEL 5


#define BUTTON  IO_SDIO_DAT2  /* Use the SW2 Button */

//Define your own WiFi SSID and passphrase
#define SSID_STR   "cyber9"
#define USE_ENCRYPTION
#define PASSPHRASE "Cha5hk123"
/*****************************************************************************
E X T E R N A L S
*****************************************************************************/

/*****************************************************************************
T E M P O R A R Y   T E S T V A R I A B L E S
*****************************************************************************/

/*****************************************************************************
C O N S T A N T S / M A C R O S
*****************************************************************************/
/*
 * Hard-coded Kaa profile body.
 */
//#define KAA_ECONAIS_DEMO_PROFILE_ID "simplified"


/*****************************************************************************
L O C A L   D A T A T Y P E S
*****************************************************************************/

/*****************************************************************************
L O C A L   F U N C T I O N   P R O T O T Y P E S
*****************************************************************************/
static void   APP_main();
static bool_t APP_handle_msg(sndc_appmsg_msg_t* msg);
static bool_t APP_handle_error_cb(bool_t user_called, uint16_t ecode);
static void   APP_config_cb();

/*****************************************************************************
 M O D U L E   V A R I A B L E S
*****************************************************************************/
static sndc_config_t const * sndc_device_config;
static bool_t ip_connected;
static bool_t kaa_started;
static bool_t testDone = false;
static kaa_client_t *kaa_client = NULL;
//static kaa_profile_name_t *kaa_econais_profile = NULL; //Profile example

/*****************************************************************************
G L O B A L   C O N S T A N T S / V A R I A B L E S
*****************************************************************************/

/*****************************************************************************
G L O B A L   F U N C T I O N S
*****************************************************************************/

__run_once void APP_init(void)
{
   /*
   NOTES :
      - APP_main is the entry point of the application thread
      - APP_handle_msg is the function that will handle the messages dispatched by wifi engine
      - APP_handle_error_cb will be called to handle system errors/traps/exceptions.
      - APP_config_cb The callback that can apply the configurations at the proper time during the initialization sequence
      - SDK_APP_API_LEVEL is the current level of the SDK API that is used as defined above.
   */
   sndc_sys_init(APP_main, APP_handle_msg, APP_handle_error_cb, APP_config_cb,
                 SDK_APP_API_LEVEL);
}

/*****************************************************************************
L O C A L    F U N C T I O N S
*****************************************************************************/

/* This function can initialize sndc_config_t parameters
 * during system startup.
 * Note: Certain configuration values can only be correctly setup
 * on startup and not during runtime.
 */
static void APP_config_cb()
{
   /* sndc_config_set.. */

   sndc_config_apply();
}

static void start_client(void)
{
   sndc_printf("Connecting as client...\n");

   /* Clear all profiles */
   sndc_profile_eraseAll();

   /* clean the local profile and read the stored valies */
   sndc_profile_t *connect_profile = sndc_profile_getNew();

   /* lock the profile for direct writing */
   sndc_profile_writeLock();

   /*copy ssid*/
   sndc_profile_set_ssidStr(connect_profile, SSID_STR);

#ifdef USE_ENCRYPTION
   sndc_profile_set_passphraseStr(connect_profile, PASSPHRASE);
   sndc_profile_set_wpa1wpa2(connect_profile, TRUE);
#else
   sndc_profile_set_open(connect_profile, TRUE);
#endif

   /* work as client station*/
   sndc_profile_set_role(connect_profile, SNDC_PROFILE_ROLE_CLIENT);

   /* do not keep profile */
   sndc_profile_set_deleteOnDisconnect(connect_profile, TRUE);

   /* unlock the profile */
   sndc_profile_writeUnlock();

   /* request connection to the specific profile */
   sndc_cm_profileConnect(connect_profile);

   /* Release profile reference */
   sndc_profile_release(connect_profile);
}

/* -------------------------------------------------------------------------*/

#define MAIN_ROAD_RED_LIGHT          IO_GPIO_1
#define MAIN_ROAD_YELLOW_LIGHT       IO_GPIO_2
#define MAIN_ROAD_GREEN_LIGHT        IO_GPIO_3

#define SECONDARY_ROAD_RED_LIGHT     IO_GPIO_4
#define SECONDARY_ROAD_YELLOW_LIGHT  IO_GPIO_5
#define SECONDARY_ROAD_GREEN_LIGHT   IO_GPIO_6

void switch_lights(int allow_red, int allow_yellow, int allow_green, int disallow_red, int disallow_yellow, int disallow_green)
{
    sndc_io_write(disallow_green, 0);

    sndc_io_write(disallow_yellow, 1);
    sndc_thrd_delay(4000 * SNDC_MILLISECOND);
    sndc_io_write(disallow_yellow, 0);

    sndc_io_write(disallow_red, 1);

    sndc_thrd_delay(1000 * SNDC_MILLISECOND);
    sndc_io_write(allow_red, 0);
    sndc_io_write(allow_yellow, 0);
    sndc_io_write(allow_green, 1);
}

static kaa_configuration_main_road_state_t main_road_state = ENUM_MAIN_ROAD_STATE_DISALLOW;

kaa_error_t kaa_on_configuration_updated(void *context, const kaa_root_configuration_t *configuration)
{
    sndc_printf("Configuration updated\n");
    if (main_road_state != configuration->main_road_state) {
        switch (configuration->main_road_state) {
        case ENUM_MAIN_ROAD_STATE_ALLOW:
            switch_lights(MAIN_ROAD_RED_LIGHT, MAIN_ROAD_YELLOW_LIGHT, MAIN_ROAD_GREEN_LIGHT, SECONDARY_ROAD_RED_LIGHT, SECONDARY_ROAD_YELLOW_LIGHT, SECONDARY_ROAD_GREEN_LIGHT);
            break;
        case ENUM_MAIN_ROAD_STATE_DISALLOW:
            switch_lights(SECONDARY_ROAD_RED_LIGHT, SECONDARY_ROAD_YELLOW_LIGHT, SECONDARY_ROAD_GREEN_LIGHT, MAIN_ROAD_RED_LIGHT, MAIN_ROAD_YELLOW_LIGHT, MAIN_ROAD_GREEN_LIGHT);
            break;
        }
        main_road_state = configuration->main_road_state;
    }
    return KAA_ERR_NONE;
}

static void APP_main()
{
   sndc_io_setMode(MAIN_ROAD_RED_LIGHT, IO_MODE_OUTPUT);
   sndc_io_setMode(MAIN_ROAD_YELLOW_LIGHT, IO_MODE_OUTPUT);
   sndc_io_setMode(MAIN_ROAD_GREEN_LIGHT, IO_MODE_OUTPUT);
   sndc_io_setMode(SECONDARY_ROAD_RED_LIGHT, IO_MODE_OUTPUT);
   sndc_io_setMode(SECONDARY_ROAD_YELLOW_LIGHT, IO_MODE_OUTPUT);
   sndc_io_setMode(SECONDARY_ROAD_GREEN_LIGHT, IO_MODE_OUTPUT);

   switch_lights(SECONDARY_ROAD_RED_LIGHT, SECONDARY_ROAD_YELLOW_LIGHT, SECONDARY_ROAD_GREEN_LIGHT, MAIN_ROAD_RED_LIGHT, MAIN_ROAD_YELLOW_LIGHT, MAIN_ROAD_GREEN_LIGHT);

   ip_connected = false;
   kaa_started = false;

   kaa_error_t kaa_error = KAA_ERR_NONE;
   { //Used to limit kaa_props visibility, it creates on stack and release once is used
       kaa_client_props_t kaa_props;
       kaa_props.max_update_time = 10;
       kaa_error = kaa_client_create(&kaa_client, &kaa_props);
       if (kaa_error) {
           sndc_printf("Error %d initializing Kaa client \n",kaa_error);
           return;
       }
   }

   /**
    * Configuration example, below is how to configure and read default configuration values
    */
   kaa_configuration_root_receiver_t receiver = { NULL, &kaa_on_configuration_updated };
   kaa_error = kaa_configuration_manager_set_root_receiver(kaa_client_get_context(kaa_client)->configuration_manager, &receiver);
   sndc_printf("Configuration setting done. %d\n", kaa_error);
   //sndc_thrd_delay(TRACE_DELAY * SNDC_MILLISECOND);
   const kaa_root_configuration_t *root_config = kaa_configuration_manager_get_configuration(kaa_client_get_context(kaa_client)->configuration_manager);
   kaa_on_configuration_updated(NULL, root_config);

   // set SW2 button as key input
   sndc_io_ctrl(BUTTON,
                IO_PIN_FUNC_PULL_UP,
                IO_PIN_DRIVE_DEFAULT,
                IO_PIN_SLEW_RATE_DEFAULT);
   sndc_io_setMode(BUTTON, IO_MODE_KEY);
   
   sndc_device_config = sndc_config_get();

   /* clean all profiles */
   sndc_profile_eraseAll();

   start_client();

   //infinite thread loop, button press is monitored by system events
   while(1)
   {
      
      if (ip_connected && !kaa_started) {
          kaa_client_start(kaa_client);
          kaa_started = true;
      }
      //thread sleep for 500 ms
      sndc_thrd_delay(500 * SNDC_MILLISECOND);
   }
}

/* ---------------------------------------------------------------------------------------------------- */

static bool_t APP_handle_msg(sndc_appmsg_msg_t* msg)
{
   bool_t consumed = FALSE;
   switch(msg->id)
   {
      case SNDC_APPMSG_SCAN_COMPLETED:
      {
         sndc_printf("SNDC_APPMSG_SCAN_COMPLETED\n");
         break;
      }
      case SNDC_APPMSG_SCAN_IND:
      {
         sndc_printf("SNDC_APPMSG_SCAN_IND\n");
         break;
      }
      case SNDC_APPMSG_P2P_EVENT:
      {
         sndc_printf("SNDC_APPMSG_P2P_EVENT\n");
         break;
      }
      case SNDC_APPMSG_CM_EVENT:
      {
        sndc_printf("SNDC_APPMSG_CM_EVENT\n");
        sndc_appmsg_cmEvent_t *cm_event = (sndc_appmsg_cmEvent_t *)msg->par;

        sndc_printf("GOT SNDC_APPMSG_CM_EVENT FROM PROFILE ID: %d WITH ID: %d \n", cm_event->priv.profileID, cm_event->id);
        switch(cm_event->id)
        {
         case EVENT_TYPE_CM_PEER_STATUS:
         {
            switch(cm_event->priv.u.peer_status){
               case SNDC_CM_WLAN_DISCONNECTED: sndc_printf( "SNDC_CM_WLAN_DISCONNECTED\n"); break;
               case SNDC_CM_WLAN_CONNECT_TIMEOUT: sndc_printf( "SNDC_CM_WLAN_CONNECT_TIMEOUT\n"); break;
               case SNDC_CM_WLAN_CONNECT_FAILED: sndc_printf( "SNDC_CM_WLAN_CONNECT_FAILED\n"); break;
               case SNDC_CM_WLAN_WPS_FAILED: sndc_printf( "SNDC_CM_WLAN_WPS_FAILED\n"); break;
               case SNDC_CM_WLAN_WPS_TIMEOUT: sndc_printf( "SNDC_CM_WLAN_WPS_TIMEOUT\n"); break;
               case SNDC_CM_WLAN_CONNECTED: sndc_printf( "SNDC_CM_WLAN_CONNECTED\n"); break;
               default: sndc_printf( "[%d] peer_status:%d  \n",cm_event->priv.profileID, cm_event->priv.u.peer_status); break;
            }
            break;
         }
         case EVENT_TYPE_CM_CLIENT_STATUS:
            break;
         case EVENT_TYPE_CM_GO_STATUS:
            break;
         case EVENT_TYPE_CM_IP_ACQUIRED:
         {
            sndc_profile_ipConfig_t *ipconfig = cm_event->priv.u.ip_acquired.ipconfig;
            sndc_printf("**********************\n");
            sndc_printf("IP acquired, status = %d\n", cm_event->priv.u.ip_acquired.status);
            if(cm_event->priv.u.ip_acquired.status == 0)
            {
               assert(ipconfig);
               sndc_printf("IP addr: %s\n", sndc_inet_ntoa(ipconfig->ip_address));
               sndc_printf("Netmask: %s\n", sndc_inet_ntoa(ipconfig->netmask));
               sndc_printf("Gateway: %s\n", sndc_inet_ntoa(ipconfig->gateway));
               ip_connected = true;
               sndc_sys_initRandom(sndc_sys_getTimestamp_msec());
            }
            sndc_printf("**********************\n");


            break;
         }
         case EVENT_TYPE_CM_AUTO_MODE_STATE_CHANGED:
         {
            sndc_printf( "[%d] AutoMode state changed:%d  \n",cm_event->priv.profileID, cm_event->priv.u.auto_mode_state);
            break;
         }

        }

        break;
      }
      case SNDC_APPMSG_IO_EVENT:
      {
         kaa_logging_traffic_lights_log_t *record = kaa_logging_traffic_lights_log_create();
         record->event_type = ENUM_EVENT_TYPE_BUTTON;
         kaa_client_log_record(kaa_client, record);
         record->destroy(record);
         break;
      }
      
      default:
         break;
   }
   
   return consumed;
}

/* ---------------------------------------------------------------------------------------------------- */

static bool_t APP_handle_error_cb(bool_t user_called, uint16_t ecode)
{
   return FALSE;
}


/******************************* END OF FILE ********************************/
