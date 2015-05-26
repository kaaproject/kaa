package org.kaaproject.kaa.sandbox.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.SdkKey;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.verifiers.trustful.config.TrustfulVerifierConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IotWorldDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(IotWorldDemoBuilder.class);

    private static final String SMARTHOME_ANDROID_ID = "smarthome_android";
    private static final String SMARTHOME_IOS_ID = "smarthome_ios";
    private static final String CLIMATE_ANDROID_ID = "climate_android";
    private static final String FAN_CONTROL_C_ID = "fan_control_c";
    private static final String MUSICPLAYER_JAVA_ID = "musicplayer_java";
    private static final String PHOTOPLAYER_JAVA_ID = "photoplayer_java";
    private static final String LIGHT_CONTROL_C_ID = "light_control_c";
    private static final String IRRIGATION_SYSTEM_JAVA_ID = "irrigation_system_java";
    
    private static final String FAN_EVENT_CLASS_FAMILY = "Fan Event Class Family";
    private static final String IRRIGATION_EVENT_CLASS_FAMILY = "Irrigation Event Class Family";
    private static final String LIGHT_EVENT_CLASS_FAMILY = "Light Event Class Family";
    private static final String PHOTO_EVENT_CLASS_FAMILY = "Photo Event Class Family";
    private static final String MUSIC_EVENT_CLASS_FAMILY = "Music Event Class Family";
    private static final String THERMO_EVENT_CLASS_FAMILY = "Thermo Event Class Family";
    private static final String GEO_FENCING_EVENT_CLASS_FAMILY = "Geo Fencing Event Class Family";
    private static final String DEVICE_EVENT_CLASS_FAMILY = "Device Event Class Family";

    private static Map<String, ApplicationEventAction> defaultDeviceAefMap =
            new HashMap<>();
    static {
        defaultDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest", ApplicationEventAction.SINK);
        defaultDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse", ApplicationEventAction.SOURCE);
        defaultDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest", ApplicationEventAction.SINK);
        defaultDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest", ApplicationEventAction.SINK);
    }

    private static Map<String, ApplicationEventAction> defaultGeoFencingDeviceAefMap =
            new HashMap<>();
    static {
        defaultGeoFencingDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest", ApplicationEventAction.SINK);
        defaultGeoFencingDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusResponse", ApplicationEventAction.SOURCE);
        defaultGeoFencingDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest", ApplicationEventAction.SINK);
        defaultGeoFencingDeviceAefMap.put("org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate", ApplicationEventAction.SINK);
    }
        
    private Map<String, SdkKey> projectsSdkMap = new HashMap<>();

    protected IotWorldDemoBuilder() {
        super("demo/iotworld");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client)
            throws Exception {
        
        logger.info("Loading 'Iot World Demo' data...");
        
        loginTenantAdmin(client);
        
        Map<String, EventClassFamilyDto> ecfMap = new HashMap<>();
        
        ecfMap.put(DEVICE_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    DEVICE_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "DeviceEventClassFamily",
                                    "deviceEventClassFamily.json"));
        
        ecfMap.put(GEO_FENCING_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    GEO_FENCING_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "GeoFencingEventClassFamily",
                                    "geoFencingEventClassFamily.json"));

        ecfMap.put(THERMO_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    THERMO_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "ThermoEventClassFamily",
                                    "thermoEventClassFamily.json"));

        ecfMap.put(MUSIC_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    MUSIC_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "MusicEventClassFamily",
                                    "musicEventClassFamily.json"));

        ecfMap.put(PHOTO_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    PHOTO_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "PhotoEventClassFamily",
                                    "photoEventClassFamily.json"));

        ecfMap.put(LIGHT_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    LIGHT_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "LightEventClassFamily",
                                    "lightEventClassFamily.json"));

        ecfMap.put(IRRIGATION_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    IRRIGATION_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "IrrigationEventClassFamily",
                                    "irrigationEventClassFamily.json"));

        ecfMap.put(FAN_EVENT_CLASS_FAMILY,  
                addEventClassFamily(client,
                                    FAN_EVENT_CLASS_FAMILY, 
                                    "org.kaaproject.kaa.demo.iotworld",
                                    "FanEventClassFamily",
                                    "fanEventClassFamily.json"));
        
        ApplicationDto smartHomeApplication = new ApplicationDto();
        smartHomeApplication.setName("Smart Home");
        smartHomeApplication = client.editApplication(smartHomeApplication);

        ApplicationDto thermostatApplication = new ApplicationDto();
        thermostatApplication.setName("Climate Control");
        thermostatApplication = client.editApplication(thermostatApplication);

        ApplicationDto fanControlApplication = new ApplicationDto();
        fanControlApplication.setName("Fan Control");
        fanControlApplication = client.editApplication(fanControlApplication);

        ApplicationDto musicPlayerApplication = new ApplicationDto();
        musicPlayerApplication.setName("Music Player");
        musicPlayerApplication = client.editApplication(musicPlayerApplication);

        ApplicationDto photoPlayerApplication = new ApplicationDto();
        photoPlayerApplication.setName("Photo Player");
        photoPlayerApplication = client.editApplication(photoPlayerApplication);

        ApplicationDto lightControlApplication = new ApplicationDto();
        lightControlApplication.setName("Light Control");
        lightControlApplication = client.editApplication(lightControlApplication);

        ApplicationDto irrigationSystemApplication = new ApplicationDto();
        irrigationSystemApplication.setName("Irrigation System");
        irrigationSystemApplication = client.editApplication(irrigationSystemApplication);

        loginTenantDeveloper(client);
        
        configureSmartHomeApp(client, smartHomeApplication.getId(),  ecfMap);
        configureThermostatApp(client, thermostatApplication.getId(),  ecfMap);
        configureFanControlApp(client, fanControlApplication.getId(),  ecfMap);
        configureMusicPlayerApp(client, musicPlayerApplication.getId(),  ecfMap);
        configurePhotoPlayerApp(client, photoPlayerApplication.getId(),  ecfMap);
        configureLightControlApp(client, lightControlApplication.getId(),  ecfMap);
        configureIrrigationSystemApp(client, irrigationSystemApplication.getId(),  ecfMap);
        
        logger.info("Finished loading 'Iot World Demo' data.");
    }
    
    private void configureSmartHomeApp(AdminClient client, 
            String applicationId, 
            Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkKey sdkKey = createSdkKey(client, applicationId, true);
        
        List<String> aefMapIds = new ArrayList<>();
        
        Map<String, ApplicationEventAction> actionsMap
            = new HashMap<>();

        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest", ApplicationEventAction.SOURCE);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(DEVICE_EVENT_CLASS_FAMILY),
                actionsMap));        
        
        actionsMap = new HashMap<>();
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusResponse", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate", ApplicationEventAction.SOURCE);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(GEO_FENCING_EVENT_CLASS_FAMILY),
                actionsMap));        
        
        actionsMap = new HashMap<>();
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.thermo.ThermostatStatusUpdate", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.thermo.ChangeDegreeRequest", ApplicationEventAction.SOURCE);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(THERMO_EVENT_CLASS_FAMILY),
                actionsMap));        
        
        actionsMap = new HashMap<>();
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlayListRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlayListResponse", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlayRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PauseRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.StopRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.ChangeVolumeRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.SeekRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlaybackStatusUpdate", ApplicationEventAction.SINK);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(MUSIC_EVENT_CLASS_FAMILY),
                actionsMap));        
        
        actionsMap = new HashMap<>();
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsResponse", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.StartSlideShowRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PauseSlideShowRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.DeleteUploadedPhotosRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate", ApplicationEventAction.SINK);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(PHOTO_EVENT_CLASS_FAMILY),
                actionsMap));   
        
        actionsMap = new HashMap<>();
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.BulbListRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.BulbListStatusUpdate", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.ChangeBulbBrightnessRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.ChangeBulbStatusRequest", ApplicationEventAction.SOURCE);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(LIGHT_EVENT_CLASS_FAMILY),
                actionsMap));        

        actionsMap = new HashMap<>();
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationControlRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStatusUpdate", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.irrigation.StartIrrigationRequest", ApplicationEventAction.SOURCE);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(IRRIGATION_EVENT_CLASS_FAMILY),
                actionsMap));        

        sdkKey.setAefMapIds(aefMapIds);
        
        projectsSdkMap.put(SMARTHOME_ANDROID_ID, sdkKey);
        projectsSdkMap.put(SMARTHOME_IOS_ID, sdkKey);
    }
    
    private void configureThermostatApp(AdminClient client, 
            String applicationId, 
            Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkKey sdkKey = createSdkKey(client, applicationId, true);
        
        List<String> aefMapIds = new ArrayList<>();
        
        Map<String, ApplicationEventAction> actionsMap
                    = new HashMap<>();
        
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest", ApplicationEventAction.BOTH);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse", ApplicationEventAction.BOTH);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest", ApplicationEventAction.BOTH);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest", ApplicationEventAction.SINK);

        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(DEVICE_EVENT_CLASS_FAMILY),
                actionsMap));
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(GEO_FENCING_EVENT_CLASS_FAMILY),
                defaultGeoFencingDeviceAefMap));   
        
        actionsMap = new HashMap<>();
        
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.thermo.ThermostatStatusUpdate", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.thermo.ChangeDegreeRequest", ApplicationEventAction.SINK);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(THERMO_EVENT_CLASS_FAMILY),
                actionsMap));        
        
        actionsMap = new HashMap<>();
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.fan.SwitchRequest", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.fan.FanStatusUpdate", ApplicationEventAction.SINK);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(FAN_EVENT_CLASS_FAMILY),
                actionsMap));  
        
        sdkKey.setAefMapIds(aefMapIds);
        
        projectsSdkMap.put(CLIMATE_ANDROID_ID, sdkKey);
    }
    
    private void configureFanControlApp(AdminClient client, 
            String applicationId, 
            Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkKey sdkKey = createSdkKey(client, applicationId, true);
        
        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(DEVICE_EVENT_CLASS_FAMILY),
                defaultDeviceAefMap));
        
        Map<String, ApplicationEventAction> actionsMap
                    = new HashMap<>();
        
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.fan.SwitchRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.fan.FanStatusUpdate", ApplicationEventAction.SOURCE);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(FAN_EVENT_CLASS_FAMILY),
                actionsMap));  
        
        sdkKey.setAefMapIds(aefMapIds);
        
        projectsSdkMap.put(FAN_CONTROL_C_ID, sdkKey);
    }
    
    private void configureMusicPlayerApp(AdminClient client, 
            String applicationId, 
            Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkKey sdkKey = createSdkKey(client, applicationId, true);
        
        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(DEVICE_EVENT_CLASS_FAMILY),
                defaultDeviceAefMap));
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(GEO_FENCING_EVENT_CLASS_FAMILY),
                defaultGeoFencingDeviceAefMap));   
        
        Map<String, ApplicationEventAction> actionsMap
                    = new HashMap<>();
        
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlayListRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlayListResponse", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlayRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PauseRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.StopRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.ChangeVolumeRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.SeekRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.music.PlaybackStatusUpdate", ApplicationEventAction.SOURCE);

        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(MUSIC_EVENT_CLASS_FAMILY),
                actionsMap)); 
        
        sdkKey.setAefMapIds(aefMapIds);
        
        projectsSdkMap.put(MUSICPLAYER_JAVA_ID, sdkKey);
        
    }

    private void configurePhotoPlayerApp(AdminClient client, 
            String applicationId, 
            Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkKey sdkKey = createSdkKey(client, applicationId, true);
        
        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(DEVICE_EVENT_CLASS_FAMILY),
                defaultDeviceAefMap));
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(GEO_FENCING_EVENT_CLASS_FAMILY),
                defaultGeoFencingDeviceAefMap));   
        
        Map<String, ApplicationEventAction> actionsMap
                        = new HashMap<>();
        
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsResponse", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.StartSlideShowRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PauseSlideShowRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.DeleteUploadedPhotosRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate", ApplicationEventAction.SOURCE);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(PHOTO_EVENT_CLASS_FAMILY),
                actionsMap)); 
        
        sdkKey.setAefMapIds(aefMapIds);
        
        projectsSdkMap.put(PHOTOPLAYER_JAVA_ID, sdkKey);
        
    }
    
    private void configureLightControlApp(AdminClient client, 
            String applicationId, 
            Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkKey sdkKey = createSdkKey(client, applicationId, true);
        
        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(DEVICE_EVENT_CLASS_FAMILY),
                defaultDeviceAefMap));
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(GEO_FENCING_EVENT_CLASS_FAMILY),
                defaultGeoFencingDeviceAefMap));   
        
        Map<String, ApplicationEventAction> actionsMap
                    = new HashMap<>();
        
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.BulbListRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.BulbListStatusUpdate", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.ChangeBulbBrightnessRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.light.ChangeBulbStatusRequest", ApplicationEventAction.SINK);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(LIGHT_EVENT_CLASS_FAMILY),
                actionsMap));           
        
        sdkKey.setAefMapIds(aefMapIds);
        
        projectsSdkMap.put(LIGHT_CONTROL_C_ID, sdkKey);
    }
    
    private void configureIrrigationSystemApp(AdminClient client, 
            String applicationId, 
            Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkKey sdkKey = createSdkKey(client, applicationId, true);
        
        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(DEVICE_EVENT_CLASS_FAMILY),
                defaultDeviceAefMap));
        
        Map<String, ApplicationEventAction> actionsMap
                    = new HashMap<>();
        
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationControlRequest", ApplicationEventAction.SINK);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStatusUpdate", ApplicationEventAction.SOURCE);
        actionsMap.put("org.kaaproject.kaa.demo.iotworld.irrigation.StartIrrigationRequest", ApplicationEventAction.SINK);
        
        aefMapIds.add(createAefMap(client, 
                applicationId, 
                ecfMap.get(IRRIGATION_EVENT_CLASS_FAMILY),
                actionsMap));        
        
        sdkKey.setAefMapIds(aefMapIds);
        
        projectsSdkMap.put(IRRIGATION_SYSTEM_JAVA_ID, sdkKey);
    }

    
    private EventClassFamilyDto addEventClassFamily(AdminClient client, 
            String name, String namespace, String className, String resource) throws Exception {
        EventClassFamilyDto eventClassFamily = new EventClassFamilyDto();
        eventClassFamily.setName(name);
        eventClassFamily.setNamespace(namespace);
        eventClassFamily.setClassName(className);
        eventClassFamily = client.editEventClassFamily(eventClassFamily);
        client.addEventClassFamilySchema(eventClassFamily.getId(), getResourcePath(resource));
        return eventClassFamily;
    }
    
    private SdkKey createSdkKey(AdminClient client, 
            String applicationId, 
            boolean createVerifier) throws Exception {
        SdkKey sdkKey = new SdkKey();
        sdkKey.setApplicationId(applicationId);
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        if (createVerifier) {
            sdkKey.setDefaultVerifierToken(createTrustfulVerifier(client, applicationId));
        }
        return sdkKey;
    }
    
    private String createTrustfulVerifier(AdminClient client, String applicationId) throws Exception {
        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();        
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(applicationId);
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());
        RawSchema rawSchema = new RawSchema(trustfulVerifierConfig.getPluginConfigSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm = 
                    new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        trustfulUserVerifier.setJsonConfiguration(rawData.getRawData());        
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);      
        return trustfulUserVerifier.getVerifierToken();
    }
    
    private String createAefMap(AdminClient client, 
            String applicationId, 
            EventClassFamilyDto ecf,
            Map<String, ApplicationEventAction> actionsMap) throws Exception {
        List<EventClassDto> eventClasses = 
                client.getEventClassesByFamilyIdVersionAndType(ecf.getId(), 1, EventClassType.EVENT);

        ApplicationEventFamilyMapDto aefMap = new ApplicationEventFamilyMapDto();
        aefMap.setApplicationId(applicationId);
        aefMap.setEcfId(ecf.getId());
        aefMap.setEcfName(ecf.getName());
        aefMap.setVersion(1);
        
        List<ApplicationEventMapDto> eventMaps = new ArrayList<>(eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            ApplicationEventMapDto eventMap = new ApplicationEventMapDto();
            eventMap.setEventClassId(eventClass.getId());
            eventMap.setFqn(eventClass.getFqn());
                eventMap.setAction(actionsMap.get(eventClass.getFqn()));
            eventMaps.add(eventMap);
        }
        
        aefMap.setEventMaps(eventMaps);
        aefMap = client.editApplicationEventFamilyMap(aefMap);
        
        return aefMap.getId();
    }
    
    @Override
    protected boolean isMiltiApplcationProject() {
        return true;
    }
    
    @Override
    protected Map<String, SdkKey> getProjectsSdkMap() {
        return projectsSdkMap;
    }

}
