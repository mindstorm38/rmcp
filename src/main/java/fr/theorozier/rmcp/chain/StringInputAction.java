package fr.theorozier.rmcp.chain;

import java.util.function.Consumer;

public class StringInputAction extends InputAction {
	
	private final boolean allowEmpty;
	
	public StringInputAction(String message, Consumer<Object> setter, boolean allowEmpty) {
		super(message, setter);
		this.allowEmpty = allowEmpty;
	}
	
	@Override
	protected Object parseValue(String line) {
		return !this.allowEmpty && line.isEmpty() ? null : line;
	}
	
}
