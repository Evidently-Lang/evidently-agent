package org.evidently.agent;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.struct.ControlTaintTagStack;

public class Evidently {
	
	static {
		//Configuration.ADDL_IGNORE = "org/evidently/"; //agent";
	}

	public static List<Class <? extends Flowpoint>> flowpoints = null; //new ArrayList<Flowpoint>();

	public static void premain$$PHOSPHORTAGGED(String agentArgs, Instrumentation inst, ControlTaintTagStack ctts){
		System.out.println(String.format("[Evidently] [AGENT] Starting Phosphor Shim..."));
		
		premain(agentArgs, inst);
	}

	public static void premain(String agentArgs, Instrumentation inst){


		System.out.println(String.format("[Evidently] [AGENT] Starting..."));

        final EvidentlyClassTransformer transformer = new EvidentlyClassTransformer();
        inst.addTransformer(transformer);		

	}
		///if(1==1){ return;}
		
		// get all the flowpoints in the classpath. 
//		Reflections reflections = new Reflections("org.evidently");
//		//Reflections reflections = new Reflections("example");
//		
//		Set<Class<? extends Flowpoint>> subTypes = reflections.getSubTypesOf(Flowpoint.class);
//
//		for(Class<? extends Flowpoint> f : subTypes){
//			try {
//				Flowpoint fp = (Flowpoint)f.newInstance();
//
//				System.out.println(String.format("[Evidently] [AGENT] Loading flowpoint [%s]", fp.getName()));
//
//				flowpoints.add(fp);
//				
//			} catch (InstantiationException | IllegalAccessException e) {
//				e.printStackTrace();
//			}
//		}
		
        //final EvidentlyClassTransformer transformer = new EvidentlyClassTransformer();
        //inst.addTransformer(transformer);		
		
//	}
	
	public static void agentmain(String agentArgs, Instrumentation inst){
        //final EvidentlyClassTransformer transformer = new EvidentlyClassTransformer();
        //inst.addTransformer(transformer);		
	}
	
}
