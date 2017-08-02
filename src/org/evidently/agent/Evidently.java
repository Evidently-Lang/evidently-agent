package org.evidently.agent;

import java.lang.instrument.Instrumentation;
import java.util.List;
import edu.columbia.cs.psl.phosphor.struct.ControlTaintTagStack;

public class Evidently {
	
	public static List<Class <? extends Flowpoint>> flowpoints = null; 
	public static boolean debug = true;

	public static void premain$$PHOSPHORTAGGED(String args, Instrumentation inst, ControlTaintTagStack ctrl) {
		premain(args, inst);
	}
	
	public static void premain(String agentArgs, Instrumentation inst){


		System.out.println(String.format("[Evidently] [AGENT] Starting..."));

        final EvidentlyClassTransformer transformer = new EvidentlyClassTransformer();
        inst.addTransformer(transformer);		

	}
		
	public static void agentmain(String agentArgs, Instrumentation inst){
        //final EvidentlyClassTransformer transformer = new EvidentlyClassTransformer();
        //inst.addTransformer(transformer);		
	}
	
}
