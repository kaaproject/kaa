{% include variables.md %}


<!--== Features and components ==-->
{% capture kaa_features_url %}{{root_url}}Features/{% endcapture %}
[kaa features]: {{kaa_features_url}}

<!-- Device management -->
{% capture feature_device_mgmt_url %}{{kaa_features_url}}Device-management/{% endcapture %}
[identity]: {{feature_device_mgmt_url}}
[device management]: {{feature_device_mgmt_url}}

{% capture epr_url %}{{feature_device_mgmt_url}}EPR/{% endcapture %}
[EPR]:                          {{epr_url}}
[EPR REST API]:                 {{epr_url}}REST-API/
[EPR endpoint POST REST API]:   {{epr_url}}REST-API/#endpoints_post

{% capture epmx_url %}{{feature_device_mgmt_url}}EPMX/{% endcapture %}
[EPMX]: {{epmx_url}}

{% capture cm_url %}{{feature_device_mgmt_url}}CM/{% endcapture %}
[CM]: {{cm_url}}
[CM REST API]:                  {{cm_url}}REST-API/
[CM clients REST API]:          {{cm_url}}REST-API/#clients
[CM certificate POST REST API]: {{cm_url}}REST-API/#clients_certificates_post


<!-- Protocols -->
{% capture 1kp %}https://github.com/kaaproject/kaa-rfcs/blob/master/0001/README.md{% endcapture %}
[1/KP]: {{1kp}}
[1/KP request ID]: {{1kp}}#request-id

<!-- Communication -->
{% capture feature_communication_url %}{{kaa_features_url}}Communication/{% endcapture %}
[communication]:  {{feature_communication_url}}
[1/KP structure]: {{feature_communication_url}}#kaa-protocol-v1-structure
[1/KP over MQTT]: {{feature_communication_url}}#mqtt-transport-binding-for-1kp
[1/KP over HTTP]: {{feature_communication_url}}#http-transport-binding-for-1kp
{% capture 1kp_over_http_postman_collection %}https://github.com/kaaproject/kaa/raw/master/doc/Features/Communication/1kp-http-binding.zip{% endcapture %}

{% capture kpc_url %}{{feature_communication_url}}KPC/{% endcapture %}
[KPC]: {{kpc_url}}
[KPC HTTP]: {{kpc_url}}Configuration/#http-transport
[KPC MQTT]: {{kpc_url}}Configuration/#mqtt-transport

{% capture epl_url %}{{feature_communication_url}}EPL/{% endcapture %}
[EPL]: {{epl_url}}

{% capture ccm_url %}{{feature_communication_url}}CCM/{% endcapture %}
[CCM]: {{ccm_url}}
[CCM REST API]: {{ccm_url}}REST-API/

{% capture rsx_url %}{{feature_communication_url}}RSX/{% endcapture %}
[RSX]: {{rsx_url}}

<!-- Data collection -->
{% capture feature_data_collection_url %}{{kaa_features_url}}Data-collection/{% endcapture %}
[data collection]: {{feature_data_collection_url}}
[batching]: {{feature_data_collection_url}}#batching

{% capture dcx_url %}{{feature_data_collection_url}}DCX/{% endcapture %}
[DCX]: {{dcx_url}}
[DCX metadata enrichment]:        {{dcx_url}}#enriching-data-samples-with-endpoint-metadata
[DCX metadata enrichment config]: {{dcx_url}}Configuration/#endpoint-metadata-enrichment
[DCX plain data handling]:        {{dcx_url}}#plain-data-handling

{% capture bcx_url %}{{feature_data_collection_url}}BCX/{% endcapture %}
[BCX]:                                      {{bcx_url}}
[BCX token exchange extension interface]:   {{bcx_url}}#token-exchange-extension-interface
[BCX REST API]:                             {{bcx_url}}REST-API/
[BCX binary data blob upload REST API]:     {{bcx_url}}REST-API/#binary_data_post
[BCX data blob management REST API]:        {{bcx_url}}REST-API/#applications__appname_
[BCX data blob last REST API]:              {{bcx_url}}REST-API/#applications__appname__endpoints__endpointid__binary_data_data_last_get
[BCX data blob delete REST API]:            {{bcx_url}}REST-API/#applications__appname__endpoints__endpointid__binary_data__blobid__delete

