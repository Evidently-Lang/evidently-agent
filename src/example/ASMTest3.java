package example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.evidently.agent.FlowpointCollector;
import org.evidently.agent.FlowpointInstrumenter;
import org.evidently.agent.FlowpointVariableRewriter;
import org.objectweb.asm.ClassWriter;

public class ASMTest3 {

	public static void main(String[] args) throws IOException {

		String clazz = "example/SomeClass";
		String cls   = "example.SomeClass";
		
		FlowpointCollector coll = new FlowpointCollector(clazz);
		coll.collectFlowpoints();
		
		FlowpointVariableRewriter c = new FlowpointVariableRewriter(clazz, coll);
		
		ClassWriter cw = c.instrument();
		
		// first we scan over 
		new File("bin-debug/example/").mkdirs();
		FileOutputStream fos = new FileOutputStream("bin-debug/example/SomeClass.class");
		fos.write(cw.toByteArray());
		fos.close();

		ASMTest.loadClass(cw.toByteArray(), cls);

		//NumberGuesser sc = new NumberGuesser();

		//sc.main(null);
		
	}

}
