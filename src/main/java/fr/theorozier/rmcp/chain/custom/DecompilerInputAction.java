package fr.theorozier.rmcp.chain.custom;

import fr.theorozier.rmcp.chain.InputAction;
import fr.theorozier.rmcp.decompiler.Decompiler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DecompilerInputAction extends InputAction {
	
	private final Map<String, Supplier<Decompiler>> decompilerSuppliers = new LinkedHashMap<>();
	
	public DecompilerInputAction(String message, Consumer<Object> setter) {
		super(message, setter);
	}
	
	public DecompilerInputAction decompiler(String name, Supplier<Decompiler> decompilerSupplier) {
		this.decompilerSuppliers.put(name.toLowerCase(), decompilerSupplier);
		return this;
	}
	
	@Override
	protected String buildMessage() {
		
		StringBuilder builder = new StringBuilder(this.message);
		
		if (this.decompilerSuppliers.isEmpty()) {
			builder.append(" [no valid value, please contact developer]: ");
		} else {
			
			builder.append(" [");
			
			boolean first = true;
			for (String key : this.decompilerSuppliers.keySet()) {
				if (first) {
					builder.append(key.toUpperCase());
					first = false;
				} else {
					builder.append('/');
					builder.append(key);
				}
			}
			
			builder.append("]: ");
			
		}
		
		return builder.toString();
	}
	
	@Override
	protected Object parseValue(String line) {
		if (this.decompilerSuppliers.isEmpty()) {
			return null;
		} else {
			return line.isEmpty() ? this.decompilerSuppliers.values().iterator().next() : this.decompilerSuppliers.get(line.toLowerCase());
		}
	}
	
	@Override
	protected boolean retryIfNull() {
		return !this.decompilerSuppliers.isEmpty();
	}
}
