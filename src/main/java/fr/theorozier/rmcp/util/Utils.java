package fr.theorozier.rmcp.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

	public static Path getMinecraftPath() {
	
		switch (OperatingSystem.get()) {
			case WINDOWS: return Paths.get("~", "AppData", "Roaming", ".minecraft");
			case MAC: return Paths.get("~", "Library", "Application Support", "minecraft");
			case UNIX: return Paths.get("~", ".minecraft");
			default: return null;
		}
		
	}
	
	public static int getJavaVersion() {
		
		String version = System.getProperty("java.version");
		
		if (version == null) {
			return 0;
		}
		
		if (version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf('.');
			if (dot != -1) {
				version = version.substring(0, dot);
			}
		}
		
		try {
			return Integer.parseInt(version);
		} catch (NumberFormatException e) {
			return 0;
		}
		
	}
	
	public static JsonObject readJsonFromUrl(Gson gson, URL url) throws JsonException {
		
		try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			return gson.fromJson(reader, JsonObject.class);
		} catch (IOException | JsonParseException e) {
			throw new JsonException(e);
		}
		
	}
	
	public static void writeJsonToPath(Gson gson, JsonObject json, Path path) throws JsonException {
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(path)))) {
			gson.toJson(json, writer);
		} catch (IOException | JsonParseException e) {
			throw new JsonException(e);
		}
		
	}
	
	public static URL pathToUrl(Path path) {
		try {
			return path.toUri().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
