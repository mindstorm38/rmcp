package fr.theorozier.rmcp.util.lib;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.Manifest;

public enum Module {
	
	CFR ("cfr-0.150"),
	FERN_FLOWER ("fernflower-44ae885"),
	SPECIAL_SOURCE ("SpecialSource-1.8.6"),
	PROCYON ("procyon-decompiler-0.5.36"),
	JSR305 ("jsr305-3.0.1");
	
	private final String lib;
	
	private URL libUrl;
	private URLClassLoader classLoader;
	private Class<?> mainClass;
	private Method mainMethod;
	
	Module(String lib) {
		this.lib = lib;
	}
	
	public Path ensureExtracted() {
		return ModuleManager.ensureModuleExtracted(this.lib);
	}
	
	public ClassLoader getClassLoader() {
		
		if (this.classLoader == null) {
			
			try {
				
				this.libUrl = this.ensureExtracted().toUri().toURL();
				this.classLoader = new URLClassLoader(new URL[] {this.libUrl}, Module.class.getClassLoader());
				
			} catch (MalformedURLException e) {
				throw new IllegalStateException("Failed to get lib URL for " + this + ".", e);
			}
			
		}
		
		return this.classLoader;
		
	}
	
	public Class<?> getMainClass() {
		
		if (this.mainClass == null) {
			
			ClassLoader loader = this.getClassLoader();
			
			InputStream stream = null;
			
			try {
				URL url = new URL("jar:file:" + this.libUrl.getFile() + "!/META-INF/MANIFEST.MF");
				stream = url.openStream();
			} catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			} catch (IOException e) {
				//
			}
			
			if (stream == null) {
				throw new IllegalStateException("This lib " + this + " do not have manifest.");
			}
			
			Manifest manifest = new Manifest();
			
			try {
				manifest.read(stream);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to load lib " + this + " manifest.", e);
			}
			
			String mainClassPath = manifest.getMainAttributes().getValue("Main-Class");
			
			if (mainClassPath == null) {
				throw new IllegalStateException("Main-Class attribute not found in lib " + this + " manifest.");
			}
			
			try {
				this.mainClass = Class.forName(mainClassPath, true, loader);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Failed to load main class '" + mainClassPath + "' for lib " + this + ".", e);
			}
			
		}
		
		return this.mainClass;
		
	}
	
	public Method getMainMethod() {
		
		if (this.mainMethod == null) {
			
			Class<?> mainClass = this.getMainClass();
			
			try {
				this.mainMethod = mainClass.getMethod("main", String[].class);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Can't find main(String[]) method for lib " + this + ".", e);
			}
			
		}
		
		return this.mainMethod;
		
	}
	
	public long callMainMethod(String...args) {
		
		Method method = this.getMainMethod();
		
		try {
			long start = System.currentTimeMillis();
			method.invoke(this.getMainClass(), (Object) args);
			return System.currentTimeMillis() - start;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException("Failed to call lib " + this + " main method.", e);
		}
		
	}
	
	public Thread callMainMethodThreaded(String...args) {
		Thread th = new Thread(() -> this.callMainMethod(args));
		th.start();
		return th;
	}
	
}
