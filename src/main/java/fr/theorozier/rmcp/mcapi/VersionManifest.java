package fr.theorozier.rmcp.mcapi;

import com.google.gson.JsonObject;
import fr.theorozier.rmcp.util.JsonException;
import fr.theorozier.rmcp.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class VersionManifest {
	
	private final JsonObject data;
	private final Map<String, URL> urlCache = new HashMap<>();
	
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
	
	// Loading //
	
	public static VersionManifest loadManifest(URL url) throws JsonException {
		return new VersionManifest(Utils.readJsonFromUrl(MinecraftGson.GSON, url));
	}
	
	public static void writeManifest(VersionManifest manifest, Path path) throws JsonException {
		Utils.writeJsonToPath(MinecraftGson.GSON, manifest.data, path);
	}
	
}
