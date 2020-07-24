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
				"-hdc=0",   // Not hide empty default constructor
				"-dgs=1",   // Decompile generic signature
				"-ren=1",   // Rename ambiguous names
				"-lit=1",   // Output numeric literals "as-is"
				"-asc=1",   // Encode non-ASCII characters in string and character literals as Unicode escapes
				"-nls=1",   // New line character '\n'
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
