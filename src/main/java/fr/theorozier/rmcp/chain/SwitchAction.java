package fr.theorozier.rmcp.chain;

import java.util.HashMap;
import java.util.Map;

public class SwitchAction implements Action {
	
	private final Map<Object, ActionChain> cases = new HashMap<>();
	
	public SwitchAction cas(Object previous, ActionChain chain) {
		this.cases.put(previous, chain);
		return this;
	}
	
	@Override
	public Object run(Object last) {
		ActionChain chain = this.cases.get(last);
		return chain == null ? null : chain.run();
	}
	
}
