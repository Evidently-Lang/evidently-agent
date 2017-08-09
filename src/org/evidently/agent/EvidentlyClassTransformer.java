package org.evidently.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.struct.ControlTaintTagStack;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayIntTags;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;

public class EvidentlyClassTransformer implements ClassFileTransformer {

	public LazyByteArrayObjTags transform$$PHOSPHORTAGGED(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain, LazyByteArrayObjTags classtaint,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		System.out.println("[Evidently] EX:transform$$PHOSPHORTAGGED1");

		LazyByteArrayObjTags ret = null;
		if (className == null || className.startsWith("sun")) {
			ret = new LazyByteArrayObjTags(classfileBuffer);
		} else {
			ret = new LazyByteArrayObjTags(
					transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer));
		}
		return ret;
	}

	public LazyByteArrayObjTags transform$$PHOSPHORTAGGED(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain, LazyByteArrayObjTags classtaint,
			byte[] classfileBuffer, ControlTaintTagStack ctrl) throws IllegalClassFormatException {

		System.out.println("[Evidently] EX:transform$$PHOSPHORTAGGED2");
		LazyByteArrayObjTags ret = null;

		if (className == null || className.startsWith("sun")){
			ret = new LazyByteArrayObjTags(classfileBuffer);
		}else{
			ret = new LazyByteArrayObjTags(
					transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer));
		}
		return ret;
	}

	public LazyByteArrayIntTags transform$$PHOSPHORTAGGED(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain, LazyByteArrayIntTags classtaint,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		System.out.println("[Evidently] EX:transform$$PHOSPHORTAGGED3");

		LazyByteArrayIntTags ret;
		if (className == null || className.startsWith("sun"))
			ret = new LazyByteArrayIntTags(classfileBuffer);
		else
			ret = new LazyByteArrayIntTags(
					transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer));
		return ret;
	}

	public byte[] transform(ClassLoader loader, final String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		return _transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer, classfileBuffer);
	}

	static {
		{
			// support up to 100 flowpoints

			if (Evidently.flowpoints == null) {

				Evidently.flowpoints = new ArrayList<Class<? extends Flowpoint>>();

				System.out.println("[Evidently] [AGENT] Locating flowpoints in classpath...");

				for (int i = 0; i < 100; i++) {
					try {
						String flowpoint = "Flowpoint$" + i;

						Flowpoint fp = (Flowpoint) ClassLoader.getSystemClassLoader()
								.loadClass("org.evidently.flowpoints." + flowpoint).newInstance();

						System.out.println("[Evidently] [AGENT] Attempting to load flowpoint " + flowpoint);

						System.out.println(String.format("[Evidently] [AGENT] Loading flowpoint [%s]", fp.getName()));

						Evidently.flowpoints.add((Class<? extends Flowpoint>) ClassLoader.getSystemClassLoader()
								.loadClass("org.evidently.flowpoints." + flowpoint));

					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						System.out.println("[Evidently] [AGENT] Attempting to load flowpoint Flowpoint$" + i + " [NO]");
					}

				}

			}

		}
	}

	public static byte[] _transform(final ClassLoader loader, final String className,
			final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer,
			byte[] lastLoad) throws IllegalClassFormatException {

		try {
			if (Evidently.debug) {
				// System.out.println(String.format("[Evidently] [AGENT] Starting transformation of [%s]", className));
			}

			if (className == null || className.startsWith("org/evidently") || className.startsWith("sun/")
					|| className.startsWith("org/aspectj") || className.startsWith("org/reflections")
					|| className.startsWith("edu/columbia/")) {

				if (Evidently.debug) {
					//System.out.println(String.format("[Evidently] [AGENT] Skipping transformation of [%s]", className));
				}

				return lastLoad;
			}

			if (className.startsWith("example") == false) {
				return lastLoad;
			}

			if (Evidently.debug) {
				System.out.println("[Evidently] [AGENT] Bytecode Transforming Class: " + className);
			}

			{
				if (Evidently.debug) {
					new File("bin-debug/" + className).mkdirs();

					FileOutputStream fos = new FileOutputStream("bin-debug/" + className + "-orig.class");
					fos.write(lastLoad);
					fos.close();
				}

			}

			// transformation #1, add pre taint checks
			if(true)
			{
				FlowpointCollector c = new FlowpointCollector(className, lastLoad);
				c.collectFlowpoints();

				FlowpointVariableRewriter rw = new FlowpointVariableRewriter(className, lastLoad, c);

				if (Evidently.debug) {
					System.out.println("[Evidently] [AGENT] Bytecode Transforming Class [PASS1] " + className);
				}
				ClassWriter cw = rw.instrument();

				if (Evidently.debug) {
					FileOutputStream fos = new FileOutputStream("bin-debug/" + className + "-xform1.class");
					fos.write(cw.toByteArray());
					fos.close();
				}

				lastLoad = cw.toByteArray();

			}

			// transformation #2, update and store checks
			{
				FlowpointCollector c = new FlowpointCollector(className, lastLoad);
				c.collectFlowpoints();

				FlowpointInstrumenter fpi = new FlowpointInstrumenter(className, lastLoad, c, Evidently.flowpoints);

				if (Evidently.debug) {
					System.out.println("[Evidently] [AGENT] Bytecode Transforming Class [PASS2] " + className);
				}

				ClassWriter cw = fpi.instrument();

				if (Evidently.debug) {
					FileOutputStream fos = new FileOutputStream("bin-debug/" + className + "-xform2.class");
					fos.write(cw.toByteArray());
					fos.close();
				}

				return cw.toByteArray();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lastLoad;

	}
}