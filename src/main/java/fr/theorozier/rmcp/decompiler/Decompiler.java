package fr.theorozier.rmcp.decompiler;

import java.nio.file.Path;

public interface Decompiler {
	String name();
	long decompile(Path from, Path to);
}
