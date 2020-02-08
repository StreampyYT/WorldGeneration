package nl.streampy.playground;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
	private static Main instance;
	
	public void onEnable() {
		instance = this;
	}
	
	public void onDisable() {}
	
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String genId) {
	     return new BasicChunkGenerator();
	}
	
	public static Main getInstance() {
		return instance;
	}
	
}