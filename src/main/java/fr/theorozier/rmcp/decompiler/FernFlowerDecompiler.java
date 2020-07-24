package fr.theorozier.rmcp.decompiler;

import fr.theorozier.rmcp.util.Utils;
import fr.theorozier.rmcp.util.lib.Module;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FernFlowerDecompiler implements Decompiler {
	
	@Override
	public String name() {
		return "FernFlower";
	}
	
	@Override
	public long decompile(Path from, Path to) {
		
		long time = Module.FERN_FLOWER.callMainMethod(
				"-hed=0",
				"-hdc=0",
				"-dgs=1",
				"-ren=1",
				"-lit=1",
				"-asc=1",
				"-log=WARN",
				from.toAbsolutePath().toString(),
				to.toAbsolutePath().toString()
		);
		
		Path resultJarPath = to.resolve(from.getFileName());
		
		if (Files.isRegularFile(resultJarPath)) {
			
			try {
				
				Utils.extractZipArchive(resultJarPath, to, zipEntry -> {
					return zipEntry.getName().startsWith("com/") ||
							zipEntry.getName().startsWith("net/");
				});
				
			} catch (IOException e) {
				System.err.println("Failed to extract FernFlower produced jar.");
				e.printStackTrace();
			}
			
			try {
				Files.delete(resultJarPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return time;
		
	}
	
}
