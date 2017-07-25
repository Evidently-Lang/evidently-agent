package example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.evidently.agent.Flowpoint;
import org.evidently.agent.FlowpointCollector;
import org.evidently.agent.FlowpointInstrumenter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMTest {

	public static void printLocalString(String s) {
		System.out.println(">>>>>>>>" + s);
	}

	public class EvidentlyInst extends ClassVisitor {
		private String className;

		public EvidentlyInst(ClassVisitor cv, String pClassName) {
			super(Opcodes.ASM5, cv);
			className = pClassName;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			
			if(name.equals("testLocals")){
				MethodVisitor mv = super.visitMethod(access, name, desc, signature,
			            exceptions);
				
				System.out.println("Visiting Method..." + name);
				
				return new PrintMessageMethodVisitor(mv, name, className);
			}else{
				return super.visitMethod(access, name, desc, signature,
			            exceptions);
			}
			
			
		}
	}

	public class PrintMessageMethodVisitor extends MethodVisitor {

		MethodVisitor v;
		
		public PrintMessageMethodVisitor(MethodVisitor mv, String name, String className) {
			this(Opcodes.ASM5, mv);
			
			v = mv;
		}

		public PrintMessageMethodVisitor(int api, MethodVisitor mv) {
			super(api, mv);
			
			v = mv;
		}
		
		

		@Override
		public void visitVarInsn(int opcode, int var) {
			System.out.println("VARIXN " + var);
			super.visitVarInsn(opcode, var);
			
			
			v.visitVarInsn(Opcodes.ALOAD, 1);
			v.visitMethodInsn(Opcodes.INVOKESTATIC, "example/ASMTest", "printLocalString", "(Ljava/lang/String;)V", false);

		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			// TODO Auto-generated method stub
			
			
			
			System.out.println("Local Var: " + desc);
			
			if(desc.equals("Ljava/lang/String;")){
				
				System.out.println("Annotating...");
				// define the variable
				v.visitLocalVariable(name, desc, signature, start, end, index);
				
				// add the method call 
//				v.visitVarInsn(Opcodes.ALOAD, 1);
//				v.visitMethodInsn(Opcodes.INVOKESTATIC, "example/ASMTest",
//					"printLocalString", "(Ljava/lang/String;)V", false);
//				

				
			}else{			
				v.visitLocalVariable(name, desc, signature, start, end, index);
			}
			
			
		}

		@Override
		  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		    return super.visitAnnotation(desc, visible);
		  }
		                                                     
		  @Override
		  public void visitCode() {
			  System.out.println("CODEEEE");
		  }	
		}
	
	public void test1() throws IOException {

		//final ClassReader reader = new ClassReader("example/SomeClass");
		// final ClassNode classNode = new ClassNode();
		// reader.accept(classNode, 0);
		//
		//final EvidentlyInst v = new EvidentlyInst(Opcodes.ASM5);
		//reader.accept(v, 0);

		// for(final MethodNode mn : (List<MethodNode>)classNode.methods) {
		// if(mn.name.equalsIgnoreCase("testLocals")) {
		// final InsnList list = new InsnList();
		//
		// for(final LocalVariableNode local :
		// (List<LocalVariableNode>)mn.localVariables) {
		// System.out.println("Local Variable: " + local.name + " : " +
		// local.desc + " : " + local.signature + " : " + local.index);
		// if(local.desc.contains("String")) {
		//
		// mn.visitVarInsn(Opcodes.ALOAD, 1);
		// mn.visitMethodInsn(Opcodes.INVOKESTATIC, "example/ASMTest",
		// "printLocalString", "(Ljava/lang/String;)V", false);
		//
		// //mn.visitVarInsn(Opcodes.ALOAD, local.index);
		//// mn.visitVarInsn(Opcodes.ALOAD, 1);
		////
		////
		//// mn.visitMethodInsn(Opcodes.INVOKESTATIC,
		//// "example/ASMTest", "printLocalString",
		//// "(Ljava/lang/String;)V", false);
		//////
		//// final VarInsnNode node = new VarInsnNode(Opcodes.ALOAD, 1);
		////
		//// list.add(node);
		//// System.out.println("added local var '" + local.name + "'");
		////
		//// final MethodInsnNode insertion = new
		// MethodInsnNode(Opcodes.INVOKESTATIC, "example/ASMTest",
		// "printLocalString", "(Ljava/lang/String;)V");
		//// //list.add(insertion);
		//// mn.instructions.add(list);
		//
		//
		//
		// }
		// }
		//
		//
		//
		// }
		// }

		ClassReader cr = new ClassReader("example/SomeClass");
	    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
	    
	    ClassVisitor cv = new EvidentlyInst(cw, "example/SomeClass");
	    cr.accept(cv, 0);
	    
		//ClassWriter writer = new ClassWriter(0);

		//reader.accept(writer, 0);
		// classNode.accept(writer);

		FileOutputStream fos = new FileOutputStream("test/SomeClass.class");
		fos.write(cw.toByteArray());
		fos.close();

		loadClass(cw.toByteArray(), "example.SomeClass");
		//SomeClass.testLocals(true);

	}

	private static Class loadClass(byte[] b, String name) {
		// override classDefine (as it is protected) and define the class.
		Class clazz = null;
		try {
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			Class cls = Class.forName("java.lang.ClassLoader");
			java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass",
					new Class[] { String.class, byte[].class, int.class, int.class });

			// protected method invocaton
			method.setAccessible(true);
			try {
				Object[] args = new Object[] { name, b, new Integer(0), new Integer(b.length) };
				clazz = (Class) method.invoke(loader, args);
			} finally {
				method.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return clazz;
	}

	public static void main(String args[]) throws IOException {
		//ASMTest t = new ASMTest();

		//t.test1();
		
		// collect the metadata for flowpoints
		FlowpointCollector c = new FlowpointCollector("example/SomeClass");
		c.collectFlowpoints();
		
		// instrement the code 		
		FlowpointInstrumenter fpi = new FlowpointInstrumenter("example/SomeClass", c.getLocalSlots(), new ArrayList<Flowpoint>());

		ClassWriter cw = fpi.instrument();
		
		FileOutputStream fos = new FileOutputStream("test/SomeClass.class");
		fos.write(cw.toByteArray());
		fos.close();

		loadClass(cw.toByteArray(), "example.SomeClass");

		SomeClass sc = new SomeClass();
		sc.doStuff2(3);

	}

}
