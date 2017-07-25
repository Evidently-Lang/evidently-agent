package org.evidently.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;

public class EvidentlyClassTransformer implements ClassFileTransformer {
	@Override
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

		if (className.startsWith("org/evidently"))
			return null;

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
}