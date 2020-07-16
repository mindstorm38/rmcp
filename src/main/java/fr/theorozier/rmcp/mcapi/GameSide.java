package fr.theorozier.rmcp.mcapi;

public enum GameSide {
	
	CLIENT,
	SERVER;
	
	private final String id;
	
	GameSide() {
		this.id = name().toLowerCase();
	}
	
	public String id() {
		return this.id;
	}
	
}
