package org.evidently.agent;

public class FlowpointPredicates {
	
	public static boolean within(String arg, String scope, String currentClass){
		String s = scope;
		if(s.contains("/")){
			s = s.replaceAll("/", ".");
		}
		
		// scope could be:
		// org.apache
		// org.apache.foo
		
		// arg could be 
		// org.apache => true for both cases
		// org.apache.foo => true for only the second case
		
		return (scope.startsWith(arg) || (scope + "." + currentClass).startsWith(arg));
	}
	
	public static boolean execution(String arg, String currentMethod, String methodSignature)
	{
		return currentMethod.equals(arg);
	}
	
	public static boolean resultof(String arg, MethodCall lastMethodCall){
		if(lastMethodCall==null){
			return false;
		}
		
		return false;
	}
	
	public static boolean pThis(String arg, String scope, String currentClass)
	{
		// they could have written it several ways
		// for example this(foo.bar.MyClass)
		// OR within(foo.bar) && this(MyClass)
		
		if(arg.indexOf('.')!=-1){
			return scope.equals(arg);
		}
		
		return arg.equals(currentClass);		
	}
	
	public static boolean cflow(String arg, String name, String methodSignature){
		return execution(arg, name, methodSignature); // not implemented for now
	}
	
	
	public static boolean named(String arg, String name){ 
		return arg.equals(name);
	}
	public static boolean field(String arg, String name, ASTType type){		
		return type==ASTType.FIELD && named(arg, name); 
	}
	
	public static boolean typeof(String arg, String type){ 
		return arg.equals(type);
	}

}
