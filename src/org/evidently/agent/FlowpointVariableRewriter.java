package org.evidently.agent;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.evidently.agent.FlowpointCollector.Slot;
import org.evidently.monitor.Pair;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class FlowpointVariableRewriter {

	private String clazz;
	private String currentMethod;
	private String currentPackage;
	private String currentMethodDesc;
	private String currentMethodSignature;
	private Stack<String> currentClass = new Stack<String>();
	private Set<Integer> currentMethodBoundaries;

	private FlowpointCollector flowpointCollector;
	private byte[] lastLoad = null;
	private ClassWriter cw;

	public FlowpointVariableRewriter(String clazz, FlowpointCollector flowpointCollector) {
		this.clazz = clazz;
		this.flowpointCollector = flowpointCollector;
		
		int i = clazz.lastIndexOf('/');
		currentPackage = clazz.substring(0, i).replaceAll("/", ".");
		currentClass.push(clazz.substring(i + 1));

	}
	
	public FlowpointVariableRewriter(String className, byte[] lastLoad, FlowpointCollector c) {
		this(className, c);
		this.lastLoad = lastLoad;
	}

	public void refreshMappings() {

		
	}

	public ClassWriter instrument() throws IOException {
		ClassReader cr =  null;

		if (lastLoad == null) {
			cr = new ClassReader(clazz);
		} else {
			cr = new ClassReader(lastLoad);
		}

		cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new FlowpointVariableRewriterClassVisitor(cw, clazz);
		cr.accept(cv, 0);
			
		return cw;
	}

	private int localOffset;
	
	public class FlowpointVariableRewriterClassVisitor extends ClassVisitor {
		private String className;
		

		public FlowpointVariableRewriterClassVisitor(ClassVisitor cv, String pClassName) {
			super(ASM5, cv);
			className = pClassName;

		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			System.out.println(
					String.format("[Evidently] Visiting inner class, name=%s,outerName=%s,innerName=%s,access=%d", name,
							outerName, innerName, access));

			currentClass.push(currentClass.peek() + "." + innerName);

			super.visitInnerClass(name, outerName, innerName, access);

			currentClass.pop();
		}

		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

			currentMethod = name;
			currentMethodDesc = desc;
			currentMethodSignature = signature;
			localOffset = 0;
			
			currentMethodBoundaries = flowpointCollector.methodBoundaries.poll();
			
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

	        FlowpointVariableRewriterMethodVisitor fvrmv = new FlowpointVariableRewriterMethodVisitor(mv, name, className);
			
	        fvrmv.aa = new AnalyzerAdapter(currentClass.peek(), access, name, desc, fvrmv);
			
	        fvrmv.lvs = new LocalVariablesSorter(access, desc, fvrmv.aa);
			
	        return fvrmv.lvs;
		}
	}

	public class FlowpointVariableRewriterMethodVisitor extends MethodVisitor {

		private LocalVariablesSorter lvs;
		private AnalyzerAdapter aa;
		private Set<Integer> initializedSlots = new HashSet<Integer>();

		public FlowpointVariableRewriterMethodVisitor(MethodVisitor mv, String name, String className) {
			this(ASM5, mv);
		}

		public FlowpointVariableRewriterMethodVisitor(int api, MethodVisitor mv) {
			super(api, mv);
		}		

		
		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			System.out.println(String.format(
					"[Evidently] [FPVRW] Visiting field instruction: opcode=%d,owner=%s,name=%s,desc=%s", opcode,
					owner, name, desc));

			super.visitFieldInsn(opcode, owner, name, desc);

			// a potential flowpoint -- also, we are only really interested in
			if (opcode == PUTFIELD || opcode == PUTSTATIC) {
			}

		}
		
		private void addTaintCheckToField(Slot s, int c)
		{
			int opcode = 0;

			if (c == PUTSTATIC) {
				opcode = GETSTATIC;
			} else if (c == PUTFIELD) {
				opcode = GETFIELD;
				
				mv.visitVarInsn(ALOAD, s.lastSlot);

			} else {
				return;
			}

			
			mv.visitFieldInsn(opcode, s.owner, s.name, s.desc);
			
			System.out.println("[Evidently] [FPVRW] Adding a taint check prior to assignment of: " + s.name);

			addTaintCheck(s);
			
			localOffset++;
			
		}
		
		private void addTaintCheck(Slot s){
			int idx = lvs.newLocal(Type.getObjectType("edu/columbia/cs/psl/phosphor/runtime/Taint"));
			mv.visitLocalVariable("evidentlyTaintCheck$" + idx, "Ledu/columbia/cs/psl/phosphor/runtime/Taint;", null,
					new Label(), new Label(), idx);

			mv.visitMethodInsn(INVOKESTATIC, "edu/columbia/cs/psl/phosphor/runtime/MultiTainter", "getTaint",
					String.format("(%s)Ledu/columbia/cs/psl/phosphor/runtime/Taint;", s.varType), false);

			mv.visitVarInsn(ASTORE, idx);

			if (Evidently.debug && false) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitVarInsn(ALOAD, idx);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);

			}

			// log the pretaint
			if (Evidently.debug) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
				mv.visitInsn(DUP);
				mv.visitLdcInsn("[Evidently] PRE Assignment Taint: ");
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
				mv.visitVarInsn(ALOAD, idx);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
						"(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
			}

		}
		
		private void addTaintCheckToLocal(Slot s, int opcode, int var)
		{

			mv.visitVarInsn(opcode - 33, var + localOffset); 

			System.out.println("[Evidently] [FPVRW] Adding a taint check prior to assignment of: " + s.v);

			addTaintCheck(s);
			
			//localOffset++;
			
		}

		@Override
		public void visitLineNumber(int line, Label start) {

			System.out.println("[Evidently] [FPVRW] Visit Line: " + line + " start=" + start);
			super.visitLineNumber(line, start);

			//int nextLine = flowpointCollector.nextLineNumber(line);
			
			//
			// The next line, even if it writes isn't in this method
			//
//			if(currentMethodBoundaries.contains(nextLine)==false){
//				return;
//			}
			
			//
			// If the current line is a field write AND
			// we won't get another chance to capture the write
			// we insert the write here
			//
//			{
//				// this line writes
//				if(flowpointCollector.lineNumberWrites(line)){
//					Triple<Integer, Integer, Slot> write = flowpointCollector.getWriteAtLineNumber(line);
//					
//					// it's a field
//					if(write.getThird()!=null){
//						
//						TreeSet<Integer> ts = new TreeSet<Integer>(currentMethodBoundaries);
//						
//						if(line == ts.first()){
//							System.out.println("[Evidently] [FPVRW] First Line Write at = " + line);
//							addTaintCheckToField(write.getThird(), write.getFirst());
//							
//							return;
//						}
//						
//					}
//
//				}
//			}

//			System.out.println("[Evidently] [FPVRW] Next Line= " + nextLine);
//			System.out.println("[Evidently] [FPVRW] Next Line Writes? " + flowpointCollector.nextLineNumberWrites(line));

			if (flowpointCollector.lineNumberWrites(line) == false) {
				return;
			}

			Triple<Integer, Integer, Slot> write = flowpointCollector.getWriteAtLineNumber(line);
			
			if(write.getThird()==null){ // it's a local
				Slot s = flowpointCollector.findSlot(currentPackage, currentClass.peek(), currentMethod, currentMethodDesc,
						currentMethodSignature, write.getSecond());

				if (s == null) {
					return;
				}
				
				addTaintCheckToLocal(s, write.getFirst(), write.getSecond());
				
			}else{                      // it's a field
				addTaintCheckToField(write.getThird(), write.getFirst());
			}
		}

		@Override
		public void visitCode() {
			// TODO Auto-generated method stub
			super.visitCode();
		}

		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
			// TODO Auto-generated method stub
			super.visitFrame(type, nLocal, local, nStack, stack);
		}
		
		

	}
	
	

}
