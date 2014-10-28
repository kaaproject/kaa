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

package org.kaaproject.kaa.examples.robotrun.android;

import org.kaaproject.kaa.client.KaaAndroid;
import org.kaaproject.kaa.examples.robotrun.controller.Launcher;
import org.kaaproject.kaa.examples.robotrun.controller.adapter.Bot;
import org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.AndroidBT;
import org.kaaproject.kaa.examples.robotrun.controller.adapter.bluetooth.AndroidBTClientSocket;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;

import android.content.Context;

public class AndroidBtLauncher extends Launcher {

    public AndroidBtLauncher(LauncherCallback callback, Context context, int startX, int startY, Direction startDirection, String name) throws Exception{
        super(new KaaAndroid(context), callback, new Bot(new AndroidBTClientSocket(context)), startX, startY, startDirection, name);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }
}
