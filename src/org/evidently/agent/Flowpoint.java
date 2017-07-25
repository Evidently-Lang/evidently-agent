package org.evidently.agent;

public interface Flowpoint {
	
	// Scope is built from package.classs
	public String getFlowpointFor(String scope, String type, String currentClass, String currentMethod, String name, ASTType astType, MethodCall lastMethodCall);
}
