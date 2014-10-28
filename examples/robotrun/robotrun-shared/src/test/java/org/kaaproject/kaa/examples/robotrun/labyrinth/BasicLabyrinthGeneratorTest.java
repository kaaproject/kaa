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
package org.kaaproject.kaa.examples.robotrun.labyrinth;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinthGenerator;

public class BasicLabyrinthGeneratorTest {
    @Test
    public void testLabyrinthGeneration() {
        int height = 4;
        int width = 3;

        LabyrinthGenerator generator = new BasicLabyrinthGenerator(width, height);
        Labyrinth labyrinth = generator.generate(0, 0, width - 1, height - 1);

        Cell startCell = labyrinth.getCell(0, 0);

        Assert.assertTrue(startCell.getBorder(Direction.WEST) == BorderType.SOLID);
        Assert.assertTrue(startCell.getBorder(Direction.NORTH) == BorderType.SOLID);
        Assert.assertTrue(startCell.getBorder(Direction.SOUTH) == BorderType.FREE ||
                startCell.getBorder(Direction.EAST) == BorderType.FREE);

        Cell finishCell = labyrinth.getCell(width - 1, height - 1);

        Assert.assertTrue(finishCell.getBorder(Direction.EAST) == BorderType.FREE);
        Assert.assertTrue(finishCell.getBorder(Direction.SOUTH) == BorderType.SOLID);
        Assert.assertTrue(finishCell.getBorder(Direction.NORTH) == BorderType.FREE ||
                finishCell.getBorder(Direction.WEST) == BorderType.FREE);
    }
}
