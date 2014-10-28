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
package org.kaaproject.kaa.examples.robotrun.controller.path;

import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;

public class ManhattanDistanceHeuristic implements Heuristic {
    private Cell target;

    public ManhattanDistanceHeuristic(Cell target) {
        this.target = target;
    }

    @Override
    public int compare(Cell o1, Cell o2) {
        return countDistance(o1, target) - countDistance(o2, target);
    }

    private int countDistance(Cell from, Cell to) {
        int dx = Math.max(from.getX(), to.getX()) - Math.min(from.getX(), to.getX());
        int dy = Math.max(from.getY(), to.getY()) - Math.min(from.getY(), to.getY());
        return dx + dy;
    }
}
