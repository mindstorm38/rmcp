package fr.theorozier.rmcp;

import fr.theorozier.rmcp.chain.*;
import fr.theorozier.rmcp.chain.custom.DecompilerInputAction;
import fr.theorozier.rmcp.chain.custom.GameSideInputAction;
import fr.theorozier.rmcp.chain.custom.VersionInputAction;
import fr.theorozier.rmcp.decompiler.CfrDecompiler;
import fr.theorozier.rmcp.decompiler.Decompiler;
import fr.theorozier.rmcp.decompiler.FernFlowerDecompiler;
import fr.theorozier.rmcp.decompiler.ProguardToTsrgMapping;
import fr.theorozier.rmcp.mcapi.GameSide;
import fr.theorozier.rmcp.mcapi.VersionListManifest;
import fr.theorozier.rmcp.mcapi.VersionManifest;
import fr.theorozier.rmcp.util.ImprovedMap;
import fr.theorozier.rmcp.util.JsonException;
import fr.theorozier.rmcp.util.Utils;
import fr.theorozier.rmcp.util.lib.Module;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Supplier;

public class Main {
	
	public static final String DIR_VERSIONS = "versions";
	public static final String DIR_PROJECTS = "projects";
	public static final String DIR_MODULES = "modules";
	
	public static final String VERSION_DIR_LIBS = "libraries";
	public static final String VERSION_DIR_LIBS_NATIVES = "natives";
	public static final String VERSION_FILE_JVM_ARGS = "jvm.args";
	
	public static final String PROJECT_DIR_SRC = "src";
	
	public static final int MINIMUM_JAVA_VERSION = 8;
	
	public static void main(String[] args) {
		
		ImprovedMap<Setting, Object> settings = ImprovedMap.improve(new HashMap<>());
		
		new ActionChain()
				.append(new PrintAction("Reliable Minecraft Coder Pack [RMCP]"))
				.append(new PrintAction("Using official Mojang mappings to decompile client or server jars."))
				.append(new PrintAction("Default options are in uppercase, just enter."))
				.append(new PrintAction())
				.append(new AssertAction(Main::validateJavaVersion, "Java 8 minimum required."))
				.append(new DecompilerInputAction("Choose a decompiler, CFR or FernFlower", settings.putRef(Setting.DECOMPILER))
						.decompiler("cfr", CfrDecompiler::new)
						.decompiler("ff", FernFlowerDecompiler::new))
				.append(() -> loadManifest(settings))
				.append(new VersionInputAction("Version" , settings.putRef(Setting.VERSION), settings.getCastRef(Setting.VERSION_LIST_MANIFEST)))
				.append(() -> validateVersion(settings))
				.append(new GameSideInputAction("Select either client or server side", settings.putRef(Setting.SIDE)))
				.append(() -> validateSide(settings))
				.append(new YesNoInputAction("Setup a project", null, true))
				.append(new SwitchAction()
						.cas(true, new ActionChain()
								.append(new StringInputAction("Enter a suffix for your project directory", settings.putRef(Setting.PROJECT), false))
								.append(() -> validateProject(settings))
								.append(() -> decompile(settings))
						)
				)
				.run();
	
	}
	
	private static boolean validateJavaVersion() {
		return Utils.getJavaVersion() >= MINIMUM_JAVA_VERSION;
	}
	
	private static boolean loadManifest(ImprovedMap<Setting, Object> settings) {
		
		try {
			
			System.out.println("Reading versions list manifest...");
			
			VersionListManifest manifest = VersionListManifest.loadManifest();
			
			if (manifest.getLatestRelease() == null || manifest.getLatestSnapshot() == null) {
				System.out.println("Can't get latest release or latest snapshot.");
				return false;
			}
			
			settings.put(Setting.VERSION_LIST_MANIFEST, manifest);
			System.out.println("Manifest loaded.");
			return true;
			
		} catch (JsonException e) {
			
			System.out.println("Failed to load manifest, aborting :");
			e.printStackTrace();
			return false;
			
		}
		
	}
	
