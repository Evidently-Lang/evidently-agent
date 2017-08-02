package example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.evidently.agent.Flowpoint;
import org.evidently.agent.FlowpointCollector;
import org.evidently.agent.FlowpointInstrumenter;
import org.evidently.agent.FlowpointVariableRewriter;
import org.objectweb.asm.ClassWriter;

public class ASMTest4 {

	public static void main(String[] args) throws IOException {

		String clazz = "example/SomeClass";

		// first transformation
		ClassWriter cw = null;
//		{
//			FlowpointCollector coll = new FlowpointCollector(clazz);
//			coll.collectFlowpoints();
//
//			FlowpointVariableRewriter c = new FlowpointVariableRewriter(clazz, coll);
//
//			cw = c.instrument();
//
//			// first we scan over
//			new File("bin-debug/example/").mkdirs();
//			FileOutputStream fos = new FileOutputStream("bin-debug/example/SomeClass-pass1.class");
//			fos.write(cw.toByteArray());
//			fos.close();
//		}

		// second transformation (adds the update statements)
//		{
//			FlowpointCollector coll = new FlowpointCollector(clazz, cw.toByteArray());
//			coll.collectFlowpoints();
//
//			FlowpointInstrumenter c = new FlowpointInstrumenter(clazz, cw.toByteArray(), coll,
//					new ArrayList<Class<? extends Flowpoint>>());
//
//			cw = c.instrument();
//
//			// first we scan over
//			new File("bin-debug/example/").mkdirs();
//			FileOutputStream fos = new FileOutputStream("bin-debug/example/SomeClass-pass2.class");
//			fos.write(cw.toByteArray());
//			fos.close();
//
//		}

		// second transformation (adds the update statements)
		{
			FlowpointCollector coll = new FlowpointCollector(clazz);
			coll.collectFlowpoints();

			FlowpointInstrumenter c = new FlowpointInstrumenter(clazz, coll,
					new ArrayList<Class<? extends Flowpoint>>());

			cw = c.instrument();

			// first we scan over
			new File("bin-debug/example/").mkdirs();
			FileOutputStream fos = new FileOutputStream("bin-debug/example/SomeClass-pass2.class");
			fos.write(cw.toByteArray());
			fos.close();

		}

		// write the final thing out
		new File("bin-debug/example/").mkdirs();
		FileOutputStream fos = new FileOutputStream("bin-debug/example/SomeClass.class");
		fos.write(cw.toByteArray());
		fos.close();

	}

}