{% capture epts_url %}{{feature_data_collection_url}}EPTS/{% endcapture %}
[EPTS]:                                             {{epts_url}}
[EPTS REST API]:                                    {{epts_url}}REST-API/
[EPTS time series PUT REST API]:                    {{epts_url}}REST-API/#applications__applicationname__time_series_data_put
[EPTS time series PUT via app version REST API]:    {{epts_url}}REST-API/#app_versions__appversionname__time_series_data_put
[EPTS time series last REST API]:                   {{epts_url}}REST-API/#applications__applicationname__time_series_last_get
[EPTS time series data REST API]:                   {{epts_url}}REST-API/#applications__applicationname__time_series_data_get
[EPTS time series extraction]:                      {{epts_url}}Configuration/#time-series-extraction
[EPTS time series auto extraction]:                 {{epts_url}}Configuration/#time-series-auto-extraction
[EPTS time series configuration]:                   {{epts_url}}Configuration/#time-series-configuration

{% capture kdca_url %}{{feature_data_collection_url}}KDCA/{% endcapture %}
[KDCA]: {{kdca_url}}

{% capture mdca_url %}{{feature_data_collection_url}}MDCA/{% endcapture %}
[MDCA]: {{mdca_url}}

<!-- Data analytics -->
{% capture feature_data_analytics_url %}{{kaa_features_url}}Data-analytics/{% endcapture %}
[data analytics]: {{feature_data_analytics_url}}

{% capture asf_url %}{{feature_data_analytics_url}}ASF/{% endcapture %}
[ASF]: {{asf_url}}

<!-- Configuration management -->
{% capture feature_config_mgmt_url %}{{kaa_features_url}}Configuration-management/{% endcapture %}
[configuration]: {{feature_config_mgmt_url}}

{% capture cmx_url %}{{feature_config_mgmt_url}}CMX/{% endcapture %}
[CMX]: {{cmx_url}}

{% capture ecr_url %}{{feature_config_mgmt_url}}ECR/{% endcapture %}
[ECR]: {{ecr_url}}
[ECR REST API]:                         {{ecr_url}}REST-API/
[ECR REST API GET default config]:      {{ecr_url}}REST-API/#app_versions__appversionname__get
[ECR REST API GET per-endpoint config]: {{ecr_url}}REST-API/#endpoints__endpointid__app_versions__appversionname__get

{% capture adx_url %}{{feature_config_mgmt_url}}ADX/{% endcapture %}
[ADX]: {{adx_url}}


<!-- Commands -->
{% capture feature_commands_url %}{{kaa_features_url}}Command-invocation/{% endcapture %}
[commands]: {{feature_commands_url}}

{% capture cex_url %}{{feature_commands_url}}CEX/{% endcapture %}
[CEX]:                              {{cex_url}}
[CEX REST API]:                     {{cex_url}}REST-API/
[CEX REST API POST command]:        {{cex_url}}REST-API/#endpoints__endpointid__commands__commandtype__post
[CEX REST API DELETE command]:      {{cex_url}}REST-API/#endpoints__endpointid__commands__commandtype___commandid__delete
[CEX REST API POST batch command]:  {{cex_url}}REST-API/#endpoints_commands__commandtype__batch_post

[CEX batch command overview]: {{cex_url}}#batch-command-execution


<!-- Software updates -->
{% capture feature_ota_url %}{{kaa_features_url}}Software-updates/{% endcapture %}
[ota]: {{feature_ota_url}}

{% capture otao_url %}{{feature_ota_url}}OTAO/{% endcapture %}
[OTAO]: {{otao_url}}
[OTAO REST API]: {{otao_url}}REST-API/


<!-- Visualization -->
{% capture feature_visualization_url %}{{kaa_features_url}}Visualization/{% endcapture %}
[visualization]: {{feature_visualization_url}}

{% capture wd_url %}{{feature_visualization_url}}WD/{% endcapture %}
[WD]: {{wd_url}}
[WD Branding]: {{wd_url}}/Branding-customization
[WD File management]: {{wd_url}}/File-management


<!-- Infrastructure -->
{% capture feature_infrastructure_url %}{{kaa_features_url}}Infrastructure/{% endcapture %}
[infrastructure]: {{feature_infrastructure_url}}

{% capture pbm_url %}{{feature_infrastructure_url}}PBM/{% endcapture %}
[PBM]: {{pbm_url}}

