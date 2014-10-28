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

/**
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
package org.kaaproject.kaa.examples.robotrun.labyrinth.impl;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderInfo;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderUpdate;

public class BasicBorderUpdate implements BorderUpdate {
    private final List<BorderInfo> hBorders;
    private final List<BorderInfo> vBorders;

    private BasicBorderUpdate() {
        super();
        hBorders = new ArrayList<>();
        vBorders = new ArrayList<>();
    }

    public static BasicBorderUpdate hBorder(int x, int y, BorderType borderType) {
        BasicBorderUpdate border = new BasicBorderUpdate();
        border.hBorders.addAll(getSingletoneBorderList(x, y, borderType));
        return border;
    }

    public static BasicBorderUpdate vBorder(int x, int y, BorderType borderType) {
        BasicBorderUpdate border = new BasicBorderUpdate();
        border.vBorders.addAll(getSingletoneBorderList(x, y, borderType));
        return border;
    }

    private static List<BorderInfo> getSingletoneBorderList(int x, int y,
            BorderType borderType) {
        List<BorderInfo> borderList = new ArrayList<>();
        borderList.add(new BorderInfo(x, y, borderType));
        return borderList;
    }

    @Override
    public List<BorderInfo> getHBorders() {
        return hBorders;
    }

    @Override
    public List<BorderInfo> getVBorders() {
        return vBorders;
    }    

    @Override
    public void add(BorderUpdate other) {
        if (other != null) {
            if (other.getHBorders() != null) {
                hBorders.addAll(other.getHBorders());
            }
            if (other.getVBorders() != null) {
                vBorders.addAll(other.getVBorders());
            }
        }
    }
}
