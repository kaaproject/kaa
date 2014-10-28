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
package org.kaaproject.kaa.examples.robotrun.controller.path;

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaaExitSearchAlgorithm implements ExitSearchAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(KaaExitSearchAlgorithm.class);
    Map<Cell, CellMetaData> cellMetaData;
    
    public KaaExitSearchAlgorithm(Labyrinth labyrinth) {
        super();
        cellMetaData = new HashMap<>();
        for(int x=0; x < labyrinth.getWidth(); x++){
            for(int y=0; y < labyrinth.getHeight(); y++){
                cellMetaData.put(labyrinth.getCell(x, y), new CellMetaData());
            }
        }
    }

    @Override
    public Cell getNextCell(Labyrinth labyrinth, Cell current, Map<String, RobotPosition> other) {
        
        
        
        return null;
    }
    
    @Override
    public void onRobotLocationChanged(String key, Cell cell) {
        LOG.trace("Robot [{}] location changed to {}", key, cell);
        CellMetaData md = cellMetaData.get(cell);
        if(md != null){
            md.update(key, cell);
        }
    }    

    private static class CellMetaData {
        private int visitCount;
        private Map<String, Integer> otherVisitCount;
        
        private CellMetaData() {
            super();
            otherVisitCount = new HashMap<>();
        }
        
        public void update(String key, Cell cell) {
            // TODO Auto-generated method stub
        }

        private boolean isDeadEnd(){
            return visitCount == 2;
        }
    }

}
