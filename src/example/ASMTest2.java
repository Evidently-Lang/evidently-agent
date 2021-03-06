package example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.evidently.agent.Flowpoint;
import org.evidently.agent.FlowpointCollector;
import org.evidently.agent.FlowpointInstrumenter;
import org.evidently.monitor.aspects.AspectConfig;
import org.objectweb.asm.ClassWriter;

public class ASMTest2 {

	public static void main(String[] args) throws IOException {
		{
		
			String clazz = "example/SomeClass";

			FlowpointCollector coll = new FlowpointCollector(clazz);
			coll.collectFlowpoints();

			FlowpointInstrumenter c = new FlowpointInstrumenter(clazz, coll,
					new ArrayList<Class<? extends Flowpoint>>());

			ClassWriter cw = c.instrument();

			// first we scan over
			new File("bin-debug/example/").mkdirs();
			FileOutputStream fos = new FileOutputStream("bin-debug/example/SomeClass.class");
			fos.write(cw.toByteArray());
			fos.close();

		}
		
	}

}
