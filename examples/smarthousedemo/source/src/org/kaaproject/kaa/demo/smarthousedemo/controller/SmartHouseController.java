/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback;
import org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceEventClassFamily;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfo;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.smarthouse.music.ChangeVolumeRequest;
import org.kaaproject.kaa.demo.smarthouse.music.MusicEventClassFamily;
import org.kaaproject.kaa.demo.smarthouse.music.PauseRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlayListRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlayListResponse;
import org.kaaproject.kaa.demo.smarthouse.music.PlayRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfo;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfoRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfoResponse;
import org.kaaproject.kaa.demo.smarthouse.music.SeekRequest;
import org.kaaproject.kaa.demo.smarthouse.music.SongInfo;
import org.kaaproject.kaa.demo.smarthouse.music.StopRequest;
import org.kaaproject.kaa.demo.smarthouse.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermoEventClassFamily;
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermostatInfo;
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermostatInfoRequest;
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermostatInfoResponse;
import org.kaaproject.kaa.demo.smarthousedemo.command.AttachEndpointCommand;
import org.kaaproject.kaa.demo.smarthousedemo.command.AttachUserCommand;
import org.kaaproject.kaa.demo.smarthousedemo.command.CommandAsyncTask;
import org.kaaproject.kaa.demo.smarthousedemo.command.CommandCallback;
import org.kaaproject.kaa.demo.smarthousedemo.command.DetachEndpointCommand;
import org.kaaproject.kaa.demo.smarthousedemo.command.EndpointCommandKey;
import org.kaaproject.kaa.demo.smarthousedemo.command.device.GetDeviceInfoCommand;
import org.kaaproject.kaa.demo.smarthousedemo.command.music.GetPlayListCommand;
import org.kaaproject.kaa.demo.smarthousedemo.concurrent.BlockingCallable;
import org.kaaproject.kaa.demo.smarthousedemo.concurrent.TimeoutExecutor;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStore;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;

import android.content.Context;
import android.util.Log;

