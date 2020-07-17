package fr.theorozier.rmcp.decompiler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProguardToTsrgMapping {
	
	private static final Map<String, Character> PRIMITIVES_REMAP = new HashMap<>();
	
	static {
		PRIMITIVES_REMAP.put("int", 'I');
		PRIMITIVES_REMAP.put("double", 'D');
		PRIMITIVES_REMAP.put("boolean", 'Z');
		PRIMITIVES_REMAP.put("float", 'F');
		PRIMITIVES_REMAP.put("long", 'J');
		PRIMITIVES_REMAP.put("byte", 'B');
		PRIMITIVES_REMAP.put("short", 'S');
		PRIMITIVES_REMAP.put("char", 'C');
		PRIMITIVES_REMAP.put("void", 'V');
	}
	
	private static String remapPath(String path) {
		Character prim = PRIMITIVES_REMAP.get(path);
		return prim == null ? ("L" + String.join("/", path.split("\\."))) : prim.toString();
	}
	
	public static void convert(InputStream in, OutputStream out) {
	
	
	
	}
	
}
