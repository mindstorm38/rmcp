package fr.theorozier.rmcp.util.lib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibManager {
	
	private static Path librariesPath = null;
	
	public static InputStream getLibStream(String lib) {
		return LibManager.class.getResourceAsStream("/lib/" + lib);
	}
	
	public static Path getLibrariesPath() {
		
		if (librariesPath == null) {
			librariesPath = Paths.get("lib");
		}
		
		if (!Files.isDirectory(librariesPath)) {
			try {
				Files.createDirectories(librariesPath);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create libraries directories.", e);
			}
		}
		
		return librariesPath;
		
	}
	
	public static Path ensureLibExtracted(String lib) {
		
		Path path = getLibrariesPath().resolve(lib);
		
		if (!Files.isRegularFile(path)) {
			
			InputStream stream = getLibStream(lib);
			
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
