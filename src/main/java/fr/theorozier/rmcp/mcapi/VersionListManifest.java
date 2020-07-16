package fr.theorozier.rmcp.mcapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.theorozier.rmcp.util.JsonException;
import fr.theorozier.rmcp.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public class VersionListManifest {
	
	private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
	
	private final JsonObject data;
	
	private VersionListManifest(JsonObject data) {
		this.data = data;
	}
	
	private String getLatest(String type) {
		try {
			return this.data.getAsJsonObject("latest").get(type).getAsString();
		} catch (ClassCastException | NullPointerException e) {
			return null;
		}
	}
	
	public String getLatestRelease() {
		return this.getLatest("release");
	}
	
	public String getLatestSnapshot() {
		return this.getLatest("snapshot");
	}
	
	public URL getVersionManifestUrl(String version) {
		
		try {
			JsonArray versions = this.data.getAsJsonArray("versions");
			for (JsonElement versionRaw : versions) {
				JsonObject versionObj = versionRaw.getAsJsonObject();
				if (versionObj.get("id").getAsString().equals(version)) {
					return new URL(versionObj.get("url").getAsString());
				}
			}
		} catch (ClassCastException | NullPointerException | MalformedURLException e) {
			//
		}
		
		return null;
		
	}
	
	/*public VersionManifest loadVersionManifest(String version) throws JsonException {
		URL manifestUrl = this.getVersionManifestUrl(version);
		return manifestUrl == null ? null : VersionManifest.loadManifest(manifestUrl);
	}*/
	
	// Loading //
	
	public static VersionListManifest loadManifest(URL url) throws JsonException {
		return new VersionListManifest(Utils.readJsonFromUrl(MinecraftGson.GSON, url));
	}
	
	public static VersionListManifest loadManifest() throws JsonException {
		
		try {
			return loadManifest(new URL(VERSION_MANIFEST_URL));
		} catch (MalformedURLException e) {
			throw new JsonException(e);
		}
		
	}
	
}
