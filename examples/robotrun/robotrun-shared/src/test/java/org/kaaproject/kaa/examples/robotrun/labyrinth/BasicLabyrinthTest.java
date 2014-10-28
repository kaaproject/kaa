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

package org.kaaproject.kaa.examples.robotrun.labyrinth;

import org.junit.Test;
import org.junit.Assert;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinth;

public class BasicLabyrinthTest {

	@Test
	public void createTest() {
		Labyrinth labyrinth = new BasicLabyrinth(6, 6);
		for (int i = 0; i < labyrinth.getHeight(); i++) {
			for (int j = 0; j < labyrinth.getWidth(); j++) {
				for (Direction side : Direction.values()) {
					Assert.assertEquals(BorderType.UNKNOWN, labyrinth.getCell(j, i)
							.getBorder(side));
				}
			}
		}
	}
	
	@Test
	public void cellUpdateTest() {
		Labyrinth labyrinth = new BasicLabyrinth(3, 6);
		Cell target, neighbor;
		target = labyrinth.getCell(1, 4);
		
		target.setBorder(Direction.WEST, BorderType.SOLID);
		neighbor = labyrinth.getCell(0, 4);
		Assert.assertEquals(BorderType.SOLID, neighbor.getBorder(Direction.EAST));
		target.setBorder(Direction.WEST, BorderType.UNKNOWN);
		
		target.setBorder(Direction.EAST, BorderType.SOLID);
		neighbor = labyrinth.getCell(2, 4);
		Assert.assertEquals(BorderType.SOLID, neighbor.getBorder(Direction.WEST));
		target.setBorder(Direction.EAST, BorderType.UNKNOWN);
		
		target.setBorder(Direction.NORTH, BorderType.SOLID);
		neighbor = labyrinth.getCell(1, 3);
		Assert.assertEquals(BorderType.SOLID, neighbor.getBorder(Direction.SOUTH));
		target.setBorder(Direction.NORTH, BorderType.UNKNOWN);
		
		target.setBorder(Direction.SOUTH, BorderType.SOLID);
		neighbor = labyrinth.getCell(1, 5);
		Assert.assertEquals(BorderType.SOLID, neighbor.getBorder(Direction.NORTH));
		target.setBorder(Direction.SOUTH, BorderType.UNKNOWN);	
	}	

	@Test
	public void cellIsDiscoveredTest() {
		Labyrinth labyrinth = new BasicLabyrinth(6, 6);
		Cell cell = labyrinth.getCell(5, 5);
		Assert.assertFalse(cell.isDiscovered());
		cell.setBorder(Direction.SOUTH, BorderType.SOLID);
		Assert.assertFalse(cell.isDiscovered());
		cell.setBorder(Direction.NORTH, BorderType.SOLID);
		Assert.assertFalse(cell.isDiscovered());
		cell.setBorder(Direction.WEST, BorderType.SOLID);
		Assert.assertFalse(cell.isDiscovered());
		cell.setBorder(Direction.EAST, BorderType.SOLID);
		Assert.assertTrue(cell.isDiscovered());		
	}	
	
	@Test
	public void cellIsExitTest() {
		Labyrinth labyrinth = new BasicLabyrinth(6, 6);
		Cell cell = labyrinth.getCell(5, 5);
		Assert.assertFalse(cell.isDiscovered());
		cell.setBorder(Direction.NORTH, BorderType.SOLID);
		cell.setBorder(Direction.SOUTH, BorderType.SOLID);
		cell.setBorder(Direction.WEST, BorderType.SOLID);
		cell.setBorder(Direction.EAST, BorderType.SOLID);
		Assert.assertFalse(cell.isExit());
		cell.setBorder(Direction.NORTH, BorderType.FREE);
		Assert.assertFalse(cell.isExit());
		cell.setBorder(Direction.WEST, BorderType.FREE);
		Assert.assertFalse(cell.isExit());
		cell.setBorder(Direction.EAST, BorderType.FREE);
		Assert.assertTrue(cell.isExit());
		cell.setBorder(Direction.EAST, BorderType.SOLID);
		Assert.assertFalse(cell.isExit());
		cell.setBorder(Direction.EAST, BorderType.FREE);
		Assert.assertTrue(cell.isExit());				
	}	
}
