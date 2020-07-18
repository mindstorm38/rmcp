package fr.theorozier.rmcp.util;

public enum OperatingSystem {

	WINDOWS,
	MAC,
	UNIX,
	SOLARIS,
	UNKNOWN;
	
	private static OperatingSystem os;

	public static OperatingSystem get() {
		
		if (os == null) {
			
			String osn = System.getProperty("os.name", "").toLowerCase();
			
			if (osn.contains("win")) {
				os = WINDOWS;
			} else if (osn.contains("mac") || osn.contains("darwin")) {
				os = MAC;
			} else if (osn.contains("nix") || osn.contains("nux") || osn.contains("aix")) {
				os = UNIX;
			} else if (osn.contains("sunos") || osn.contains("solaris")) {
				os = SOLARIS;
			} else {
				os = UNKNOWN;
			}
			
		}
		
		return os;
		
	}
	
	public static boolean is(OperatingSystem tested) {
		return get() == tested;
	}
	
	public static boolean isWindows() {
		return os == WINDOWS;
	}
	
	public static boolean isMac() {
		return os == MAC;
	}
	
	public static boolean isUnix() {
		return os == UNIX;
	}
	
	public static boolean isSolaris() {
		return os == SOLARIS;
	}
	
	public static boolean isUnknown() {
		return os == UNKNOWN;
	}
	
}