{% capture tekton_url %}{{feature_infrastructure_url}}TEKTON/{% endcapture %}
[TEKTON]:                               {{tekton_url}}
[TEKTON REST API]:                      {{tekton_url}}REST-API/
[TEKTON applications REST API]:         {{tekton_url}}REST-API/#applications
[TEKTON application create REST API]:   {{tekton_url}}REST-API/#applications_post
[TEKTON app version create REST API]:   {{tekton_url}}REST-API/#applications__appname__app_versions_post
[TEKTON bulk config load REST API]:     {{tekton_url}}REST-API/#app_configs_post
[TEKTON tenant configs REST API]:       {{tekton_url}}REST-API/#tenant_configs
[TEKTON bulk REST API]:                 {{tekton_url}}REST-API/#bulk

{% capture tsa_url %}{{feature_infrastructure_url}}TSA/{% endcapture %}
[TSA]: {{tsa_url}}


<!-- Multi-tenancy -->
{% capture feature_multi_tenancy_url %}{{kaa_features_url}}Multi-tenancy/{% endcapture %}
[multi-tenancy]: {{feature_multi_tenancy_url}}

{% capture iam_url %}{{feature_multi_tenancy_url}}IAM/{% endcapture %}
[IAM]:          {{iam_url}}
[IAM REST API]: {{iam_url}}REST-API/


{% capture tenant_manager_url %}{{feature_multi_tenancy_url}}TENANT-MANAGER/{% endcapture %}
[TM]:                                   {{tenant_manager_url}}
[TM REST API]:                          {{tenant_manager_url}}REST-API/
[TM tenant]:                            {{tenant_manager_url}}#tenant
[TM tenant user]:                       {{tenant_manager_url}}#tenant-user
[TM realm template]:                    {{tenant_manager_url}}#realm-template
[TM backend client template]:           {{tenant_manager_url}}#realm-backend-client-template
[TM frontend client template]:          {{tenant_manager_url}}#realm-frontend-client-template
[TM scope]:                             {{tenant_manager_url}}#scope
[TM scope version]:                     {{tenant_manager_url}}#scope-version
[TM scope version mapping]:             {{tenant_manager_url}}#scope-version-mapping
[TM role]:                              {{tenant_manager_url}}#role
[TM role version]:                      {{tenant_manager_url}}#role-version
[TM role scope mapping]:                {{tenant_manager_url}}#role-scope-mapping
[TM role version mapping]:              {{tenant_manager_url}}#role-version-mapping
[TM default resource]:                  {{tenant_manager_url}}#default-resource
[TM default resource version]:          {{tenant_manager_url}}#default-resource-version
[TM default resource scope mapping]:    {{tenant_manager_url}}#default-resource-scope-mapping
[TM default resource version mapping]:  {{tenant_manager_url}}#default-resource-version-mapping
[TM idp]:                               {{tenant_manager_url}}#idp
[TM keycloak server]:                   {{tenant_manager_url}}#keycloak-server
[TM tenant subscription]:               {{tenant_manager_url}}#tenant-subscription
[TM package type]:                      {{tenant_manager_url}}#package-type


<!-- Miscellaneous -->
{% capture feature_miscellaneous_url %}{{kaa_features_url}}Miscellaneous/{% endcapture %}
[misc]: {{feature_miscellaneous_url}}

{% capture tsx_url %}{{feature_miscellaneous_url}}TSX/{% endcapture %}
[TSX]: {{tsx_url}}

<!-- Integration -->
{% capture feature_integration_url %}{{kaa_features_url}}Integration/{% endcapture %}
[integration]: {{feature_integration_url}}

{% capture ttnc_url %}{{feature_integration_url}}TTNC/{% endcapture %}
[TTNC]: {{ttnc_url}}


