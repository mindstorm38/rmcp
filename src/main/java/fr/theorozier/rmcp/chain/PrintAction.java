package fr.theorozier.rmcp.chain;

public class PrintAction implements Action {
	
	private final String message;
	
	public PrintAction(String message) {
		this.message = message;
	}
	
	public PrintAction() {
		this("");
	}
	
	@Override
	public Object run(Object last) {
		System.out.println(this.message);
		return true;
	}
	
}
