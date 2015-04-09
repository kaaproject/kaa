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

#define LIGHT_ZONES_COUNT    6
static const int light_zones[] = { IO_GPIO_1, IO_GPIO_2, IO_GPIO_3, IO_GPIO_4, IO_GPIO_5, IO_GPIO_6 };

kaa_error_t kaa_on_configuration_updated(void *context, const kaa_root_configuration_t *configuration)
{
    int i = 0;
    for(; i < LIGHT_ZONES_COUNT; ++i) {
        sndc_io_write(light_zones[i], 0);
    }
    sndc_printf("Configuration updated\n");
    kaa_list_t *it = configuration->light_zones;
    while (it) {
        kaa_configuration_light_zone_t *zone = (kaa_configuration_light_zone_t *) kaa_list_get_data(it);
        if (zone->zone_id >= 0 && zone->zone_id < LIGHT_ZONES_COUNT) {
            switch (zone->zone_status) {
            case ENUM_ZONE_STATUS_ENABLE:
                sndc_io_write(light_zones[zone->zone_id], 1);
                break;
            case ENUM_ZONE_STATUS_DISABLE:
                sndc_io_write(light_zones[zone->zone_id], 0);
                break;
            }
        }
        it = kaa_list_next(it);
    }
    return KAA_ERR_NONE;
}

static void APP_main()
{
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

   kaa_list_t *zones = NULL;
   int i = 0;
   for(; i < LIGHT_ZONES_COUNT; ++i) {
       sndc_io_setMode(light_zones[i], IO_MODE_OUTPUT);
       int32_t *zone_id = (int32_t *) KAA_MALLOC(sizeof(int32_t));
       *zone_id = i;
       if (zones) {
           zones = kaa_list_push_front(zones, zone_id);
       } else {
           zones = kaa_list_create(zone_id);
       }
   }
   kaa_profile_street_lights_profile_t *profile = kaa_profile_street_lights_profile_create();
   profile->light_zones = zones;
   kaa_profile_manager_update_profile(kaa_client_get_context(kaa_client)->profile_manager, profile);
   profile->destroy(profile);

   /**
    * Configuration example, below is how to configure and read default configuration values
    */
   kaa_configuration_root_receiver_t receiver = { NULL, &kaa_on_configuration_updated };
   kaa_error = kaa_configuration_manager_set_root_receiver(kaa_client_get_context(kaa_client)->configuration_manager, &receiver);
   sndc_printf("Configuration setting done. %d\n", kaa_error);
   //sndc_thrd_delay(TRACE_DELAY * SNDC_MILLISECOND);
   const kaa_root_configuration_t *root_config = kaa_configuration_manager_get_configuration(kaa_client_get_context(kaa_client)->configuration_manager);
   kaa_on_configuration_updated(NULL, root_config);

   //set SW2 button as key input
   //sndc_io_ctrl(BUTTON,
   //             IO_PIN_FUNC_PULL_UP,
   //             IO_PIN_DRIVE_DEFAULT,
   //             IO_PIN_SLEW_RATE_DEFAULT);
   //sndc_io_setMode(BUTTON, IO_MODE_KEY);
   
   //sndc_device_config = sndc_config_get();

   /* clean all profiles */
   //sndc_profile_eraseAll();
   //sndc_printf("Press SW2 to start test. \n");

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
         sndc_printf("SNDC_APPMSG_IO_EVENT\n");
         sndc_appmsg_ioEvent_t *io_event = (sndc_appmsg_ioEvent_t *)msg->par;
         if (testDone) {
             sndc_printf("SNDC_APPMSG_IO_EVENT level %d, pinMask %d\n", io_event->level, io_event->pin_mask);
             //Logging example, it just log button pressing.
             //kaa_client_log_record(kaa_client, "Button pressed");
         } else {
             testDone  = true;
             sndc_printf("Button pressed. \n");
             start_client();
         }
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
