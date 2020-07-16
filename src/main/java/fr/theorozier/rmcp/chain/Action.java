package fr.theorozier.rmcp.chain;

@FunctionalInterface
public interface Action {
	Object run(Object last);
}
