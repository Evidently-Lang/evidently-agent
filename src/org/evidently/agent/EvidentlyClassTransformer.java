package org.evidently.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;

import edu.columbia.cs.psl.phosphor.runtime.TaintSentinel;
import edu.columbia.cs.psl.phosphor.struct.ControlTaintTagStack;

public class EvidentlyClassTransformer implements ClassFileTransformer {
	
	
//	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
//			final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
//		System.out.println(String.format("[Evidently] [AGENT] Starting transformation of [%s]", className));
//
//		return _transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
//	}
//	
	public byte[] transform$$PHOSPHORTAGGED(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
		
		System.out.println("CALLING transform$$PHOSPHORTAGGED");
		return null;
	}
	@Override
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

		System.out.println(String.format("[Evidently] [AGENT] Starting transformation of [%s]", className));

		
		if (className==null 
				|| className.startsWith("org/evidently")
				|| className.startsWith("sun/")
				|| className.startsWith("org/aspectj")
				|| className.startsWith("edu/columbia/")){

			System.out.println(String.format("[Evidently] [AGENT] Skipping transformation of [%s]", className));

			return null;
		}
 
		
		
		//if(1==1){ return null; }
		
		System.out.println("[Evidently] [AGENT] Bytecode Transforming Class: " + className);
		
		try {
			FlowpointCollector c = new FlowpointCollector(className);
			c.collectFlowpoints();

			FlowpointInstrumenter fpi = new FlowpointInstrumenter(className, c, Evidently.flowpoints);

			ClassWriter cw = fpi.instrument();

			return cw.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		
		return null;

	}
	
	
	
	public static byte[] _transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, final byte[] classfileBuffer, byte[] lastLoad) throws IllegalClassFormatException {

		System.out.println(String.format("[Evidently] [AGENT] Starting transformation of [%s]", className));

		
		if (className==null 
				|| className.startsWith("org/evidently")
				|| className.startsWith("sun/")
				|| className.startsWith("org/aspectj")
				|| className.startsWith("org/reflections")
				|| className.startsWith("edu/columbia/")){

			System.out.println(String.format("[Evidently] [AGENT] Skipping transformation of [%s]", className));

			return lastLoad;
		}
		
		if(className.startsWith("examples")==false){
			return lastLoad;
		}
 
		System.out.println("[Evidently] [AGENT] Bytecode Transforming Class: " + className);
		
		try {
			FlowpointCollector c = new FlowpointCollector(className);
			c.collectFlowpoints();

			FlowpointInstrumenter fpi = new FlowpointInstrumenter(className, c, Evidently.flowpoints);

			ClassWriter cw = fpi.instrument();

			return cw.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		
		return lastLoad;

	}
}