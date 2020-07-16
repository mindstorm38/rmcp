package fr.theorozier.rmcp.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ActionChain implements Action {

	private final List<Action> actions = new ArrayList<>();
	
	public Object run() {
		
		List<Action> actions = this.actions;
		Object result = null;
		
		for (Action action : actions) {
			if ((result = action.run(result)) == null) {
				return null;
			}
		}
		
		return result;
		
	}
	
	@Override
	public Object run(Object last) {
		return this.run();
	}
	
	public ActionChain append(Action action) {
		this.actions.add(action);
		return this;
	}
	
	public ActionChain append(Runnable callback) {
		return this.append((last) -> {
			callback.run();
			return true;
		});
	}
	
	public ActionChain append(Supplier<Object> callback) {
		return this.append((last) -> callback.get());
	}
	
	public ActionChain append(BooleanSupplier callback) {
		return this.append((last) -> callback.getAsBoolean() ? true : null);
	}
	
}
