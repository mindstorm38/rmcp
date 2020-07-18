package fr.theorozier.rmcp.decompiler;

import fr.theorozier.rmcp.util.lib.Lib;

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
		return Lib.FERN_FLOWER.callLibMainMethod(
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
	}
	
}
