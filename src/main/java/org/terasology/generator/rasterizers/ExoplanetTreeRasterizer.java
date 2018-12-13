/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.generator.rasterizers;

import org.terasology.generator.ExoplanetTree;
import org.terasology.generator.facets.ExoplanetTreeFacet;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import static org.terasology.generator.ExoplanetWorldGenerator.EXOPLANET_SEA_LEVEL;

import java.util.Map;

public class ExoplanetTreeRasterizer implements WorldRasterizer {
    private Block trunk, leaf;

    @Override
    public void initialize() {
        trunk = CoreRegistry.get(BlockManager.class).getBlock("Exoplanet:ExoplanetTreeTrunk");
        leaf = CoreRegistry.get(BlockManager.class).getBlock("Exoplanet:ExoplanetTreeLeaf");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        ExoplanetTreeFacet facet = chunkRegion.getFacet(ExoplanetTreeFacet.class);

        for (Map.Entry<BaseVector3i, ExoplanetTree> entry : facet.getWorldEntries().entrySet()) {
            Vector3i treePosition = new Vector3i(entry.getKey()).addY(1);

            int height = entry.getValue().getHeight();
            int width = entry.getValue().getWidth();
            int radius = entry.getValue().getCrownRadius();
            int trunkHeight = entry.getValue().getTrunkHeight();
            int crownHeight = entry.getValue().getCrownHeight();
            int topCrownHeight = entry.getValue().getTopCrownHeight();
            int topCrownWidth = entry.getValue().getTopCrownWidth();

            // the position at the far top left corner - used as origin to create regions
            Vector3i treeMinimumPos = new Vector3i(treePosition).sub(radius, 0, radius);

            // creates regions for different parts of a tree
            Region3i treeRegion = Region3i.createFromMinAndSize(treeMinimumPos, new Vector3i(width, height, width));
            Region3i treeTrunk = Region3i.createFromMinAndSize(treePosition, new Vector3i(1, trunkHeight, 1));
            Region3i treeCrown = Region3i.createFromMinAndSize(new Vector3i(treeMinimumPos).addY(trunkHeight - 1),
                    new Vector3i(width, crownHeight, width));
            Region3i treeTop = Region3i.createFromMinAndSize(
                    new Vector3i(treeMinimumPos).add((width - topCrownWidth) / 2, trunkHeight + crownHeight - 1,
                            (width - topCrownWidth) / 2),
                    new Vector3i(topCrownWidth, topCrownHeight, topCrownWidth));

            // loop through each of the positions in the created regions and placing blocks
            for (Vector3i newBlockPosition : treeRegion) {
                if (chunkRegion.getRegion().encompasses(newBlockPosition)) {
                    if (treeTrunk.encompasses(newBlockPosition)) {
                        chunk.setBlock(ChunkMath.calcBlockPos(newBlockPosition), trunk);
                    } else if (!treeTrunk.encompasses(newBlockPosition)) {

                        if (treeCrown.encompasses(newBlockPosition) || treeTop.encompasses(newBlockPosition)) {
                            chunk.setBlock(ChunkMath.calcBlockPos(newBlockPosition), leaf);
                        }
                    }
                }
            }
        }
    }
}
