package fr.theorozier.rmcp.decompiler;

import java.nio.file.Path;
import java.util.List;

public interface Decompiler {
	String name();
	long decompile(Path from, Path to, List<Path> classPaths);
}