	private static boolean validateVersion(ImprovedMap<Setting, Object> settings) {
		
		String version = settings.getCast(Setting.VERSION);
		Path versionPath = Paths.get(DIR_VERSIONS, version);
		
		Path versionManifestPath = versionPath.resolve("manifest.json");
		URL versionManifestUrl;
		boolean mustCache = false;
		
		if (Files.isRegularFile(versionManifestPath)) {
			System.out.println("Version manifest '" + versionManifestPath + "' already exists, delete this file if you want to download again.");
			versionManifestUrl = Utils.pathToUrl(versionManifestPath);
		} else {
			VersionListManifest versionListManifest = settings.getCast(Setting.VERSION_LIST_MANIFEST);
			versionManifestUrl = versionListManifest.getVersionManifestUrl(version);
			mustCache = true;
		}
		
		if (versionManifestUrl == null) {
			System.out.println("Invalid version '" + version + "'.");
			return false;
		} else {
			
			try {
				Files.createDirectories(versionPath);
			} catch (IOException e) {
				System.out.println("Failed to create directory '" + versionPath + "'.");
				return false;
			}
			
			try {
				
				VersionManifest manifest = VersionManifest.loadManifest(versionManifestUrl);
				
				if (!manifest.doSupportMappings()) {
					System.out.println("This version is too old it do not support mappings, you can use MCP for that instead.");
					return false;
				}
				
				if (mustCache) {
					VersionManifest.writeManifest(manifest, versionManifestPath);
				}
				
				System.out.println("Selected version: " + version);
				
				settings.put(Setting.VERSION_MANIFEST, manifest);
				settings.put(Setting.VERSION_PATH, versionPath);
				return true;
				
			} catch (JsonException e) {
				System.out.println("Failed to load version manifest :");
				e.printStackTrace();
				return false;
			}
			
		}
		
	}
	
	private static boolean validateSide(ImprovedMap<Setting, Object> settings) {
		
		VersionManifest manifest = settings.getCast(Setting.VERSION_MANIFEST);
		Path versionPath = settings.getCast(Setting.VERSION_PATH);
		GameSide side = settings.getCast(Setting.SIDE);
		
		URL jarUrl = manifest.getJarUrl(side);
		URL mappingsUrl = manifest.getMappingsUrl(side);
		
		Path jarPath = versionPath.resolve(side.id() + ".jar");
		Path mappingsPath = versionPath.resolve(side.id() + ".map");
		
		if (!downloadIfNotExists(jarPath, true, jarUrl)) return false;
		if (!downloadIfNotExists(mappingsPath, true, mappingsUrl)) return false;
		
		Path tsrgMappingsPath = versionPath.resolve(side.id() + ".tsrg");
		
		if (!Files.isRegularFile(tsrgMappingsPath)) {
			
			try {
				ProguardToTsrgMapping.convert(mappingsPath, tsrgMappingsPath);
			} catch (IOException e) {
				System.out.println("Failed to convert (proguard) mappings to TSRG mappings.");
				e.printStackTrace();
				return false;
			}
			
		}
		
		Path remappedJarPath = versionPath.resolve(side.id() + ".remap.jar");
		
		if (!Files.isRegularFile(remappedJarPath)) {
			
			System.out.println("==== REMAPPING USING SPECIAL SOURCE ====");
			long time = Module.SPECIAL_SOURCE.callMainMethod(
					"--in-jar", jarPath.toAbsolutePath().toString(),
					"--out-jar", remappedJarPath.toAbsolutePath().toString(),
					"--srg-in", tsrgMappingsPath.toAbsolutePath().toString(),
					"--kill-lvt"
			);
			System.out.printf("Done remapping in '%s' in %.1fs\n", remappedJarPath.toString(), (time / 1000.0));
			
		}
		
		if (side == GameSide.CLIENT) {
			
			Path versionLibsPath = versionPath.resolve(VERSION_DIR_LIBS);
			Path versionNativesPath = versionLibsPath.resolve(VERSION_DIR_LIBS_NATIVES);
			Path jvmArgsPath = versionPath.resolve(VERSION_FILE_JVM_ARGS);
			
			try {
				Files.createDirectories(versionNativesPath);
			} catch (IOException e) {
				System.out.println("Failed to create directory for libs '" + versionNativesPath + "'.");
				e.printStackTrace();
				return false;
			}
			
			System.out.println("Downloading libraries ...");
			StringBuilder classPathBuilder = new StringBuilder();
			
			for (VersionManifest.Library lib : manifest.getLibraries()) {
				
				Path libPath = versionLibsPath.resolve(lib.getFilename());
				
				if (!lib.hasNatives()) {
					classPathBuilder.append(libPath.toAbsolutePath().toString());
					classPathBuilder.append(';');
				}
				
				if (downloadIfNotExists(libPath, false, lib.getUrl()) && lib.hasNatives()) {
					
					try {
						
						Utils.extractZipArchive(libPath, versionNativesPath, zipEntry -> {
							return !zipEntry.getName().startsWith("META-INF/") &&
									!zipEntry.getName().endsWith(".sha1") &&
									!zipEntry.getName().endsWith(".git");
						});
						
					} catch (IOException e) {
						System.out.println("Failed to extract lib natives:");
						e.printStackTrace();
					}
					
				}
				
			}
			
			System.out.println("Libraries downloaded.");
			
			if (!Files.isRegularFile(jvmArgsPath)) {
				
				System.out.println("Writing JVM arguments for classpath and libraries path to '" + jvmArgsPath + "'.");
				
				try (BufferedWriter writer = Files.newBufferedWriter(jvmArgsPath)) {
					writer.write("-classpath ");
					writer.write(classPathBuilder.toString());
					writer.write(" -Djava.library.path=");
					writer.write(versionNativesPath.toAbsolutePath().toString());
				} catch (IOException e) {
					System.out.println("Failed to write JVM arguments file:");
					e.printStackTrace();
				}
				
			}
			
		} else if (side == GameSide.SERVER) {
			System.out.println("Classpath and library path will be soon available for server projects.");
		}
		
		settings.put(Setting.SIDE_JAR_PATH, jarPath);
		settings.put(Setting.SIDE_MAPPINGS_PATH, mappingsPath);
		settings.put(Setting.SIDE_TSRG_MAPPINGS_PATH, tsrgMappingsPath);
		settings.put(Setting.SIDE_REMAPPED_JAR_PATH, remappedJarPath);
		
		return true;
		
	}
	