<!--== Kaa RFCs ==-->
[RFCs]:     {{rfc_url}}#kaa-rfcs
[1/KP]:     {{rfc_url}}blob/master/0001/README.md
[2/DCP]:    {{rfc_url}}blob/master/0002/README.md
[3/ISM]:    {{rfc_url}}blob/master/0003/README.md
[4/ESP]:    {{rfc_url}}blob/master/0004/README.md
[6/CDTP]:   {{rfc_url}}blob/master/0006/README.md
[7/CMP]:    {{rfc_url}}blob/master/0007/README.md
[8/KPSR]:   {{rfc_url}}blob/master/0008/README.md
[9/ELCE]:   {{rfc_url}}blob/master/0009/README.md
[10/EPMP]:  {{rfc_url}}blob/master/0010/README.md
[11/CEP]:   {{rfc_url}}blob/master/0011/README.md
[12/CIP]:   {{rfc_url}}blob/master/0012/README.md
[13/DSTP]:  {{rfc_url}}blob/master/0013/README.md
[14/TSTP]:  {{rfc_url}}blob/master/0014/README.md
[15/EME]:   {{rfc_url}}blob/master/0015/README.md
[16/ECAP]:  {{rfc_url}}blob/master/0016/README.md
[17/SCMP]:  {{rfc_url}}blob/master/0017/README.md
[18/EFE]:   {{rfc_url}}blob/master/0018/README.md
[19/EPMMP]: {{rfc_url}}blob/master/0019/README.md
[20/EFMP]:  {{rfc_url}}blob/master/0020/README.md
[21/TLE]:   {{rfc_url}}blob/master/0021/README.md
[22/CAP]:   {{rfc_url}}blob/master/0022/README.md


<!--== Kaa terminology ==-->
[architecture overview]:    {{root_url}}Architecture-overview/
[scalability]:              {{root_url}}Architecture-overview/#scalability
[service configuration]:    {{root_url}}Architecture-overview/#configuration

{% capture kaa_concepts %}{{root_url}}Kaa-concepts/{% endcapture %}
[kaa concepts]:         {{kaa_concepts}}
[endpoint]:             {{kaa_concepts}}#endpoints
[endpoint-id]:          {{kaa_concepts}}#endpoint-id
[endpoint-token]:       {{kaa_concepts}}#endpoint-token
[endpoint-metadata]:    {{kaa_concepts}}#endpoint-metadata
[data-sample]:          {{kaa_concepts}}#data-sample
[endpoint-filter]:      {{epr_url}}Key-service-features/Ep-filters/
[client]:               {{kaa_concepts}}#kaa-client
[application]:          {{kaa_concepts}}#applications-and-application-versions
[service]:              {{kaa_concepts}}#kaa-services
[solution]:             {{kaa_concepts}}#solutions
[blueprint]:            {{kaa_concepts}}#blueprint
[extension]:            {{kaa_concepts}}#extension-services
[dashboard]:            {{wd_url}}Dashboards/#dashboards
[widget]:               {{wd_url}}/Widgets/
[tenant]:               {{kaa_concepts}}#tenant
[policy]:               {{iam_url}}#policy

<!--== Tutorials ==-->
{% capture tutorials_url %}{{root_url}}Tutorials/{% endcapture %}
[tutorials]:                              {{tutorials_url}}

<!--== Getting Started ==-->
[getting started tutorials]:                             {{tutorials_url}}getting-started/
{% capture connecting_your_first_device %}               {{tutorials_url}}getting-started/connecting-your-first-device/{% endcapture %}
[connecting your first device]:                          {{connecting_your_first_device}}
{% capture collecting_data_from_a_device %}              {{tutorials_url}}getting-started/collecting-data-from-a-device/{% endcapture %}
[collecting data from a device]:                         {{collecting_data_from_a_device}}
{% capture custom_web_dashboard %}                       {{tutorials_url}}getting-started/build-iot-dashboard/{% endcapture %}
[custom web dashboard]:                                  {{custom_web_dashboard}}
{% capture sending_commands_to_device %}                 {{tutorials_url}}getting-started/sending-commands-to-device/{% endcapture %}
[sending commands to device]:                            {{sending_commands_to_device}}
{% capture open_distro_alerting %}                       {{tutorials_url}}getting-started/open-distro-alerting/{% endcapture %}
[open distro alerting tutorial]:                         {{open_distro_alerting}}
{% capture email_alerting %}                             {{tutorials_url}}getting-started/email-alerting/{% endcapture %}
[email alerting tutorial]:                               {{email_alerting}}
{% capture authenticating_client_with_tls_certificate %} {{tutorials_url}}getting-started/authenticating-client-with-tls-certificate/{% endcapture %}
[authenticating client with tls certificate]:            {{authenticating_client_with_tls_certificate}}
{% capture user_management %}                            {{tutorials_url}}getting-started/user-management/{% endcapture %}
[user management]:                                       {{user_management}}
{% capture custom_web_dashboard %}                       {{tutorials_url}}getting-started/build-iot-dashboard/{% endcapture %}
[custom web dashboard tutorial]:                         {{custom_web_dashboard}}
{% capture custom_widget_tutorial %}                     {{tutorials_url}}getting-started/custom-widget/{% endcapture %}
[custom widget tutorial]:                                {{custom_widget_tutorial}}
{% capture public_dashboard_tutorial %}                  {{tutorials_url}}getting-started/public-dashboard/{% endcapture %}
[public dashboard tutorial]:                             {{public_dashboard_tutorial}}
{% capture esp32_ota_update_tutorial %}                  {{tutorials_url}}device-integration/hardware-guides/esp32-ota-updates/{% endcapture %}
[esp32 ota update]:                                      {{esp32_ota_update_tutorial}}

