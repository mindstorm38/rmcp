package fr.theorozier.rmcp.chain;

import java.util.function.Consumer;

public class YesNoInputAction extends InputAction {
	
	private final Boolean def;
	
	public YesNoInputAction(String message, Consumer<Object> setter, Boolean def) {
		super(message, setter);
		this.def = def;
	}
	
	@Override
	protected String buildMessage() {
		
		StringBuilder builder = new StringBuilder(this.message);
		
		if (this.def == Boolean.TRUE) {
			builder.append(" [Y/n]: ");
		} else if (this.def == Boolean.FALSE) {
			builder.append(" [y/N]: ");
		} else {
			builder.append(" [y/n]: ");
		}
		
		return builder.toString();
		
	}
	
	@Override
	protected Object parseValue(String line) {
		
		if (line.isEmpty()) {
			
			if (this.def == Boolean.TRUE) {
				return true;
			} else if (this.def == Boolean.FALSE) {
				return false;
			} else {
				return null;
			}
			
		}
		
		char c = Character.toLowerCase(line.charAt(0));
		
		if (c == 'y') {
			return true;
		} else if (c == 'n') {
			return false;
		} else {
			return null;
		}
		
	}
	
}
