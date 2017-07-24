package example;

import org.evidently.agent.FlowpointCollector;

public class SomeClass {

	public int p;
	
	
	public int getNewInt(int last, SomeClass c){
		return 3 + last;
	}
	
	public void doStuff(){
		p = getNewInt(2, new SomeClass());
		
		FlowpointCollector.f = "as";
	}
//	public static void testLocals(boolean one) {
//	    String two = "hello local variables";
//	    one = true;
//	    int three = 64;
//	    
//	    //ASMTest.printLocalString(two);
//	}

}
