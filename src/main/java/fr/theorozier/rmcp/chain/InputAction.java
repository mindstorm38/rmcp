package fr.theorozier.rmcp.chain;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

public class InputAction implements Action {
	
	private static Scanner scanner;
	
	protected final String message;
	private final Consumer<Object> setter;
	
	public InputAction(String message, Consumer<Object> setter) {
		this.message = message;
		this.setter = setter;
	}
	
	protected String buildMessage() {
		return this.message + ": ";
	}
	
	protected Object parseValue(String line) {
		return line;
	}
	
	protected boolean retryIfNull() {
		return true;
	}
	
	@Override
	public Object run(Object last) {
		
		if (scanner == null) {
			scanner = new Scanner(System.in);
		}
		
		try {
			
			for (;;) {
				
				System.out.println(this.buildMessage());
				Object value = this.parseValue(scanner.nextLine());
				
				if (this.retryIfNull() && value == null) {
					System.out.print("Retry. ");
					continue;
				}
				
				this.setter.accept(value);
				return value;
				
			}
			
		} catch (NoSuchElementException e) {
			return null;
		}
		
	}
	
	public static Consumer<Object> buildMapSetter(Map<Object, Object> map, Object key) {
		return (val) -> map.put(key, val);
	}
	
}
