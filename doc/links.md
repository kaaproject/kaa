{% include variables.md %}


<!--== Features and components ==-->
{% capture kaa_features_url %}{{root_url}}Features/{% endcapture %}
[kaa features]: {{kaa_features_url}}


<!-- Device management -->
{% capture feature_device_mgmt_url %}{{kaa_features_url}}Device-management/{% endcapture %}
[identity]: {{feature_device_mgmt_url}}
[device management]: {{feature_device_mgmt_url}}

{% capture epr_url %}{{feature_device_mgmt_url}}EPR/{% endcapture %}
[EPR]: {{epr_url}}
[EPR REST API]: {{epr_url}}REST-API/

{% capture epmx_url %}{{feature_device_mgmt_url}}EPMX/{% endcapture %}
[EPMX]: {{epmx_url}}

{% capture cm_url %}{{feature_device_mgmt_url}}CM/{% endcapture %}
[CM]: {{cm_url}}
[CM REST API]: {{cm_url}}REST-API/


<!-- Communication -->
{% capture feature_communication_url %}{{kaa_features_url}}Communication/{% endcapture %}
[communication]: {{feature_communication_url}}
[1/KP over MQTT topic structure]: {{feature_communication_url}}#mqtt-topic-structure-for-the-kaa-protocol-v1

{% capture kpc_url %}{{feature_communication_url}}KPC/{% endcapture %}
[KPC]: {{kpc_url}}

{% capture epl_url %}{{feature_communication_url}}EPL/{% endcapture %}
[EPL]: {{epl_url}}


<!-- Data collection -->
{% capture feature_data_collection_url %}{{kaa_features_url}}Data-collection/{% endcapture %}
[data collection]: {{feature_data_collection_url}}

{% capture dcx_url %}{{feature_data_collection_url}}DCX/{% endcapture %}
[DCX]: {{dcx_url}}

{% capture epts_url %}{{feature_data_collection_url}}EPTS/{% endcapture %}
[EPTS]: {{epts_url}}
[EPTS REST API]: {{epts_url}}REST-API/

{% capture kdca_url %}{{feature_data_collection_url}}KDCA/{% endcapture %}
[KDCA]: {{kdca_url}}

{% capture mdca_url %}{{feature_data_collection_url}}MDCA/{% endcapture %}
[MDCA]: {{mdca_url}}


<!-- Configuration management -->
{% capture feature_config_mgmt_url %}{{kaa_features_url}}Configuration-management/{% endcapture %}
[configuration]: {{feature_config_mgmt_url}}

{% capture cmx_url %}{{feature_config_mgmt_url}}CMX/{% endcapture %}
[CMX]: {{cmx_url}}

{% capture ecr_url %}{{feature_config_mgmt_url}}ECR/{% endcapture %}
[ECR]: {{ecr_url}}
[ECR REST API]: {{ecr_url}}REST-API/


<!-- Commands -->
{% capture feature_commands_url %}{{kaa_features_url}}Command-invocation/{% endcapture %}
[commands]: {{feature_commands_url}}

{% capture cex_url %}{{feature_commands_url}}CEX/{% endcapture %}
[CEX]: {{cex_url}}
[CEX REST API]: {{cex_url}}REST-API/

{% capture rci_url %}{{feature_commands_url}}RCI/{% endcapture %}
[RCI]: {{rci_url}}
[RCI REST API]: {{rci_url}}REST-API/


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
[WD WIDGETS]: {{wd_url}}/Widgets


<!-- Infrastructure -->
{% capture feature_infrastructure_url %}{{kaa_features_url}}Infrastructure/{% endcapture %}
[infrastructure]: {{feature_infrastructure_url}}

{% capture tekton_url %}{{feature_infrastructure_url}}TEKTON/{% endcapture %}
[TEKTON]: {{tekton_url}}
[TEKTON REST API]: {{tekton_url}}REST-API/