[iot notification tutorial]:              {{tutorials_url}}getting-started/iot-notification/
[kaa cloud getting started]:              {{tutorials_url}}getting-started/getting-started-kaa-cloud/

<!--== Device Integration ==-->
[device integration tutorials]:           {{tutorials_url}}device-integration/
[connect Arduino (MKR-1010 + MKR-ENV)]:   {{tutorials_url}}device-integration/hardware-guides/connect-arduino-mkr-1010-to-kaa-platform/
[connect BlitzWolf smart socket]:         {{tutorials_url}}device-integration/hardware-guides/connect-blitzwolf-smart-socket/
[how to connect an ESP8266]:              {{tutorials_url}}device-integration/hardware-guides/connect-esp8266-to-kaa-platform/
[how to connect an STM32]:                {{tutorials_url}}device-integration/hardware-guides/connect-stm32-to-kaa-platform/
[how to connect a Raspberry Pi]:          {{tutorials_url}}device-integration/hardware-guides/connect-raspberry-to-kaa-platform/
[connecting node-red to kaa]:             {{tutorials_url}}device-integration/connecting-node-red-to-kaa/

<!--== Solutions ==-->
[solutions tutorials]:                                    {{tutorials_url}}solutions/
{% capture air_quality_monitoring_tutorial %}             {{tutorials_url}}solutions/air-quality-monitoring/{% endcapture %}
[air quality monitoring]:                                 {{air_quality_monitoring_tutorial}}

<!-- Administration -->
{% capture administration_url %}{{root_url}}Administration/{% endcapture %}
[administration]:               {{administration_url}}
[local installation]:           {{administration_url}}Local-installation/
[k8s installation]:             {{administration_url}}Installation-to-kubernetes-cluster/
[api security]:                 {{administration_url}}API-security/
[endpoint resource type]:       {{administration_url}}API-security/#endpoint-resource-type
[application resource type]:    {{administration_url}}API-security/#application-resource-type
[dashboard resource type]:      {{administration_url}}API-security/#dashboard-resource-type
[kaa resource type]:            {{administration_url}}API-security/#kaa-resource-type
[platform backup]:              {{administration_url}}platform-backup

<!-- Webinars -->
[webinars]: {{root_url}}Webinars/
[webinar Kaa IoT Cloud and Kaa 1.1]:        {{root_url}}Webinars/2019-12-11-Kaa-IoT-Cloud-and-Kaa-1.1/
[webinar Data Analytics and Notifications]: {{root_url}}Webinars/2020-04-02-Data-Analytics-and-Notifications/
[webinar kaa 1.2 release]: {{root_url}}Webinars/2020-08-11-Kaa-1.2/
[webinar kaa 1.3 release]: {{root_url}}Webinars/2021-04-07-Kaa-1.3/

<!-- What's new -->
{% capture whats_new_url %}{{root_url}}Whats-new/{% endcapture %}
[whats new]:        {{whats_new_url}}
[whats new in 1.1]: {{whats_new_url}}#kaa-11-november-8-th-2019
[whats new in 1.2]: {{whats_new_url}}#kaa-12-july-6-th-2020
[whats new in 1.3]: {{whats_new_url}}#kaa-13-february-17-th-2021

