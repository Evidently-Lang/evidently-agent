package org.evidently.agent;

import java.io.IOException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import example.ASMTest.EvidentlyInst;
import example.ASMTest.PrintMessageMethodVisitor;

public class FlowpointCollector {

	private String clazz;
	public static String f;
	public FlowpointCollector(String clazz) {
		this.clazz = clazz;
	}

	// should probably get a list of flowpoint definitions
	public void collectFlowpoints() throws IOException {
		ClassReader cr = new ClassReader(clazz);
		// ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

		ClassVisitor cv = new CollectFlowpointDetails(null, clazz);
		cr.accept(cv, 0);

	}

	/**
	 * Tagging of Flowpoints
	 * 
	 * How this works is as follows:
	 * 
	 * 1) Somewhere on the classpath we have a bunch of flowpoint classes
	 * loaded. 2) They serve one purpose and one purpose only. To take IN the
	 * primatives we can discover here and identify places we can stick a
	 * flowpoint. 3) We try to identify as many of those places at this point as
	 * possible, and rely on the runtime monitor to catch the rest. 4)
	 * Fundementally, flowpoints can exist in two places: - In the body of a
	 * method - A field. 5) ALL assignments need to be instrumeted for reasons
	 * that are beyond the scope of this comment block.
	 */

	public class CollectFlowpointDetails extends ClassVisitor {
		private String className;

		public CollectFlowpointDetails(ClassVisitor cv, String pClassName) {
			super(Opcodes.ASM5, cv);
			className = pClassName;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

			System.out.println(String.format("[Evidently] Examining body of method for possible flowpoints: %s", name));

			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

			return new CollectMethodFlowpointDetails(mv, name, className);
		}
	}

	public class CollectMethodFlowpointDetails extends MethodVisitor {
		
		private MethodCall lastMethodCall;

		public CollectMethodFlowpointDetails(MethodVisitor mv, String name, String className) {
			this(Opcodes.ASM5, mv);
		}

		public CollectMethodFlowpointDetails(int api, MethodVisitor mv) {
			super(api, mv);
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			
			System.out.println(String.format("[Evidently] Visiting variable instruction: opcode=%d,slot=%d", opcode, var));
			
			if(var==0){
				System.out.println("[Evidently] Ignoring THIS reference (because it's probably a field reference)");
			}
			
			super.visitVarInsn(opcode, var);
		}

//		@Override
//		public void visitParameter(String name, int access) {
//			// TODO Auto-generated method stub
//			super.visitParameter(name, access);
//		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			System.out.println(String.format("[Evidently] Visiting method invocation: opcode=%d,owner=%s,name=%s,desc=%s", opcode, owner,name,desc));
			lastMethodCall = new MethodCall(opcode, owner, name, desc, itf);
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
			System.out.println(String.format("[Evidently] Visiting method invocation (invoke dynamic): name=%s,desc=%s,handle=%s", name,desc, bsm.toString()));

			
			super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			System.out.println(String.format("[Evidently] Visiting Local Variable Decl, name=%s,desc=%s,signature=%s,index=%d", name, desc, signature, index));

			super.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			System.out.println(String.format("[Evidently] Visiting field instruction: opcode=%d,owner=%s,name=%s,desc=%s", opcode, owner, name, desc));

			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start,
				Label[] end, int[] index, String desc, boolean visible) {
			return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
		}
		
		

	}

}