	private static boolean downloadIfNotExists(Path path, boolean warnIfExists, URL url) {
		return Utils.ifPathNotExists(path, warnIfExists, (p) -> {
			try {
				System.out.println("Downloading " + url + " file...");
				Files.copy(url.openStream(), path);
				return true;
			} catch (IOException e) {
				System.out.println("Failed to download file.");
				e.printStackTrace();
				return false;
			}
		}, true);
	}
	
	private static boolean validateProject(ImprovedMap<Setting, Object> settings) {
		
		String project = settings.getCast(Setting.PROJECT);
		Path projectPath = Paths.get(DIR_PROJECTS, project);
		
		if (Files.isDirectory(projectPath)) {
			System.out.println("This project '" + project + "' already exists, can't continue.");
			return false;
		} else {
			
			try {
				Files.createDirectories(projectPath);
			} catch (IOException e) {
				System.out.println("Failed to created directory '" + projectPath + "'.");
			}
			
			String version = settings.getCast(Setting.VERSION);
			GameSide side = settings.getCast(Setting.SIDE);
			
			settings.put(Setting.PROJECT_PATH, projectPath);
			
			try {
				Files.createFile(projectPath.resolve(version + ".mcversion"));
				Files.createFile(projectPath.resolve(side.id() + ".mcside"));
			} catch (IOException e) {
				System.out.println("Failed to create .mcversion/.mcside files.");
				e.printStackTrace();
				return false;
			}
			
			return true;
			
		}
	
	}
	
	private static boolean decompile(ImprovedMap<Setting, Object> settings) {
		
		Supplier<Decompiler> decompilerSupplier = settings.getCast(Setting.DECOMPILER);
		Decompiler decompiler = decompilerSupplier.get();
		
		Path remappedJarPath = settings.getCast(Setting.SIDE_REMAPPED_JAR_PATH);
		Path projectPath = settings.getCast(Setting.PROJECT_PATH);
		
		Path projectSrcPath = projectPath.resolve(PROJECT_DIR_SRC);
		
		if (!Files.isDirectory(projectSrcPath)) {
			
			try {
				Files.createDirectories(projectSrcPath);
			} catch (IOException e) {
				System.out.println("Failed to create project src directory.");
				e.printStackTrace();
				return false;
			}
			
		}
		
		System.out.println("==== DECOMPILING USING " + decompiler.name() + " ====");
		long time = decompiler.decompile(remappedJarPath, projectSrcPath);
		System.out.printf("Decompiled in '%s' in %.1fs\n", projectSrcPath.toString(), (time / 1000.0));
		
		return true;
		
	}
	
	private enum Setting {
		DECOMPILER,
		VERSION_LIST_MANIFEST,
		VERSION,
		VERSION_MANIFEST,
		VERSION_PATH,
		SIDE,
		SIDE_JAR_PATH,
		SIDE_MAPPINGS_PATH,
		SIDE_TSRG_MAPPINGS_PATH,
		SIDE_REMAPPED_JAR_PATH,
		PROJECT,
		PROJECT_PATH
	}
	
}
