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

#include <time.h>
#include "cc32xx_support.h"

#define DEMO_UNUSED(x) if(x){}


unsigned long  g_ulStatus = 0;//SimpleLink Status
unsigned long  g_ulGatewayIP = 0; //Network Gateway IP address
unsigned char  g_ucConnectionStatus = 0;
unsigned char  g_ucSimplelinkstarted = 0;
unsigned long  g_ulIpAddr = 0;

extern void (* const g_pfnVectors[])(void);

void SimpleLinkWlanEventHandler(SlWlanEvent_t *pWlanEvent)
{
    UART_PRINT("SimpleLinkWlanEventHandler\r\n");
    switch (pWlanEvent->Event) {
        case SL_WLAN_CONNECT_EVENT:
        {
            SET_STATUS_BIT(g_ulStatus, STATUS_BIT_CONNECTION);

            //
            // Information about the connected AP (like name, MAC etc) will be
            // available in 'slWlanConnectAsyncResponse_t'-Applications
            // can use it if required
            //
            //  slWlanConnectAsyncResponse_t *pEventData = NULL;
            // pEventData = &pWlanEvent->EventData.STAandP2PModeWlanConnected;
            //

            UART_PRINT("[WLAN EVENT] STA Connected to the AP: %s\n\r",
                       pWlanEvent->EventData.STAandP2PModeWlanConnected.ssid_name);
        }
        break;

        case SL_WLAN_DISCONNECT_EVENT:
        {
            slWlanConnectAsyncResponse_t*  pEventData = NULL;

            CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_CONNECTION);
            CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_IP_AQUIRED);

            pEventData = &pWlanEvent->EventData.STAandP2PModeDisconnected;

            // If the user has initiated 'Disconnect' request,
            //'reason_code' is SL_USER_INITIATED_DISCONNECTION
            if(SL_USER_INITIATED_DISCONNECTION == pEventData->reason_code)
            {
                UART_PRINT("[WLAN EVENT]Device disconnected from the AP: %s\r\n",
                           pWlanEvent->EventData.STAandP2PModeWlanConnected.ssid_name);
            }
            else
            {
                UART_PRINT("[WLAN ERROR]Device disconnected from the AP AP: %s\r\n",
                           pWlanEvent->EventData.STAandP2PModeWlanConnected.ssid_name);
            }
        }
        break;

        default:
        {
            UART_PRINT("[WLAN EVENT] Unexpected event [0x%x]\n\r",
                       pWlanEvent->Event);
        }
        break;
    }
}

void SimpleLinkNetAppEventHandler(SlNetAppEvent_t *pNetAppEvent)
{
    switch(pNetAppEvent->Event) {
        case SL_NETAPP_IPV4_IPACQUIRED_EVENT:
        {
            SlIpV4AcquiredAsync_t *pEventData = NULL;

            SET_STATUS_BIT(g_ulStatus, STATUS_BIT_IP_AQUIRED);

            //Ip Acquired Event Data
            pEventData = &pNetAppEvent->EventData.ipAcquiredV4;

            //Gateway IP address
            g_ulGatewayIP = pEventData->gateway;

            UART_PRINT("[NETAPP EVENT] IP Acquired: IP=%d.%d.%d.%d , "
            "Gateway=%d.%d.%d.%d\n\r",
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,3),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,2),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,1),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,0),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,3),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,2),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,1),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,0));
        }
        break;

        default:
        {
            UART_PRINT("[NETAPP EVENT] Unexpected event [0x%x] \n\r",
                       pNetAppEvent->Event);
        }
        break;
    }
}

void SimpleLinkHttpServerCallback(SlHttpServerEvent_t *pHttpEvent,
                                  SlHttpServerResponse_t *pHttpResponse)
{
    DEMO_UNUSED(pHttpEvent)
    DEMO_UNUSED(pHttpResponse)
    // Unused in this application
}

void SimpleLinkGeneralEventHandler(SlDeviceEvent_t *pDevEvent)
{
    DEMO_UNUSED(pDevEvent)
    // Unused in this application
}

void SimpleLinkSockEventHandler(SlSockEvent_t *pSock)
{
    DEMO_UNUSED(pSock)
    // Unused in this application
}

void SimpleLinkPingReport(SlPingReport_t *report)
{
    SET_STATUS_BIT(g_ulStatus, STATUS_BIT_PING_DONE);
    UART_PRINT("ping %d\r\n", report->AvgRoundTime);
}

void BoardInit()
{
    MAP_IntVTableBaseSet((unsigned long)&g_pfnVectors[0]);

    MAP_IntMasterEnable();
    MAP_IntEnable(FAULT_SYSTICK);

    PRCMCC3200MCUInit();

    UDMAInit();
    InitTerm();
}


