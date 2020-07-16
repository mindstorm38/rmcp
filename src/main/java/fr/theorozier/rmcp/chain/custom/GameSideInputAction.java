package fr.theorozier.rmcp.chain.custom;

import fr.theorozier.rmcp.chain.InputAction;
import fr.theorozier.rmcp.mcapi.GameSide;

import java.util.function.Consumer;

public class GameSideInputAction extends InputAction {
	
	public GameSideInputAction(String message, Consumer<Object> setter) {
		super(message, setter);
	}
	
	@Override
	protected String buildMessage() {
		return this.message + " [C/s]: ";
	}
	
	@Override
	protected Object parseValue(String line) {
		
		if (line.isEmpty()) {
			return GameSide.CLIENT;
		} else {
			char c = Character.toLowerCase(line.charAt(0));
			return c == 'c' ? GameSide.CLIENT : c == 's' ? GameSide.SERVER : null;
		}
		
	}
	
}
