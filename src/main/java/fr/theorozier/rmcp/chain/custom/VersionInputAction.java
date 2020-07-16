package fr.theorozier.rmcp.chain.custom;

import fr.theorozier.rmcp.chain.InputAction;
import fr.theorozier.rmcp.mcapi.VersionListManifest;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VersionInputAction extends InputAction {
	
	private final Supplier<VersionListManifest> versionManifestSupplier;
	
	public VersionInputAction(String message, Consumer<Object> setter, Supplier<VersionListManifest> versionManifestSupplier) {
		super(message, setter);
		this.versionManifestSupplier = versionManifestSupplier;
	}
	
	@Override
	protected Object parseValue(String line) {
		
		line = line.toLowerCase();
		
		VersionListManifest manifest = this.versionManifestSupplier.get();
		
		if (line.equals("snapshot") && manifest != null) {
			return manifest.getLatestSnapshot();
		} else if (line.equals("release") && manifest != null) {
			return manifest.getLatestRelease();
		} else {
			return line;
		}
		
	}
	
	@Override
	public Object run(Object last) {
		System.out.println("Please select a valid version (starting from 19w36a / 1.14.4).\nUse 'snapshot' or 'release' for latest snapshot or release.");
		return super.run(last);
	}
	
}