void wlan_configure()
{
    sl_Start(NULL, NULL, NULL);

    //
    // reset all network policies
    //
    unsigned char ucpolicyVal;
    int ret;
    ret = sl_WlanPolicySet(SL_POLICY_CONNECTION,
                    SL_CONNECTION_POLICY(0,0,0,0,0),
                    &ucpolicyVal,
                    1 /*PolicyValLen*/);
    if (ret < 0) {
        LOOP_FOREVER();
    }

    sl_WlanSetMode(ROLE_STA);
    sl_WlanPolicySet(SL_POLICY_CONNECTION, SL_CONNECTION_POLICY(1, 0, 0, 0, 1), NULL, 0);
    sl_WlanProfileDel(0xFF);
    sl_WlanDisconnect();

    _WlanRxFilterOperationCommandBuff_t  RxFilterIdMask;// = {0};
    memset(&RxFilterIdMask, 0, sizeof(RxFilterIdMask));

    unsigned char ucVal = 0;
    unsigned char ucConfigOpt = 0;
    // Enable DHCP client
    sl_NetCfgSet(SL_IPV4_STA_P2P_CL_DHCP_ENABLE,1,1,&ucVal);
    // Disable scan
    ucConfigOpt = SL_SCAN_POLICY(0);
    sl_WlanPolicySet(SL_POLICY_SCAN , ucConfigOpt, NULL, 0);
    // Set Tx power level for station mode
    // Number between 0-15, as dB offset from max power - 0 will set max power
    unsigned char ucPower = 0;
    sl_WlanSet(SL_WLAN_CFG_GENERAL_PARAM_ID, WLAN_GENERAL_PARAM_OPT_STA_TX_POWER, 1, (unsigned char *)&ucPower);
    // Set PM policy to normal
    sl_WlanPolicySet(SL_POLICY_PM , SL_NORMAL_POLICY, NULL, 0);
    // Unregister mDNS services
    sl_NetAppMDNSUnRegisterService(0, 0);
    // Remove  all 64 filters (8*8)
    memset(RxFilterIdMask.FilterIdMask, 0xFF, 8);
    sl_WlanRxFilterSet(SL_REMOVE_RX_FILTER, (_u8 *)&RxFilterIdMask, sizeof(_WlanRxFilterOperationCommandBuff_t));
    sl_Stop(SL_STOP_TIMEOUT);
}

void wlan_scan()
{
    unsigned char ucpolicyOpt;
    union {
        unsigned char ucPolicy[4];
        unsigned int uiPolicyLen;
    } policyVal;

    ucpolicyOpt = SL_CONNECTION_POLICY(0, 0, 0, 0,0);
    sl_WlanPolicySet(SL_POLICY_CONNECTION , ucpolicyOpt, NULL, 0);
    ucpolicyOpt = SL_SCAN_POLICY(1);
    policyVal.uiPolicyLen = 10;
    sl_WlanPolicySet(SL_POLICY_SCAN , ucpolicyOpt, (unsigned char*)(policyVal.ucPolicy), sizeof(policyVal));
    MAP_UtilsDelay(8000000);

    Sl_WlanNetworkEntry_t netEntries[10];
    _i16 resultsCount = sl_WlanGetNetworkList(0,10,&netEntries[0]);
    for (int i=0; i< resultsCount; i++)
        UART_PRINT("ssid: %s\trssi: %d\tsec-t: %u\r\n",netEntries[i].ssid, netEntries[i].rssi, netEntries[i].sec_type);
}

int wlan_connect(const char *ssid, const char *pass, unsigned char sec_type)
{
    SlSecParams_t secParams = {0};
    long lRetVal = 0;

    secParams.Key = (signed char*)pass;
    secParams.KeyLen = strlen(pass);
    secParams.Type = sec_type;

    lRetVal = sl_WlanConnect((signed char*)ssid, strlen(ssid), 0, &secParams, 0);
    ASSERT_ON_ERROR(lRetVal);

    while((!IS_CONNECTED(g_ulStatus)) || (!IS_IP_ACQUIRED(g_ulStatus)))
        _SlNonOsMainLoopTask();   

    SlDateTime_t dateTime= {0};
    dateTime.sl_tm_day =   1;          // Day of month (DD format) range 1-13
    dateTime.sl_tm_mon =   1;           // Month (MM format) in the range of 1-12
    dateTime.sl_tm_year =  1970;        // Year (YYYY format)
    dateTime.sl_tm_hour =  0;          // Hours in the range of 0-23
    dateTime.sl_tm_min =   0;          // Minutes in the range of 0-59
    dateTime.sl_tm_sec =   1;          // Seconds in the range of  0-59

    lRetVal = sl_DevSet(SL_DEVICE_GENERAL_CONFIGURATION, SL_DEVICE_GENERAL_CONFIGURATION_DATE_TIME,
              sizeof(SlDateTime_t),(unsigned char *)(&dateTime));
    ASSERT_ON_ERROR(lRetVal);

    return 0;
}

void net_ping(const char *host)
{
    SlPingStartCommand_t pingParams = {0};
    SlPingReport_t pingReport = {0};
    unsigned long ulIpAddr = 0;
    CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_PING_DONE);

    // Set the ping parameters
    pingParams.PingIntervalTime = 1000;
    pingParams.PingSize = 20;
    pingParams.PingRequestTimeout = 3000;
    pingParams.TotalNumberOfAttempts = 3;
    pingParams.Flags = 0;
    pingParams.Ip = g_ulGatewayIP;

    UART_PRINT("ping host: %s\r\n", host);

    sl_NetAppDnsGetHostByName((signed char*)host, strlen(host), &ulIpAddr, SL_AF_INET);
    UART_PRINT("host ip: 0x%08X\r\n", host);
    pingParams.Ip = ulIpAddr;
    sl_NetAppPingStart((SlPingStartCommand_t*)&pingParams, SL_AF_INET, (SlPingReport_t*)&pingReport, SimpleLinkPingReport);

    while (!IS_PING_DONE(g_ulStatus))
        _SlNonOsMainLoopTask();
}
