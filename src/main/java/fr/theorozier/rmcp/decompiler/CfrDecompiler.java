package fr.theorozier.rmcp.decompiler;

import fr.theorozier.rmcp.util.lib.Lib;

import java.nio.file.Path;

public class CfrDecompiler implements Decompiler {
	
	@Override
	public String name() {
		return "CFR";
	}
	
	@Override
	public long decompile(Path from, Path to) {
		
		long time = Lib.CFR.callLibMainMethod(
				from.toAbsolutePath().toString(),
				"--outputdir", to.toAbsolutePath().toString(),
				"--caseinsensitivefs", "true",
				"--silent", "true"
		);
		
		/*try {
			Files.delete(to.resolve("summary.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		return time;
		
	}
	
}