<!-- Miscellaneous -->
{% capture feature_miscellaneous_url %}{{kaa_features_url}}Miscellaneous/{% endcapture %}
[misc]: {{feature_miscellaneous_url}}

{% capture tsx_url %}{{feature_miscellaneous_url}}TSX/{% endcapture %}
[TSX]: {{tsx_url}}


<!--== Kaa RFCs ==-->
[RFCs]: {{rfc_url}}#kaa-rfcs
[1/KP]: {{rfc_url}}blob/master/0001/README.md
[2/DCP]: {{rfc_url}}blob/master/0002/README.md
[3/ISM]: {{rfc_url}}blob/master/0003/README.md
[4/ESP]: {{rfc_url}}blob/master/0004/README.md
[6/CDTP]: {{rfc_url}}blob/master/0006/README.md
[7/CMP]: {{rfc_url}}blob/master/0007/README.md
[8/KPSR]: {{rfc_url}}blob/master/0008/README.md
[9/ELCE]: {{rfc_url}}blob/master/0009/README.md
[10/EPMP]: {{rfc_url}}blob/master/0010/README.md
[11/CEP]: {{rfc_url}}blob/master/0011/README.md
[12/CIP]: {{rfc_url}}blob/master/0012/README.md
[13/DSTP]: {{rfc_url}}blob/master/0013/README.md
[14/TSTP]: {{rfc_url}}blob/master/0014/README.md
[15/EME]: {{rfc_url}}blob/master/0015/README.md
[16/ECAP]: {{rfc_url}}blob/master/0016/README.md
[17/SCMP]: {{rfc_url}}blob/master/0017/README.md
[18/EFE]: {{rfc_url}}blob/master/0018/README.md

<!--== Kaa terminology ==-->
[architecture overview]: {{root_url}}Architecture-overview/
[scalability]: {{root_url}}Architecture-overview/#scalability
[service configuration]:{{root_url}}Architecture-overview/#configuration

{% capture kaa_concepts %}{{root_url}}Kaa-concepts/{% endcapture %}
[kaa concepts]: {{kaa_concepts}}
[endpoint]: {{kaa_concepts}}#endpoints
[endpoint-id]: {{kaa_concepts}}#endpoint-id
[endpoint-token]: {{kaa_concepts}}#endpoint-token
[endpoint-metadata]: {{kaa_concepts}}#endpoint-metadata
[endpoint-filter]: {{epr_url}}Key-service-features/Ep-filters/
[client]: {{kaa_concepts}}#kaa-client
[application]: {{kaa_concepts}}#applications-and-application-versions
[service]: {{kaa_concepts}}#kaa-services
[solution]: {{kaa_concepts}}#solution-cluster
[blueprint]: {{kaa_concepts}}#blueprint
[extension]: {{kaa_concepts}}#extension-services

<!--== Tutorials ==-->
[tutorials]: {{root_url}}Tutorials/
[how to connect device]: {{root_url}}Tutorials/connect-device-to-iot-platform/
[data collection tutorial]: {{root_url}}Tutorials/iot-data-collection/
[custom web dashboard]: {{root_url}}Tutorials/build-iot-dashboard/
[custom microservice integration]: {{root_url}}Tutorials/custom-microservice/

<!--== 3-rd party components ==-->
[docker]: https://www.docker.com/
[k8s]: https://kubernetes.io/
[helm]: https://helm.sh/
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

<!--== Technologies ==-->
[mqtt]: http://mqtt.org/
[coap]: http://coap.technology/
[json]: https://www.json.org/
[avro]: https://avro.apache.org/
[oauth2]: https://oauth.net/2/

<!--== General ==-->
[digital twin]: https://en.wikipedia.org/wiki/Digital_twin
[over-the-air]: https://en.wikipedia.org/wiki/Over-the-air_programming
[access token]: https://www.oauth.com/oauth2-servers/access-tokens/
[openid]: https://openid.net/connect/
