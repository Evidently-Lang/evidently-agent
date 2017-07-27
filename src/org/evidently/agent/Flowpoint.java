package org.evidently.agent;

public abstract class Flowpoint {

	protected String scope;
	protected String type;
	protected String currentClass;
	protected String currentMethod;
	protected String name;
	protected ASTType astType;
	protected MethodCall lastMethodCall;
	public Flowpoint(){}
	public Flowpoint(String scope, String type, String currentClass, String currentMethod, String name, ASTType astType, MethodCall lastMethodCall)
	{
		this.scope = scope!=null ? scope.trim() : null;
		this.type = type!=null ? type.trim() : null; ;
		this.currentClass = currentClass!=null ? currentClass.trim() : null;;
		this.currentMethod = currentMethod!=null ? currentMethod.trim() : null;;
		this.name = name!=null ? name.trim() : null;;
		this.astType = astType;
		this.lastMethodCall = lastMethodCall;
	}

	public abstract String getName();
	// Scope is built from package.classs
	public abstract String getFlowpointFor();


	// implement the predicates. 
	public boolean within(String arg){
		return FlowpointPredicates.within(arg, scope, currentClass);
	}

	public boolean execution(String arg){
		return FlowpointPredicates.execution(arg, currentMethod, null); // TODO: implement advanced syntax
	}
	
	public boolean resultof(String arg){
		return FlowpointPredicates.resultof(arg, lastMethodCall);
	}
	
	public boolean pThis(String arg){
		return FlowpointPredicates.pThis(arg, scope, currentClass);
	}
	
	public boolean cflow(String arg){
		return FlowpointPredicates.cflow(arg, name, null);
	}
	
	public boolean named(String arg){
		return FlowpointPredicates.named(arg, name);
	}
	
	public boolean field(String arg){
		return FlowpointPredicates.field(arg, name, astType);
	}
	
	public boolean typeof(String arg){
		return FlowpointPredicates.typeof(arg, type);
	}
	
	
}