<!--== 3-rd party components ==-->
[docker]: https://www.docker.com/
[k8s]: https://kubernetes.io/
[k8s image pull secret]: https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod
[k8s docs home]: https://kubernetes.io/docs/home/
[k8s config map]: https://kubernetes.io/docs/tasks/configure-pod-container/configure-pod-configmap/
[helm]: https://helm.sh/
[helm3]: https://helm.sh/blog/helm-3-released/
[helm3-changes]: https://helm.sh/docs/faq/#changes-since-helm-2
[helm client]: https://helm.sh/docs/using_helm/#installing-the-helm-client
[Helmfile]: https://github.com/roboll/helmfile
[install helm]: https://helm.sh/docs/intro/install/
[prometheus]: https://prometheus.io/
[nginx]: https://www.nginx.com/
[fluentd]: https://www.fluentd.org/
[grafana]: https://grafana.com/
[nats]: https://www.nats.io/
[keycloak]: https://www.keycloak.org/
[influxdb]: https://docs.influxdata.com/influxdb/
[mongo]: https://www.mongodb.com/what-is-mongodb
[maria]: https://mariadb.org/
[redis]: https://redis.io
[postgresql]: https://www.postgresql.org/
[kibana]: https://www.elastic.co/kibana
[vault]: https://www.vaultproject.io/
[echarts]: https://echarts.apache.org/en/index.html
[open distro]: https://opendistro.github.io/
[open distro security]: https://opendistro.github.io/for-elasticsearch-docs/docs/security/
[open distro kibana]: https://opendistro.github.io/for-elasticsearch-docs/docs/kibana/
[open distro alerting]: https://opendistro.github.io/for-elasticsearch-docs/docs/alerting/
[curl]: https://en.wikipedia.org/wiki/CURL
[minio]: https://min.io/
[elastic stack]: https://www.elastic.co/elastic-stack
[elasticsearch]: https://www.elastic.co/elasticsearch/
[elastic mapping]: https://www.elastic.co/guide/en/elasticsearch/reference/7.17/mapping.html
[elastic pipeline processors]: https://www.elastic.co/guide/en/elasticsearch/reference/7.17/processors.html
[elastic script processor]: https://www.elastic.co/guide/en/elasticsearch/reference/7.17/script-processor.html
[elastic index template]: https://www.elastic.co/guide/en/elasticsearch/reference/7.17/index-templates.html
[elastic ingest pipeline]: https://www.elastic.co/guide/en/elasticsearch/reference/7.17/ingest.html

<!--== Technologies ==-->
[mqtt]: http://mqtt.org/
[coap]: http://coap.technology/
[json]: https://www.json.org/
[uuid]: https://en.wikipedia.org/wiki/Universally_unique_identifier
[avro]: https://avro.apache.org/
[oauth2]: https://tools.ietf.org/html/rfc6749
[resource server]: https://www.oauth.com/oauth2-servers/the-resource-server/
[access token]: https://www.oauth.com/oauth2-servers/access-tokens/
[oauth scope]: https://www.oauth.com/oauth2-servers/scope/
[openid]: https://openid.net/connect/
[uma]: https://en.wikipedia.org/wiki/User-Managed_Access
[python download]: https://www.python.org/downloads/
[gjson]: https://github.com/tidwall/gjson

<!--== General ==-->
[42]: https://en.wikipedia.org/wiki/42_(number)#The_Hitchhiker's_Guide_to_the_Galaxy
[digital twin]: https://en.wikipedia.org/wiki/Digital_twin
[over-the-air]: https://en.wikipedia.org/wiki/Over-the-air_programming
[openid]: https://openid.net/connect/

<!--== KaaIoT sites ==-->
[Kaa cloud]:              https://cloud.kaaiot.com
[Kaa cloud registration]: https://www.kaaiot.com/free-trial
[Kaa user chat]:          https://gitter.im/KaaIoT/community
[kaaproject.org]:         https://www.kaaiot.com
{% capture kaa_museum %}https://museum.kaaiot.net/{% endcapture %}
[kaa museum]:             {{kaa_museum}}

<!--== Replit.com ==-->
[connecting your first device repl]:                        https://replit.com/@KaaIoT/ConnectingYourFirstDevice120
[collecting data from a device repl]:                       https://replit.com/@KaaIoT/CollectingDataFromADevice130
[sending commands to the device repl]:                      https://replit.com/@KaaIoT/Getting-Started130SendingCommandsTODevice130
[one way authenticating client with tls certificate repl]:  https://replit.com/@KaaIoT/OneWayAuthenticatingClientWithTlsCertificate120
[two way authenticating client with tls certificate repl]:  https://replit.com/@KaaIoT/TwoWayAuthenticatingClientWithTlsCertificate120

<!--== Hardware ==-->
[MKR-1010]:       https://www.arduino.cc/en/Guide/MKRWiFi1010/
[MKR-ENV-Shield]: https://www.arduino.cc/en/Guide/MKRENVShield/
