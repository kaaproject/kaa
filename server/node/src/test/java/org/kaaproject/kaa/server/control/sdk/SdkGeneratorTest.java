/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.control.sdk;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.server.control.service.sdk.SdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.SdkGeneratorFactory;

/**
 * The Class SdkGeneratorTest.
 */
public class SdkGeneratorTest {

    private ApplicationEventMapDto makeDto(String fqn, ApplicationEventAction action) {
        ApplicationEventMapDto dto = new ApplicationEventMapDto();
        dto.setFqn(fqn);
        dto.setAction(action);
        return dto;
    }

    /**
     * Test create sdk generator.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("static-access")
    @Test
    public void testCreateSdkGenerator() throws Exception {
        SdkGeneratorFactory factory = new SdkGeneratorFactory();
        SdkGenerator generator = factory.createSdkGenerator(SdkPlatform.JAVA);
        Assert.assertNotNull(generator);
        generator = factory.createSdkGenerator(SdkPlatform.CPP);
        Assert.assertNotNull(generator);
        generator = factory.createSdkGenerator(SdkPlatform.C);
        Assert.assertNotNull(generator);
        generator = factory.createSdkGenerator(SdkPlatform.OBJC);
        Assert.assertNotNull(generator);
//
//        String profileSchema = "{\"type\":\"record\",\"name\":\"SuperProfile\",\"namespace\":\"org.kaaproject.kaa.common.endpoint.gen.test\",\"fields\":[{\"name\":\"profileBody\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}";
//
//        EventFamilyMetadata efm1 = Mockito.mock(EventFamilyMetadata.class);
//        Mockito.when(efm1.getEcfName()).thenReturn("1ecf");
//        Mockito.when(efm1.getVersion()).thenReturn(3);
//
//        EventFamilyMetadata efm2 = Mockito.mock(EventFamilyMetadata.class);
//        Mockito.when(efm2.getEcfName()).thenReturn("2ecf");
//        Mockito.when(efm2.getVersion()).thenReturn(2);
//
//        List<EventFamilyMetadata> eList = Arrays.asList(efm1, efm2);
//
//        ConnectionInfo ci = new Mockito().mock(ConnectionInfo.class);
//        Mockito.when(ci.getPublicKey()).thenReturn(ByteBuffer.wrap(new byte[] {1, 2, 3}));
//
//        IpComunicationParameters icp1 = Mockito.mock(IpComunicationParameters.class);
//        Mockito.when(icp1.getHostName()).thenReturn("http.server");
//        Mockito.when(icp1.getPort()).thenReturn(80);
//
//        IpComunicationParameters icp2 = Mockito.mock(IpComunicationParameters.class);
//        Mockito.when(icp2.getHostName()).thenReturn("kaatcp.server");
//        Mockito.when(icp2.getPort()).thenReturn(999);
//
//        ZkHttpComunicationParameters hcp = Mockito.mock(ZkHttpComunicationParameters.class);
//        Mockito.when(hcp.getZkComunicationParameters()).thenReturn(icp1);
//
//        ZkKaaTcpComunicationParameters kcp = Mockito.mock(ZkKaaTcpComunicationParameters.class);
//        Mockito.when(kcp.getZkComunicationParameters()).thenReturn(icp2);
//
//        ZkSupportedChannel sch1 = Mockito.mock(ZkSupportedChannel.class);
//        Mockito.when(sch1.getChannelType()).thenReturn(ZkChannelType.HTTP);
//        Mockito.when(sch1.getCommunicationParameters()).thenReturn(hcp);
//
//        ZkSupportedChannel sch2 = Mockito.mock(ZkSupportedChannel.class);
//        Mockito.when(sch2.getChannelType()).thenReturn(ZkChannelType.KAATCP);
//        Mockito.when(sch2.getCommunicationParameters()).thenReturn(kcp);
//
//        BootstrapSupportedChannel bsc1 = Mockito.mock(BootstrapSupportedChannel.class);
//        Mockito.when(bsc1.getZkChannel()).thenReturn(sch1);
//
//        BootstrapSupportedChannel bsc2 = Mockito.mock(BootstrapSupportedChannel.class);
//        Mockito.when(bsc2.getZkChannel()).thenReturn(sch2);
//
//        List<BootstrapSupportedChannel> supportedChannel = Arrays.asList(bsc1, bsc2);
//
//        BootstrapNodeInfo info1 = Mockito.mock(BootstrapNodeInfo.class);
//        Mockito.when(info1.getConnectionInfo()).thenReturn(ci);
//        Mockito.when(info1.getSupportedChannelsArray()).thenReturn(supportedChannel);
//
//        List<BootstrapNodeInfo> bList = Arrays.asList(info1, info1);
//
//        List<EventFamilyMetadata> eventFamilies = new ArrayList<EventFamilyMetadata>();
//        EventFamilyMetadata eventF1 = new EventFamilyMetadata();
//        eventF1.setEcfClassName("PlayerClassFamily");
//        eventF1.setEcfSchema("[{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"DeviceInfoRequest\",\"fields\":[]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"object\",\"name\":\"DeviceInfo\",\"fields\":[{\"name\":\"model\",\"type\":[\"string\",\"null\"]},{\"name\":\"product\",\"type\":[\"string\",\"null\"]},{\"name\":\"manufacturer\",\"type\":[\"string\",\"null\"]},{\"name\":\"device\",\"type\":[\"string\",\"null\"]},{\"name\":\"brand\",\"type\":[\"string\",\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"DeviceInfoResponse\",\"fields\":[    {\"name\":\"deviceInfo\",\"type\":[\"org.kaaproject.kaa.demo.player.DeviceInfo\",\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"PlayListRequest\",\"fields\":[]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"object\",\"name\":\"SongInfo\",\"fields\":[{\"name\":\"title\",\"type\":[\"string\",\"null\"]},{\"name\":\"artist\",\"type\":[\"string\",\"null\"]},{\"name\":\"url\",\"type\":[\"string\",\"null\"]},{\"name\":\"displayName\",\"type\":[\"string\",\"null\"]},{\"name\":\"duration\",\"type\":[\"int\",\"null\"]},{\"name\":\"album\",\"type\":[\"string\",\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"PlayListResponse\",\"fields\":[    {\"name\":\"playList\",\"type\":[{\"type\":\"array\",\"items\":\"org.kaaproject.kaa.demo.player.SongInfo\"},\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"PlayRequest\",\"fields\":[{\"name\":\"url\",\"type\":[\"string\",\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"PauseRequest\",\"fields\":[]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"StopRequest\",\"fields\":[]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"PlaybackInfoRequest\",\"fields\":[]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"enum\",\"classType\":\"object\",\"name\":\"PlaybackStatus\",\"symbols\":[\"PLAYING\",\"PAUSED\",\"STOPPED\"]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"object\",\"name\":\"PlaybackInfo\",\"fields\":[{\"name\":\"url\",\"type\":[\"string\",\"null\"]},{\"name\":\"time\",\"type\":[\"int\",\"null\"]},{\"name\":\"status\",\"type\":[\"org.kaaproject.kaa.demo.player.PlaybackStatus\",\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"PlaybackInfoResponse\",\"fields\":[ {\"name\":\"playbackInfo\",\"type\":[\"org.kaaproject.kaa.demo.player.PlaybackInfo\",\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"BatteryInfoRequest\",\"fields\":[]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"enum\",\"classType\":\"object\",\"name\":\"BatteryChargingStatus\",\"symbols\":[\"NOT_CHARGING\",\"CHARGING\",\"CHARGED\"]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"object\",\"name\":\"BatteryInfo\",\"fields\":[{\"name\":\"batteryLife\",\"type\":[\"int\",\"null\"]},{\"name\":\"chargingStatus\",\"type\":[\"org.kaaproject.kaa.demo.player.BatteryChargingStatus\",\"null\"]}]},{\"namespace\":\"org.kaaproject.kaa.demo.player\",\"type\":\"record\",\"classType\":\"event\",\"name\":\"BatteryInfoResponse\",\"fields\":[{\"name\":\"batteryInfo\",\"type\":[\"org.kaaproject.kaa.demo.player.BatteryInfo\",\"null\"]}]}]");
//        eventF1.setEcfName("Player Event Class Family");
//        eventF1.setEcfNamespace("org.kaaproject.kaa.demo.player");
//
//        List<ApplicationEventMapDto> appMap = new ArrayList<>();
//
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.DeviceInfoRequest", ApplicationEventAction.SINK));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.DeviceInfoResponse", ApplicationEventAction.SOURCE));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.PlayListRequest", ApplicationEventAction.SINK));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.PlayListResponse", ApplicationEventAction.SOURCE));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.PlayRequest", ApplicationEventAction.SINK));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.PauseRequest", ApplicationEventAction.SINK));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.StopRequest", ApplicationEventAction.SINK));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.PlaybackInfoRequest", ApplicationEventAction.SINK));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.PlaybackInfoResponse", ApplicationEventAction.SOURCE));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.BatteryInfoRequest", ApplicationEventAction.SINK));
//        appMap.add(makeDto("org.kaaproject.kaa.demo.player.BatteryInfoResponse", ApplicationEventAction.SOURCE));
//
//        eventF1.setEventMaps(appMap);
//        eventFamilies.add(eventF1);
//
//        Sdk sdk = generator.generateSdk("test", bList, "token", 7, 6, 5, 4, profileSchema, null, null, null, eventFamilies, null);
//
//
//        String prefixHome = "/home/dyosick/";
//
//        FileOutputStream fos = new FileOutputStream(prefixHome + "sdk.tar.gz");
//
//        fos.write(sdk.getData());
//
//        fos.close();
    }
}
