package fr.theorozier.rmcp.decompiler;

import fr.theorozier.rmcp.util.lib.Module;

import java.nio.file.Path;
import java.util.List;

public class ProcyonDecompiler implements Decompiler {
	
	@Override
	public String name() {
		return "Procyon";
	}
	
	@Override
	public long decompile(Path from, Path to, List<Path> classPaths) {
		
		return Module.PROCYON.callMainMethod(
				"-jar", from.toString(),
				"-o", to.toString()
		);
		
	}
	
}
