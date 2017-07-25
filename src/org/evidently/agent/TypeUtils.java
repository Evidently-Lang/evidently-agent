package org.evidently.agent;

public class TypeUtils {

	
	enum Type {
		BOOL, CHAR, SHORT, INT, LONG, DOUBLE, FLOAT, BYTE, ARRAY, OBJECT
	}
	
	
	
	public static Type getType(String s){
		
		if(s.startsWith("L")){
			return Type.OBJECT;
		}
		
		if(s.startsWith("[")){
			return Type.ARRAY;
		}
		
		if(s.equals("S")){
			return Type.SHORT;			
		}
		
		if(s.equals("Z")){
			return Type.BOOL;			
		}		
		
		if(s.equals("C")){
			return Type.CHAR;
			
		}
		
		if(s.equals("I")){
			return Type.INT;
		}
		
		if(s.equals("J")){
			return Type.LONG;
		}

		
		if(s.equals("D")){
			return Type.DOUBLE;
		}

		if(s.equals("F")){
			return Type.FLOAT;
		}		
		
		if(s.equals("B")){
			return Type.BYTE;
		}
		
		throw new IllegalArgumentException("Invalid type specification: " + s);
	}
}
