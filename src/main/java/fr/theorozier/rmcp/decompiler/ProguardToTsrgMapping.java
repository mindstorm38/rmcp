package fr.theorozier.rmcp.decompiler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	private static Character remapPrimitive(String raw) {
		return PRIMITIVES_REMAP.get(raw);
	}
	
	private static String remapPath(String path) {
		return String.join("/", path.split("\\."));
	}
	
	private static String remapType(String type, Map<String, String> typesMapping) {
		
		Character primitive = remapPrimitive(type);
		
		if (primitive != null) {
			return primitive.toString();
		} else {
			String tmp = remapPath(type);
			return "L" + typesMapping.getOrDefault(tmp, tmp) + ";";
		}
		
	}
	
	private static String remapTypeWithArray(String type, Map<String, String> typesMapping) {
		
		int idx = type.indexOf('[');
		if (idx == -1) idx = type.length();
		int dim = (type.length() - idx) / 2;
		type = remapType(type.substring(0, idx), typesMapping);
		
		if (dim == 0) {
			return type;
		}
		
		StringBuilder builder = new StringBuilder(type);
		
		while (--dim >= 0) {
			builder.insert(0, '[');
		}
		
		return builder.toString();
		
	}
	
	public static void convert(Path in, Path out) throws IOException {
		
		BufferedWriter writer = Files.newBufferedWriter(out);
		
		Map<String, String> typesMapping = new HashMap<>();
		
		String line;
		String[] split;
		String deobf, obf;
		String type, name, args, tmp;
		int i, j;
		
		try (BufferedReader reader = Files.newBufferedReader(in)) {
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#") && !line.startsWith("    ")) {
					split = line.split(" -> ");
					deobf = split[0];
					obf = split[1];
					obf = obf.substring(0, obf.length() - 1);
					typesMapping.put(remapPath(deobf), remapPath(obf));
				}
			}
		}
		
		try (BufferedReader reader = Files.newBufferedReader(in)) {
			while ((line = reader.readLine()) != null) {
				
				if (line.startsWith("#")) {
					continue;
				}
				
				split = line.split(" -> ");
				deobf = split[0];
				obf = split[1];
				
				if (line.startsWith("   ")) {
					
					deobf = deobf.trim();
					obf = obf.trim();
					
					split = deobf.split(" ");
					type = split[0];
					name = split[1];
					
					if ((i = type.lastIndexOf(':')) != -1) {
						type = type.substring(i + 1);
					}
					
					i = name.indexOf('(');
					j = name.indexOf(')');
					
					writer.write('\t');
					writer.write(obf);
					
					if (i != -1 && j != -1) {
						
						args = name.substring(i + 1, j);
						name = name.substring(0, i);
						type = remapTypeWithArray(type, typesMapping);
						
						if (!args.isEmpty()) {
							args = Arrays.stream(args.split(","))
									.map(arg -> remapTypeWithArray(arg, typesMapping))
									.collect(Collectors.joining());
						}
						
						writer.write(" (");
						writer.write(args);
						writer.write(')');
						writer.write(type);
						
					}
					
					writer.write(' ');
					writer.write(name);
					
				} else {
					
					obf = obf.substring(0, obf.length() - 1);
					writer.write(remapPath(obf));
					writer.write(' ');
					writer.write(remapPath(deobf));
					
				}
				
				writer.write('\n');
				
			}
		}
		
		writer.close();
	
	}
	
}
