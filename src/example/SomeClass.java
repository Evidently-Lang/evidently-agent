package example;

import org.evidently.agent.FlowpointCollector;
import org.evidently.annotations.Sink;
import org.evidently.annotations.Source;
import org.evidently.monitor.Label;
import org.evidently.monitor.SecurityLabelManager;
import org.evidently.policy.PolicyElementType;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

public class SomeClass {

	int myField;
	static int mySField;
	// public void writeLog(int msg) {
	// System.out.println("" + msg);
	// }

	public void test2() {
		System.out.println("TESTING");

		System.out.println("Assignment to a1");
		int a = MultiTainter.taintedInt(3, "a");

		System.out.println("Assignment to b1");
		int b = 3;

		System.out.println("Assignment to a2");
		// Taint t1 = MultiTainter.getTaint(a);
		// System.out.println(t1);
		a = b + 100;

		System.out.println("Assignment to a3");
		// Taint t2 = MultiTainter.getTaint(a);
		// System.out.println(t2);

		a = b + 100;

	}

	public int test() {
		 System.out.println("[BEFORE] MultiTainter.taintedInt()");
		 myField = MultiTainter.taintedInt(3, "a");
		 
		System.out.println("[BEFORE] assign b");
		 int b = 3;
		
		 System.out.println("Assignment to b1");
		 b = myField + 1;
		

		 System.out.println("Assignment to b2");
		 b = myField + 1;

		 System.out.println("Assignment to myField1");
		 myField = myField*2;
		
		 System.out.println("Assignment to mySField1");		 
		 mySField = myField;
		 
		 System.out.println("Assignment to myField2");
		 myField = 100 + 2;
		 
		 System.out.println("Assignment to mySField2");		 
		 mySField = myField + 1;
////		 		 
		 System.out.println("[BEFORE] Assignment to mySField3");		 
		 mySField = myField + 2;
		 
		
		return 3;

	}

	public static void main(String args[]) {
		System.out.println("[BEFORE] new");
		SomeClass sc = new SomeClass();

		System.out.println("[BEFORE] callsc");		
		sc.test();
	}

	// public static void main2(String args[]){
	// System.out.println("TESTING");
	//
	// System.out.println("Assignment to a1");
	// int a = MultiTainter.taintedInt(3, "a");
	//
	// System.out.println("Assignment to b1");
	// int b = 3;
	//
	// System.out.println("Assignment to a2");
	// //Taint t1 = MultiTainter.getTaint(a);
	// //System.out.println(t1);
	// a = b + 100;
	//
	// System.out.println("Assignment to a3");
	// //Taint t2 = MultiTainter.getTaint(a);
	// //System.out.println(t2);
	//
	// a = b + 100;
	//
	// //System.out.println(t2);
	////
	//// System.out.println("Assignment to b2");
	//// b = 1 + a;
	////
	//// System.out.println("Assignment to b3");
	//// b = 3;
	//
	// }
	// public void writeLog(String msg) {
	// int a = 3;
	//
	// int b = a + 3;
	// }

	// public String fieldString;
	// public long fieldInt;
	//
	// public void doStuff2(SomeClass cz){
	//
	//// NumberGuesser ng = new NumberGuesser();
	////
	//// SecurityLabelManager.update(
	//// ng,
	//// null);
	//
	// char b = 'a';
	//
	// b = SecurityLabelManager.update(b, null);
	//
	//
	//// cz.fieldString = "test";
	//// SecurityLabelManager.update(
	//// cz.fieldString,
	//// null);
	//
	//
	// }
	// @Sink("FIELDP") public int p;
	//
	// public int getNewInt(int last, SomeClass c){
	// return 3 + last;
	// }

	// public void doStuff2(){
	// System.out.println("I WAS HARD CODED");
	// }
	//
	// public void registerFlowpointPrimWithSources(){
	// int realNumber = 100;
	// realNumber = SecurityLabelManager.update(
	// realNumber,
	// new Label(
	// new String[] { "SINK_DB" , "SINK_NET", "SINK_DB" , "SINK_NET", "SINK_DB"
	// , "SINK_NET",}, // sinks
	// new String[] { "SOURCE_DB", "SOURCE_NET" }, // sources
	// PolicyElementType.FLOWPOINT, // the policy element this matches.
	// "Guess.guess" // the NAME in the policy it matches.
	//
	// ));
	//
	// }

	// public void registerFlowpointObjectWithSources(){
	// String realNumber = "asefs";
	//
	// SecurityLabelManager.register(
	// realNumber,
	// new Label(
	// new String[] { "DB" }, // sinks
	// new String[] { "DB" }, // sources
	// PolicyElementType.FLOWPOINT, // the policy element this matches.
	// "Guess.guess" // the NAME in the policy it matches.
	//
	// ));
	//
	// }
	//
	// public void registerPrim(){
	// int realNumber = 100;
	// realNumber = SecurityLabelManager.register(
	// realNumber,
	// new Label(
	// new String[] { "DB" }, // sinks
	// new String[] { "DB" } // sources
	// ));
	//
	// }
	//
	// public void registerObject(){
	// String realNumber = "asefs";
	//
	// SecurityLabelManager.register(
	// realNumber,
	// null
	// );
	//
	// }
	//
	//
	// public void doStuff(){
	// boolean bool = true;
	// int a = 1;
	// long b = 1;
	// double c = 2;
	// float d = 2;
	// byte e = 1;
	// String s = "test";
	// SomeClass sc = new SomeClass();
	// short mm = 3;
	// char ax = 's';
	// }
	// public static void testLocals(boolean one) {
	// String two = "hello local variables";
	// one = true;
	// int three = 64;
	//
	// //ASMTest.printLocalString(two);
	// }

}
