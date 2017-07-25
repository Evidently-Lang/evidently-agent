package org.evidently.agent;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

public class Evidently {

	public static List<Flowpoint> flowpoints = new ArrayList<Flowpoint>();
	
	public static void premain(String agentArgs, Instrumentation inst){
		
		// get all the flowpoints in the classpath. 
		Reflections reflections = new Reflections("org.evidently");
		//Reflections reflections = new Reflections("example");
		
		Set<Class<? extends Flowpoint>> subTypes = reflections.getSubTypesOf(Flowpoint.class);

		for(Class<? extends Flowpoint> f : subTypes){
			try {
				Flowpoint fp = (Flowpoint)f.newInstance();

				System.out.println(String.format("[Evidently] [AGENT] Loading flowpoint [%s]", fp.getName()));

				flowpoints.add(fp);
				
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		
        final EvidentlyClassTransformer transformer = new EvidentlyClassTransformer();
        inst.addTransformer(transformer);
	}
	
	public static void agentmain(String agentArgs, Instrumentation inst){
		
	}
	
}
