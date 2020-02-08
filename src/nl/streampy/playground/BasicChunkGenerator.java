package nl.streampy.playground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class BasicChunkGenerator extends ChunkGenerator {

	/*@SuppressWarnings("deprecation")
	void setBlock(int x, int y, int z, ChunkData chunk, Material material) {
		if (y < 256 && y >= 0 && x <= 16 && x >= 0 && z <= 16 && z >= 0) { 
			if (chunk[y >> 4] == null)
				chunk[y >> 4] = new short[16 * 16 * 16];
			chunk[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (short) material.getId();
		}
	}

	short getBlock(int x, int y, int z, short[][] chunk) {
		if (y < 256 && y >= 0 && x <= 16 && x >= 0 && z <= 16 && z >= 0) { 
			if (chunk[y >> 4] == null)
				return 0;
			return chunk[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
		}
		else return 0;
	}*/

	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
		Biomes.setWorld(world);
		
		ChunkData chunk = createChunkData(world);
		BiomeGenerator biomeGenerator = new BiomeGenerator(world);

		for (int x=0; x<16; x++) {
			for (int z=0; z<16; z++) {
				int realX = x + chunkX * 16;
	 			int realZ = z + chunkZ * 16;
				
				//We get the 3 closest biome's to the temperature and rainfall at this block
				HashMap<Biomes, Double> biomes = biomeGenerator.getBiomes(realX, realZ);
				//And tell bukkit (who tells the client) what the biggest biome here is
				biomeGrid.setBiome(x, z, getDominantBiome(biomes));
				
				// for illustrative purposes, we colour the biomes differently
				Material material = getBiomeMaterial(biomeGrid.getBiome(x, z)); 

				// To make it more maintainable, we've abstracted finding height
				// and density values
				int bottomHeight = getHeight(realX, realZ, biomes);
				
				// This has been lowered to 10 to avoid massive performance issues.
				// We take (10 vertical blocks * 3 (closest) biomes * 16 columns)
				// noise values per chunk! In the next tutorial, I will show you how
				// to use interpolation to "guess" the values between, so that you
				// can avoid expensive calls to the noise generator
				int maxHeight = bottomHeight + 10;
				double threshold = 0.3;

				for (int y=0; y<maxHeight; y++) {
					if (y > bottomHeight) {
						double density = getDensity(realX, y, realZ, biomes);

						if (density > threshold) chunk.setBlock(x, y, z, material);

					} else {
						chunk.setBlock(x, y, z, material);
					}
				}

				chunk.setBlock(x, bottomHeight, z, getBiomeMaterial(biomeGrid.getBiome(x, z)));
				chunk.setBlock(x, bottomHeight -1, z, material);
				chunk.setBlock(x, bottomHeight -2, z, material);

				for (int y=bottomHeight + 1; y>bottomHeight && y < maxHeight; y++ ) {
					Material thisblock = chunk.getBlockData(x, y, z).getMaterial();
					Material blockabove = chunk.getBlockData(x, y +1, z).getMaterial();

					if(thisblock != Material.AIR && blockabove == Material.AIR) {
						chunk.setBlock(x, y, z, material);
						if(chunk.getBlockData(x, y -1, z).getMaterial() != Material.AIR)
							chunk.setBlock(x, y -1, z, material);
						if(chunk.getBlockData(x, y -2, z).getMaterial() != Material.AIR)
							chunk.setBlock(x, y-2, z, material);
					}
				}

			}
		}
		return chunk;
	}

	//This would normaly be in an enum, but it's only so you can see biome lines
	private Material getBiomeMaterial(Biome biome) {
		switch (biome) {
		case DESERT: return Material.SANDSTONE;
		case FOREST: return Material.OAK_LOG;
		case PLAINS: return Material.DIRT;
		case SWAMP: return Material.MYCELIUM;
		case MOUNTAINS: return Material.STONE;
		default: return Material.GLASS;
		}
	}

	//We get the closest biome to send to the client (using the biomegrid)
	private Biome getDominantBiome(HashMap<Biomes, Double> biomes) {
		double maxNoiz = 0.0;
		Biomes maxBiome = null;
		
		for (Biomes biome : biomes.keySet()) {
			if (biomes.get(biome) >= maxNoiz) {
				maxNoiz = biomes.get(biome);
				maxBiome = biome;
			}
		}
		return maxBiome.biome;
	}

	private double getDensity(int x, int y, int z, HashMap<Biomes, Double> biomes) {
		double noise = 0.0;
		for (Biomes biome : biomes.keySet()) {
			double weight = biomes.get(biome);
			noise += biome.generator.get3dNoise(x, y, z)*weight;
		}
		return noise;
	}

	private int getHeight(int x, int z, HashMap<Biomes, Double> biomes) {
		double noise = 0.0;
		for (Biomes biome : biomes.keySet()) {
			double weight = biomes.get(biome);
			noise += biome.generator.get2dNoise(x, z)*weight;
		}
		return (int) (noise + 64);
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		ArrayList<BlockPopulator> pops = new ArrayList<BlockPopulator>();
		//Add Block populators here
		return pops;
	}
}