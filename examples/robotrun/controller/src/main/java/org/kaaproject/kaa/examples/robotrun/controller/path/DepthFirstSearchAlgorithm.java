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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepthFirstSearchAlgorithm implements PathSearchAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(DepthFirstSearchAlgorithm.class);
    private Heuristic heuristic;

    @Override
    public List<Cell> getPath(Cell start, Cell target, Labyrinth labyrinth) {
        int moves = 0;
        Set<Cell> visitedCells = new HashSet<Cell>();
        List<Cell> path = new ArrayList<Cell>();
        Cell current = start;

        LOG.debug("Searching path from [{}, {}] to [{}, {}]", current.getX(), current.getY(), target.getX(), target.getY());
        while (!current.equals(target)) {
            moves++;
            visitedCells.add(current);
            LOG.debug("Adding cell [{}, {}] to visited set", current.getX(), current.getY());
            List<Cell> available = getAvailableTargets(current, target, labyrinth);
            if (available.isEmpty()) {
                LOG.info("There are no available paths from cell (dead end or cell is not yet discovered) [{}, {}]", current.getX(), current.getY());
                return null;
            }
            Cell next = getNext(available, visitedCells);
            LOG.debug("Next cell will be {}", next);
            if (next != null) {
                path.add(current);
                current = next;
            } else {
                if (!path.isEmpty()) {
                    LOG.debug("Path size is {}.", path.size());
                    current = path.remove(path.size() - 1);
                    LOG.debug("Going back to cell [{}, {}]", current.getX(), current.getY());
                } else {
                    LOG.info("Path was not found.");
                    return null;
                }
            }
        }
        path.add(current);
        path.remove(0);
        LOG.trace("Took {} moves", moves);
        return path;
    }

    private List<Cell> getAvailableTargets(Cell current, Cell target, Labyrinth lab) {
        List<Cell> result = new ArrayList<Cell>();

        int x = current.getX();
        int y = current.getY();
        LOG.debug("Border at direction {} is {}", Direction.EAST, current.getBorder(Direction.EAST));
        if (x + 1 < lab.getWidth() && current.getBorder(Direction.EAST) == BorderType.FREE) {
            result.add(lab.getCell(x + 1, y));
        }
        LOG.debug("Border at direction {} is {}", Direction.NORTH, current.getBorder(Direction.NORTH));
        if (y > 0 && current.getBorder(Direction.NORTH) == BorderType.FREE) {
            result.add(lab.getCell(x, y - 1));
        }
        LOG.debug("Border at direction {} is {}", Direction.WEST, current.getBorder(Direction.WEST));
        if (x > 0 && current.getBorder(Direction.WEST) == BorderType.FREE) {
            result.add(lab.getCell(x - 1, y));
        }
        LOG.debug("Border at direction {} is {}", Direction.SOUTH, current.getBorder(Direction.SOUTH));
        if (y + 1 < lab.getHeight() && current.getBorder(Direction.SOUTH) == BorderType.FREE) {
            result.add(lab.getCell(x, y + 1));
        }

        return result;
    }

    private Cell getNext(List<Cell> available, Set<Cell> visited) {
        if (heuristic != null) {
            Collections.sort(available, heuristic);
        }
        for (Cell c : available) {
            if (!visited.contains(c)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public void setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

}