public class SmartHouseController implements DeviceEventClassFamily.Listener,
                                             ThermoEventClassFamily.Listener,
                                             MusicEventClassFamily.Listener,
                                             AttachEndpointToUserCallback, DetachEndpointFromUserCallback
{

    private static final String TAG = SmartHouseController.class.getSimpleName();
    
    /** Default timeout for commands in milliseconds */
    private static final long DEFAULT_TASK_TIMEOUT = 10000;
    
    /** Android application context */
    private Context context;
    
    private DeviceType deviceType;
    
    private String userAccount;
    
    private DeviceStore deviceStore;
        
    /** Reference to Kaa client */
    private KaaClient client;
    
    /** Reference to device event class family used to send events */
    private DeviceEventClassFamily devices;

    /** Reference to thermo event class family used to send events */
    private ThermoEventClassFamily thermostats;
    
    /** Reference to music event class family used to send events */
    private MusicEventClassFamily soundSystems;

    /** Flag indicating that Kaa client SDK was initialized */
    private boolean inited = false;
    
    /** Executor to concurrently process commands with specified timeout */
    private TimeoutExecutor executor;
    private final ExecutorService eventExecutor = Executors.newCachedThreadPool();

    /** Current device info. Prepared before Kaa client initialization. */ 
    private DeviceInfo deviceInfo;

    /** Map to store issued commands. Used to find corresponding command and set result when receiving event */ 
    private Map<EndpointCommandKey, BlockingCallable<?>> commandMap = new HashMap<>();

    public SmartHouseController(Context context, 
            DeviceType deviceType, 
            String userAccount, 
            DeviceStore deviceStore) {
        this.context = context;
        this.executor = new TimeoutExecutor();
        this.deviceType = deviceType;
        this.userAccount = userAccount;
        this.deviceStore = deviceStore;
    }
    
    /** Execute initialization of Kaa client in background. */
    public void init(CommandCallback<Void> callback) {
        new CommandAsyncTask<Void,Void>(callback) {
            @Override
            protected Void executeCommand(Void... params) throws Throwable {
                /** Set up device info. */
                initData();
                /** Set up Kaa client SDK */
                inited = new StartKaa().execute(executor, DEFAULT_TASK_TIMEOUT*3);
                if (deviceType == null && userAccount != null && userAccount.length() > 0) {
                    new AttachUserCommand(client, userAccount).execute(executor, DEFAULT_TASK_TIMEOUT);
                    discoverDevices();
                }
                return null;
            }
        }.execute();
    }
    
    /** Set up device info. */
    private void initData() {
        deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceType(getEventDeviceType());
        deviceInfo.setManufacturer(android.os.Build.MANUFACTURER);
        deviceInfo.setModel(android.os.Build.MODEL);
    }
    
    private org.kaaproject.kaa.demo.smarthouse.device.DeviceType getEventDeviceType() {
        if (deviceType != null) {
            switch (deviceType) {
            case THERMOSTAT:
                return org.kaaproject.kaa.demo.smarthouse.device.DeviceType.THERMOSTAT;
            case TV:
                return org.kaaproject.kaa.demo.smarthouse.device.DeviceType.TV;
            case SOUND_SYSTEM:
                return org.kaaproject.kaa.demo.smarthouse.device.DeviceType.SOUND_SYSTEM;
            case LAMP:
                return org.kaaproject.kaa.demo.smarthouse.device.DeviceType.LAMP;
            }
        }
        return null;
    }
    
    /** Stop Kaa client and release resourceEndpointKeys */
    public void stop() {
        if (inited) {
            if (client != null) {
                client.stop();
            }
            inited = false;
        }
    }
    
    public void pause() {
        if (inited) {
            if (client != null) {
                Log.d("Kaa", " Pausing Kaa client...");
                client.pause();
            }
        }
    }
    
    public void resume() {
        if (inited) {
            if (client != null) {
                Log.d("Kaa", "Resuming Kaa client...");
                client.resume();
            }
        }
    }
    
    /** Check if Kaa client SDK is initialized */
    private void checkInited() {
        if (!inited) {
            throw new IllegalStateException("Kaa client SDK is not initialized!");
        }
    }
    
    public String getEndpointAccessToken() {
        checkInited();
        return client.getEndpointAccessToken();
    }
    
    public boolean isAttachedToUser() {
        checkInited();
        return client.isAttachedToUser();
    }
    
    @Override
    public void onAttachedToUser(String arg0, String arg1) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onAttached();
        }
    }

    @Override
    public void onDetachedFromUser(String arg0) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onDetached();
        }
    }
    
    /** attach endpoint to user command */
    public void attachEndpoint(String endpointAccesToken, CommandCallback<String> callback) {
        checkInited();
        new CommandAsyncTask<String,String>(callback) {
            @Override
            protected String executeCommand(String... endpointAccesToken) throws Throwable {
                Log.d("Kaa", "Executing AttachEndpointCommand for access token " + endpointAccesToken[0]);
                return new AttachEndpointCommand(client, endpointAccesToken[0]).execute(executor, DEFAULT_TASK_TIMEOUT);
            }
        }.execute(endpointAccesToken);
    }
    
    /** deattach endpoint from user command */
    public void deattachEndpoint(String endpointKey, CommandCallback<Boolean> callback) {
        checkInited();
        new CommandAsyncTask<String,Boolean>(callback) {
            @Override
            protected Boolean executeCommand(String... endpointKey) throws Throwable {
                return new DetachEndpointCommand(client, endpointKey[0]).execute(executor, DEFAULT_TASK_TIMEOUT);
            }
        }.execute(endpointKey);
    }
    
    public String getCurrentEndpointKey() {
        return client.getEndpointKeyHash();
    }
    
    /** Get device info by issuing DeviceInfoRequest event to endpoint 
     *  identified by endpontKey.
     */
    public void getDeviceInfo(String endpontKey, CommandCallback<DeviceInfoResponse> callback) {
        checkInited();
        new CommandAsyncTask<String,DeviceInfoResponse>(callback) {
                @Override
                protected DeviceInfoResponse executeCommand(String... endpontKey) throws Throwable {
                    return new GetDeviceInfoCommand(commandMap, 
                            devices, 
                            endpontKey[0]).execute(executor, DEFAULT_TASK_TIMEOUT);
                }
        }.execute(endpontKey);
    }
    
    /** Implementation of Kaa client SDK set up command */
    class StartKaa extends BlockingCallable<Boolean> {
        
        StartKaa() {
            super(false);
        }

        @Override
        protected void executeAsync() {
            try {
                Log.d("Kaa", "Initializing Kaa client..."); 
                client = Kaa.newClient(new AndroidKaaPlatformContext(context), new SimpleKaaClientStateListener());

                EventFamilyFactory eventFamilyFactory = client.getEventFamilyFactory();
                devices = eventFamilyFactory.getDeviceEventClassFamily();
                devices.addListener(SmartHouseController.this);
                if (deviceType == null || deviceType == DeviceType.THERMOSTAT) {
                    thermostats = eventFamilyFactory.getThermoEventClassFamily();
                    thermostats.addListener(SmartHouseController.this);
                }
                if (deviceType == null || deviceType == DeviceType.SOUND_SYSTEM) {
                    soundSystems = eventFamilyFactory.getMusicEventClassFamily();
                    soundSystems.addListener(SmartHouseController.this);
                }
                if (deviceType != null) {
                    client.setAttachedListener(SmartHouseController.this);
                    client.setDetachedListener(SmartHouseController.this);
                }
                client.start();
                Log.d("Kaa", "Kaa client initialization completed.");
                onComplete(true);
            } catch (Exception e) {
                Log.e("Kaa", "Kaa client initialization failed.", e);
                onException(e);
            }
        }
    }
    
    /** Handle response. Find associated command by endpointKey and event class 
     *  then pass response as command result. */
    @SuppressWarnings("unchecked")
    private <T> void onResponse(String endpontKey, T response, Class<T> clazz) {
        EndpointCommandKey key = new EndpointCommandKey(clazz.getName(), endpontKey);
        BlockingCallable<T> callable = (BlockingCallable<T>) commandMap.remove(key);
        if (callable != null) {
            callable.onComplete(response);
        }
    }
    
    // Send methods
    
    // Devices
    public void discoverDevices() {
        checkInited();
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                devices.sendEventToAll(new DeviceInfoRequest());
            }
        });
    }
    
    public void discoverDevice(final String endpointKeyHash) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                devices.sendEvent(new DeviceInfoRequest(), endpointKeyHash);
            }
        });
    }
    
    public void sendDeviceInfo(final String endpointKeyHash) {
        final DeviceInfoResponse response = new DeviceInfoResponse();
        response.setDeviceInfo(deviceInfo);
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                devices.sendEvent(response, endpointKeyHash);
            }
        });
    }
    
    public void requestSpecificDeviceInfo(final String endpointKeyHash, 
            final org.kaaproject.kaa.demo.smarthouse.device.DeviceType deviceType) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                switch (deviceType) {
                case THERMOSTAT:
                    thermostats.sendEvent(new ThermostatInfoRequest(), endpointKeyHash);
                    break;
                case SOUND_SYSTEM:
                    soundSystems.sendEvent(new PlaybackInfoRequest(), endpointKeyHash);
                    break;
                default:
                    break;
                }
            }
        });
    }
    
    //Thermostats
    
    public void changeDegree(final String endpointKeyHash, final int targetDegree) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                ChangeDegreeRequest request = new ChangeDegreeRequest();
                request.setDegree(targetDegree);
                thermostats.sendEvent(request, endpointKeyHash);
            }
        });
    }
    
    public void updateThermostatInfo(final ThermostatInfo thermostatInfo) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                ThermostatInfoResponse response = new ThermostatInfoResponse();
                response.setThermostatInfo(thermostatInfo);
                thermostats.sendEventToAll(response);
            }
        });
    }
    
    public void sendThermostatInfo(ThermostatInfo thermostatInfo, final String endpointKeyHash) {
        final ThermostatInfoResponse response = new ThermostatInfoResponse();
        response.setThermostatInfo(thermostatInfo);
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                thermostats.sendEvent(response, endpointKeyHash);
            }
        });
    }
    
   //Sound systems
    
    public void playUrl(final String endpointKeyHash, final String url) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                PlayRequest request = new PlayRequest();
                request.setUrl(url);
                soundSystems.sendEvent(request, endpointKeyHash);
            }
        });
    }
    
    public void pause(final String endpointKeyHash) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                PauseRequest request = new PauseRequest();
                soundSystems.sendEvent(request, endpointKeyHash);
            }
        });
    }
    
    public void changeVolume(final String endpointKeyHash, final int volume) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                ChangeVolumeRequest request = new ChangeVolumeRequest();
                request.setVolume(volume);
                soundSystems.sendEvent(request, endpointKeyHash);
            }
        });
    }
    
    public void seekTo(final String endpointKeyHash, final int time) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                SeekRequest request = new SeekRequest();
                request.setTime(time);
                soundSystems.sendEvent(request, endpointKeyHash);
            }
        });
    }
    
    /** Get list of song info by issuing PlayListRequest event to endpoint 
     *  identified by endpontKey.
     */
    public void getPlayList(String endpontKey, CommandCallback<PlayListResponse> callback) {
        checkInited();
        new CommandAsyncTask<String,PlayListResponse>(callback) {
                @Override
                protected PlayListResponse executeCommand(String... endpontKey) throws Throwable {
                    return new GetPlayListCommand(commandMap, 
                            soundSystems, 
                            endpontKey[0]).execute(executor, DEFAULT_TASK_TIMEOUT);
                }
        }.execute(endpontKey);
    }
    
    public void updatePlaybackInfo(final PlaybackInfo playbackInfo) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                PlaybackInfoResponse response = new PlaybackInfoResponse();
                response.setPlaybackInfo(playbackInfo);
                soundSystems.sendEventToAll(response);
            }
        });
    }
    
    public void sendPlaybackInfo(PlaybackInfo playbackInfo, final String endpointKeyHash) {
        final PlaybackInfoResponse response = new PlaybackInfoResponse();
        response.setPlaybackInfo(playbackInfo);
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                soundSystems.sendEvent(response, endpointKeyHash);
            }
        });
    }
    
    public void sendPlayList(final List<SongInfo> playList, final String endpointKeyHash) {
        final PlayListResponse response = new PlayListResponse();
        response.setPlayList(playList);
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                soundSystems.sendEvent(response, endpointKeyHash);
            }
        });
    }
    
    // Listener methods

    // Device family
    
    /** Handle device info request from endpoint identified by sourceEndpointKey. 
     *  Send device info in response. */
    @Override
    public void onEvent(DeviceInfoRequest deviceInfoRequest, final String sourceEndpointKey) {
        sendDeviceInfo(sourceEndpointKey);
    }

    /** Handle device info response from endpoint identified by sourceEndpointKey. */
    @Override
    public void onEvent(DeviceInfoResponse deviceInfoResponse, String sourceEndpointKey) {
        Log.d("Kaa", "Device info response recieved");
        if (deviceStore != null) {
            deviceStore.onDeviceDiscovered(sourceEndpointKey, deviceInfoResponse.getDeviceInfo());
            requestSpecificDeviceInfo(sourceEndpointKey, deviceInfoResponse.getDeviceInfo().getDeviceType());
        }
        else {
            onResponse(sourceEndpointKey, deviceInfoResponse, DeviceInfoResponse.class);
        }
    }

    // Thermostat family
    
    @Override
    public void onEvent(ThermostatInfoRequest thermostatInfoRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(thermostatInfoRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(ThermostatInfoResponse thermostatInfoResponse, String sourceEndpointKey) {
        if (deviceStore != null) {
            deviceStore.onDeviceInfoDiscovered(sourceEndpointKey, DeviceType.THERMOSTAT, thermostatInfoResponse.getThermostatInfo());
        }
    }

    @Override
    public void onEvent(ChangeDegreeRequest changeDegreeRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(changeDegreeRequest, sourceEndpointKey);
        }
    }

    //Music family

    @Override
    public void onEvent(PlayListRequest playListRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(playListRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(PlayListResponse playListResponse, String sourceEndpointKey) {
        onResponse(sourceEndpointKey, playListResponse, PlayListResponse.class);
    }

    @Override
    public void onEvent(PlayRequest playRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(playRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(PauseRequest pauseRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(pauseRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(StopRequest stopRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(stopRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(ChangeVolumeRequest changeVolumeRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(changeVolumeRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(SeekRequest seekRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(seekRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(PlaybackInfoRequest playbackInfoRequest, String sourceEndpointKey) {
        if (context instanceof BaseDeviceListener) {
            ((BaseDeviceListener)context).onEvent(playbackInfoRequest, sourceEndpointKey);
        }
    }

    @Override
    public void onEvent(PlaybackInfoResponse playbackInfoResponse, String sourceEndpointKey) {
        if (deviceStore != null) {
            deviceStore.onDeviceInfoDiscovered(sourceEndpointKey, DeviceType.SOUND_SYSTEM, playbackInfoResponse.getPlaybackInfo());
        }
    }
}
