package fr.theorozier.rmcp.util.lib;

import fr.theorozier.rmcp.Main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModuleManager {
	
	private static final String JAR_RES_EXT = ".jar.noshade"; // To avoid shadowing of embed jars from gradle shadow plugin
	private static final String JAR_LIB_EXT = ".jar";
	
	private static Path modulesPath = null;
	
	public static InputStream getModuleStream(String lib) {
		return ModuleManager.class.getResourceAsStream("/lib/" + lib + JAR_RES_EXT);
	}
	
	public static Path getModulesPath() {
		
		if (modulesPath == null) {
			modulesPath = Paths.get(Main.DIR_MODULES);
		}
		
		if (!Files.isDirectory(modulesPath)) {
			try {
				Files.createDirectories(modulesPath);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create libraries directories.", e);
			}
		}
		
		return modulesPath;
		
	}
	
	public static Path ensureModuleExtracted(String lib) {
		
		Path path = getModulesPath().resolve(lib + JAR_LIB_EXT);
		
		if (!Files.isRegularFile(path)) {
			
			InputStream stream = getModuleStream(lib);
			
			if (stream == null) {
				throw new IllegalArgumentException("Lib '" + lib + "' not found.");
			}
			
			try {
				Files.copy(stream, path);
			} catch (IOException e) {
				throw new IllegalArgumentException("Failed to extract lib '" + lib + "' to '" + path + "'.", e);
			}
			
		}
		
		return path;
		
	}
	
}
