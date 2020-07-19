package fr.theorozier.rmcp.mcapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.theorozier.rmcp.util.JsonException;
import fr.theorozier.rmcp.util.OperatingSystem;
import fr.theorozier.rmcp.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionManifest {
	
	private final JsonObject data;
	private final Map<String, URL> urlCache = new HashMap<>();

	private List<Library> libsCache;
	private long libsTotalSize;
	
	private VersionManifest(JsonObject data) {
		this.data = data;
	}
	
	public URL getDownloadUrl(String download) {
		
		URL url = this.urlCache.get(download);
		
		if (url == null) {
			try {
				url = new URL(this.data.getAsJsonObject("downloads").getAsJsonObject(download).get("url").getAsString());
				this.urlCache.put(download, url);
			} catch (ClassCastException | NullPointerException | MalformedURLException ignored) { }
		}
		
		return url;
		
	}
	
	public URL getJarUrl(GameSide side) {
		return this.getDownloadUrl(side.id());
	}
	
	public URL getMappingsUrl(GameSide side) {
		return this.getDownloadUrl(side.id() + "_mappings");
	}
	
	public boolean doSupportCommons() {
		return this.getJarUrl(GameSide.CLIENT) != null && this.getJarUrl(GameSide.SERVER) != null;
	}
	
	public boolean doSupportMappings() {
		return this.doSupportCommons() && this.getMappingsUrl(GameSide.CLIENT) != null && this.getMappingsUrl(GameSide.SERVER) != null;
	}
	
	public List<Library> getLibraries() {
		
		if (this.libsCache == null) {
			
			List<Library> libs = new ArrayList<>();
			
			try {
				
				JsonArray libsArr = this.data.getAsJsonArray("libraries");
				
				for (JsonElement libRaw : libsArr) {
					
					JsonObject libObj = libRaw.getAsJsonObject();
					
					boolean allowed = true;
					
					JsonElement rulesRaw = libObj.get("rules");
					if (rulesRaw != null) {
						JsonArray rulesArr = rulesRaw.getAsJsonArray();
						if (rulesArr.size() != 0) {
							allowed = false;
							for (JsonElement ruleRaw : rulesArr) {
								
								JsonObject ruleObj = ruleRaw.getAsJsonObject();
								boolean newAllow = true;
								
								if (ruleObj.has("action")) {
									newAllow = ruleObj.get("action").getAsString().equals("allow");
								}
								
								if (ruleObj.has("os")) {
									OperatingSystem os = getMojangOs(ruleObj.getAsJsonObject("os").get("name").getAsString());
									if (OperatingSystem.is(os)) {
										allowed = newAllow;
									}
								} else {
									allowed = newAllow;
								}
								
							}
						}
					}
					
					if (allowed) {
						
						OperatingSystem nativesOs = null;
						String nativesClassifier = null;
						
						if (libObj.has("natives")) {
							JsonObject nativesObj = libObj.getAsJsonObject("natives");
							if (nativesObj.size() != 0) {
								for (Map.Entry<String, JsonElement> nativeEntry : nativesObj.entrySet()) {
									OperatingSystem os = getMojangOs(nativeEntry.getKey());
									if (OperatingSystem.is(os)) {
										nativesOs = os;
										nativesClassifier = nativeEntry.getValue().getAsString();
										break;
									}
								}
							}
						}
						
						JsonObject libDownloadsObj = libObj.getAsJsonObject("downloads");
						
						if (nativesOs == null) {
							
							JsonObject artifactObj = libDownloadsObj.getAsJsonObject("artifact");
							libs.add(Library.fromJson(artifactObj, null));
							
						} else {
							
							JsonObject classifiersObj = libDownloadsObj.getAsJsonObject("classifiers");
							if (classifiersObj.has(nativesClassifier)) {
								JsonObject classifierObj = classifiersObj.getAsJsonObject(nativesClassifier);
								libs.add(Library.fromJson(classifierObj, nativesOs));
							}
							
						}
						
					}
					
				}
				
			} catch (ClassCastException | NullPointerException | MalformedURLException ignored) { }
			
			this.libsCache = libs;
			this.libsTotalSize = libs.stream().mapToLong(Library::getSize).sum();
			
		}
		
		return this.libsCache;
		
	}
	
	public long getLibrariesTotalSize() {
		return this.libsTotalSize;
	}
	
	// Loading //
	
	public static VersionManifest loadManifest(URL url) throws JsonException {
		return new VersionManifest(Utils.readJsonFromUrl(MinecraftGson.GSON, url));
	}
	
	public static void writeManifest(VersionManifest manifest, Path path) throws JsonException {
		Utils.writeJsonToPath(MinecraftGson.GSON, manifest.data, path);
	}
	
	// Utils //
	
	public static OperatingSystem getMojangOs(String os) {
		switch (os) {
			case "osx": return OperatingSystem.MAC;
			case "linux": return OperatingSystem.UNIX;
			case "windows": return OperatingSystem.WINDOWS;
			default: return OperatingSystem.UNKNOWN;
		}
	}
	
	public static class Library {
		
		private final String filename;
		private final URL url;
		private final long size;
		private final OperatingSystem natives;
		
		public Library(String filename, URL url, long size, OperatingSystem natives) {
			this.filename = filename;
			this.url = url;
			this.size = size;
			this.natives = natives;
		}
		
		public String getFilename() {
			return this.filename;
		}
		
		public URL getUrl() {
			return this.url;
		}
		
		public long getSize() {
			return this.size;
		}
		
		public OperatingSystem getNatives() {
			return this.natives;
		}
		
		public boolean hasNatives() {
			return this.natives != null;
		}
		
		public static Library fromJson(JsonObject json, OperatingSystem natives) throws MalformedURLException {
			String path = json.get("path").getAsString();
			return new Library(path.substring(path.lastIndexOf('/') + 1), new URL(json.get("url").getAsString()), json.get("size").getAsLong(), natives);
		}
		
	}
	
}
