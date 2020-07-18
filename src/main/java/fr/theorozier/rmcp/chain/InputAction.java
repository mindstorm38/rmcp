package fr.theorozier.rmcp.chain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class InputAction implements Action {
	
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
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			
			try {
				
				System.out.print(this.buildMessage());
				
				String line = reader.readLine();
				Object value = this.parseValue(line);
				
				if (this.retryIfNull() && value == null) {
					System.out.print("Retry. ");
					continue;
				}
				
				this.setter.accept(value);
				return value;
				
			} catch (IOException e) {
				return null;
			}
			
		}
		
	}
	
}
