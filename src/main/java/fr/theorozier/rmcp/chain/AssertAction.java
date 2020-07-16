package fr.theorozier.rmcp.chain;

import java.util.function.BooleanSupplier;

public class AssertAction implements Action {
	
	private final BooleanSupplier callback;
	private final String message;
	
	public AssertAction(BooleanSupplier callback, String message) {
		this.callback = callback;
		this.message = message;
	}
	
	public AssertAction(BooleanSupplier callback) {
		this(callback, null);
	}
	
	@Override
	public Object run(Object last) {
		if (this.callback.getAsBoolean()) {
			return true;
		} else {
			if (this.message != null) {
				System.out.println(this.message);
			}
			return null;
		}
	}
	
}
