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

import org.kaaproject.kaa.client.common.CommonArray;
import org.kaaproject.kaa.client.common.CommonEnum;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.common.CommonValue;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.LabyrinthConverter;

public class BasicLabyrinthConverter implements LabyrinthConverter {

    @Override
    public Labyrinth convert(Labyrinth reuse, CommonRecord config) {
        if (config != null) {
            BorderType[][] hBorders = toBorders(config.getField("hBorders"));
            BorderType[][] vBorders = toBorders(config.getField("vBorders"));
            int width = hBorders.length;
            int height = 0;
            if(width > 0){
                height = Math.max(0, hBorders[0].length - 1);
            }
            if(reuse!= null && reuse.getWidth() == width && reuse.getHeight() == height){
                reuse.update(hBorders, vBorders);
                return reuse;
            }else{
                BasicLabyrinth labyrinth = new BasicLabyrinth(width, height);
                labyrinth.update(hBorders, vBorders);
                return labyrinth;
            }
        }else{
            if(reuse != null && reuse.isEmpty()){
                return reuse;
            }else{
                return new BasicLabyrinth(0, 0);
            }
        }
    }

    private BorderType[][] toBorders(CommonValue bordersVal) {
        int width = 0;
        int height = 0;
        
        List<Border> borders = new ArrayList<>();        
        if (!bordersVal.isNull() && bordersVal.isArray()) {
            CommonArray bordersArray = bordersVal.getArray();
            for (CommonValue borderVal : bordersArray.getList()) {
                if (!borderVal.isNull() && borderVal.isRecord()) {
                    CommonRecord borderRecord = borderVal.getRecord();
                    CommonEnum borderTypeEnum = borderRecord.getField("type")
                            .getEnum();
                    BorderType type = BorderType.valueOf(borderTypeEnum
                            .getSymbol());
                    Border border = new Border(borderRecord.getField("x")
                            .getInteger(), borderRecord.getField("y")
                            .getInteger(), type);
                    borders.add(border);
                    width = Math.max(width, border.x);
                    height = Math.max(height, border.y);
                }
            }
        }

        BorderType[][] result;
        if (!borders.isEmpty()) {
            result = new BorderType[width+1][height+1];
            for(Border border : borders){
                result[border.x][border.y] = border.type;
            }
        } else {
            result = new BorderType[0][0];
        }
        return result;
    }

    private static class Border {
        private final int x;
        private final int y;
        private final BorderType type;

        public Border(int x, int y, BorderType borderType) {
            super();
            this.x = x;
            this.y = y;
            this.type = borderType;
        }
    }

}
