package example;

import org.evidently.agent.FlowpointCollector;
import org.evidently.annotations.Sink;
import org.evidently.annotations.Source;
import org.evidently.monitor.Label;
import org.evidently.monitor.SecurityLabelManager;
import org.evidently.policy.PolicyElementType;

public class SomeClass {

	public String fieldString;
	public long fieldInt;	

	public void doStuff2(SomeClass cz){
		
		fieldInt = 100;
//		SecurityLabelManager.update(
//		fieldInt, 
//		null);

		

//		cz.fieldString = "test";
//		SecurityLabelManager.update(
//		cz.fieldString, 
//		null);

		
	}
//	@Sink("FIELDP") public int p;
//	
//	public int getNewInt(int last, SomeClass c){
//		return 3 + last;
//	}
	
//	public void doStuff2(){
//		System.out.println("I WAS HARD CODED");
//	}
//	
//	public void registerFlowpointPrimWithSources(){
//		int realNumber = 100;
//		realNumber =  SecurityLabelManager.update(
//				realNumber, 
//				new Label(
//						new String[] { "SINK_DB" , "SINK_NET", "SINK_DB" , "SINK_NET", "SINK_DB" , "SINK_NET",},        // sinks 
//						new String[] { "SOURCE_DB", "SOURCE_NET" },         // sources
//						PolicyElementType.FLOWPOINT,  // the policy element this matches.
//						"Guess.guess"                  // the NAME in the policy it matches. 
//
//				));
//	
//	}
	
//	public void registerFlowpointObjectWithSources(){
//		String realNumber = "asefs";
//		
//		SecurityLabelManager.register(
//				realNumber, 
//				new Label(
//						new String[] { "DB" },        // sinks 
//						new String[] { "DB" },         // sources
//						PolicyElementType.FLOWPOINT,  // the policy element this matches.
//						"Guess.guess"                  // the NAME in the policy it matches. 
//
//				));
//	
//	}
//	
//	public void registerPrim(){
//		int realNumber = 100;
//		realNumber =  SecurityLabelManager.register(
//				realNumber, 
//				new Label(
//						new String[] { "DB" },        // sinks 
//						new String[] { "DB" }         // sources
//				));
//	
//	}
//	
//	public void registerObject(){
//		String realNumber = "asefs";
//		
//		SecurityLabelManager.register(
//				realNumber, 
//				null
//				);
//	
//	}
//
//
//	public void doStuff(){
//		boolean bool = true;
//		int a = 1;
//		long b = 1;
//		double c = 2;
//		float d = 2;
//		byte e = 1;
//		String s = "test";
//		SomeClass sc = new SomeClass();
//		short mm = 3;
//		char ax = 's';
//	}
//	public static void testLocals(boolean one) {
//	    String two = "hello local variables";
//	    one = true;
//	    int three = 64;
//	    
//	    //ASMTest.printLocalString(two);
//	}

}
