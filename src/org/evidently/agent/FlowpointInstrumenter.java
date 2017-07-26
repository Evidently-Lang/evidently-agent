package org.evidently.agent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.evidently.agent.FlowpointCollector.CollectFlowpointDetails;
import org.evidently.agent.FlowpointCollector.CollectMethodFlowpointDetails;
import org.evidently.agent.FlowpointCollector.Slot;
import org.evidently.agent.TypeUtils.Type;
import org.evidently.monitor.SecurityLabelManager;
import org.evidently.policy.PolicyElementType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import static org.objectweb.asm.Opcodes.*;
/**
 * Tagging of Flowpoints
 * 
 * How this works is as follows:
 * 
 * 1) Somewhere on the classpath we have a bunch of flowpoint classes loaded.
 * 
 * 2) They serve one purpose and one purpose only. To take IN the primitives we
 * can discover here and identify places we can stick a flowpoint.
 * 
 * 3) We try to identify as many of those places at this point as possible, and
 * rely on the runtime monitor to catch the rest.
 * 
 * 4) Fundamentally, flowpoints can exist in two places: - In the body of a
 * method - A field.
 * 
 * 5) ALL assignments need to be instrumented for reasons that are beyond the
 * scope of this comment block.
 */

public class FlowpointInstrumenter {

	private String clazz;
	private String currentMethod;
	private String currentPackage;
	private String currentMethodDesc;
	private String currentMethodSignature;

	private Stack<String> currentClass = new Stack<String>();
	private List<Flowpoint> flowpoints;
	private FlowpointCollector flowpointCollector;
	
	public FlowpointInstrumenter(String clazz, FlowpointCollector flowpointCollector, List<Flowpoint> flowpoints) {
		this.clazz = clazz;
		this.flowpoints = flowpoints;
		this.flowpointCollector = flowpointCollector;
		
		int i = clazz.lastIndexOf('/');
		currentPackage = clazz.substring(0, i).replaceAll("/", ".");
		currentClass.push(clazz.substring(i + 1));

	}
	
	public ClassWriter instrument() throws IOException {
		ClassReader cr = new ClassReader(clazz);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

		ClassVisitor cv = new FlowpointTaggingClassVisitor(cw, clazz);
		cr.accept(cv, 0);
				
		return cw;
	}


	public class FlowpointTaggingClassVisitor extends ClassVisitor {
		private String className;

		public FlowpointTaggingClassVisitor(ClassVisitor cv, String pClassName) {
			super(Opcodes.ASM5, cv);
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

			System.out.println(
					String.format("[Evidently] [TAGGING] Examining body of method for possible flowpoints: %s", name));

			currentMethod = name;
			currentMethodDesc = desc;
			currentMethodSignature = signature;

			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

			return new FlowpointTaggingMethodVisitor(mv, name, className);
		}
	}

	public class FlowpointTaggingMethodVisitor extends MethodVisitor {

		private MethodCall lastMethodCall;

		public FlowpointTaggingMethodVisitor(MethodVisitor mv, String name, String className) {
			this(Opcodes.ASM5, mv);
		}

		public FlowpointTaggingMethodVisitor(int api, MethodVisitor mv) {
			super(api, mv);
		}
		
		private void updateLocal(Slot s, String flowpointName){
			
			if(s==null) return;
			
			
			{
				if(s.isObject()){
					mv.visitVarInsn(ALOAD, s.slot);
				}else{
					mv.visitVarInsn(ILOAD, s.slot);					
				}
				
				int registers[] = new int[]{ICONST_0,ICONST_1,ICONST_2,ICONST_3,ICONST_4,ICONST_5};

				// build the sinks array 		
				if(s!=null && s.sinks!=null && s.sources!=null && s.sinks.size() > 0 && s.sources.size() > 0){
				
					if(s.sinks.size() > 6 || s.sources.size() > 6){
						throw new IllegalArgumentException("A label cannot be initailized with more that 6 members");
					}
					
					mv.visitTypeInsn(NEW, "org/evidently/monitor/Label");
					mv.visitInsn(DUP);

					mv.visitIntInsn(BIPUSH, s.sinks.size());
					mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
	
					for(int i=0; i< s.sinks.size(); i++){
						mv.visitInsn(DUP);
						mv.visitInsn(ICONST_0);
						mv.visitLdcInsn(s.sinks.get(i));
						mv.visitInsn(AASTORE);
					}
					

					// build the sources array
					mv.visitIntInsn(BIPUSH, s.sources.size());
					mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
	
					
					for(int i=0; i< s.sources.size(); i++){
						mv.visitInsn(DUP);
						mv.visitInsn(ICONST_0);
						mv.visitLdcInsn(s.sources.get(i));
						mv.visitInsn(AASTORE);
					}
					
					if(flowpointName!=null){
						mv.visitFieldInsn(GETSTATIC, "org/evidently/policy/PolicyElementType", "FLOWPOINT", "Lorg/evidently/policy/PolicyElementType;");
						mv.visitLdcInsn(flowpointName);
						mv.visitMethodInsn(INVOKESPECIAL, "org/evidently/monitor/Label", "<init>", "([Ljava/lang/String;[Ljava/lang/String;Lorg/evidently/policy/PolicyElementType;Ljava/lang/String;)V", false);
					}else{
						mv.visitMethodInsn(INVOKESPECIAL, "org/evidently/monitor/Label", "<init>", "([Ljava/lang/String;[Ljava/lang/String;)V", false);
					}
				}else{
					mv.visitInsn(ACONST_NULL);
				}
				
				mv.visitMethodInsn(INVOKESTATIC, "org/evidently/monitor/SecurityLabelManager", "update", typeToUpdateSignature(s.varType), false);
				
				if(s.isObject()==false && s!=null){
					mv.visitVarInsn(ISTORE, s.slot); // we have to write back.
				}
				
			}
				
		}
		
		

		@Override
		public void visitIincInsn(int var, int increment) {
			super.visitIincInsn(var, increment);
			
			updateLocal(var);
		}

		private void updateLocal(int var){
			// get the slot
			Slot s = flowpointCollector.findSlot(currentPackage, currentClass.peek(), currentMethod, currentMethodDesc, currentMethodSignature, var);
			
			// lets' see if this particular var instruction is part of a
			// flowpoint
			String flowPointName = null;
			
			for(Flowpoint fp : flowpoints){
				
				 String scope = currentPackage;
				 String type  = s.varType;
				 String _currentClass = currentClass.peek();
				 String _currentMethod = currentMethod;
				 String _name = s.v;
				 ASTType astType = ASTType.FIELD;
				 MethodCall _lastMethodCall = lastMethodCall;
				 
				 String fpn = fp.getFlowpointFor(scope, type, _currentClass, _currentMethod, _name, astType, _lastMethodCall);
				 
				 if(fpn!=null){
					 
					 if(flowPointName!=null){
						 throw new IllegalArgumentException("Flowpoint " + fpn + " is ambiguous. Please review flowpoint defintions");
					 }else{
						 flowPointName = fpn;
					 }
				 }
						 
			}

			updateLocal(s,  flowPointName);
		}
		

		public void updateField(Slot s, String flowPointName, int opcode, String owner, String name, String desc)
		{
			// need to know if field is static
			// need to know if is a int or a object
			{
				if(opcode==PUTSTATIC){
					mv.visitFieldInsn(GETSTATIC, owner, name, desc);
	
				}else{
					mv.visitVarInsn(Opcodes.ALOAD, lastSlot);
					mv.visitVarInsn(Opcodes.ALOAD, lastSlot);

					mv.visitFieldInsn(GETFIELD, owner, name, desc);					
				}
								
				int registers[] = new int[]{ICONST_0,ICONST_1,ICONST_2,ICONST_3,ICONST_4,ICONST_5};

				// build the sinks array 		
				if(s!=null && s.sinks!=null && s.sources!=null && s.sinks.size() > 0 && s.sources.size() > 0){
				
					if(s.sinks.size() > 6 || s.sources.size() > 6){
						throw new IllegalArgumentException("A label cannot be initailized with more that 6 members");
					}
					
					mv.visitTypeInsn(NEW, "org/evidently/monitor/Label");
					mv.visitInsn(DUP);

					mv.visitIntInsn(BIPUSH, s.sinks.size());
					mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
	
					for(int i=0; i< s.sinks.size(); i++){
						mv.visitInsn(DUP);
						mv.visitInsn(ICONST_0);
						mv.visitLdcInsn(s.sinks.get(i));
						mv.visitInsn(AASTORE);
					}
					

					// build the sources array
					mv.visitIntInsn(BIPUSH, s.sources.size());
					mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
	
					
					for(int i=0; i< s.sources.size(); i++){
						mv.visitInsn(DUP);
						mv.visitInsn(ICONST_0);
						mv.visitLdcInsn(s.sources.get(i));
						mv.visitInsn(AASTORE);
					}
					
					if(flowPointName!=null){
						mv.visitFieldInsn(GETSTATIC, "org/evidently/policy/PolicyElementType", "FLOWPOINT", "Lorg/evidently/policy/PolicyElementType;");
						mv.visitLdcInsn(flowPointName);
						mv.visitMethodInsn(INVOKESPECIAL, "org/evidently/monitor/Label", "<init>", "([Ljava/lang/String;[Ljava/lang/String;Lorg/evidently/policy/PolicyElementType;Ljava/lang/String;)V", false);
					}else{
						mv.visitMethodInsn(INVOKESPECIAL, "org/evidently/monitor/Label", "<init>", "([Ljava/lang/String;[Ljava/lang/String;)V", false);
					}
				}else{
					mv.visitInsn(ACONST_NULL);
				}
				
				mv.visitMethodInsn(INVOKESTATIC, "org/evidently/monitor/SecurityLabelManager", "update", typeToUpdateSignature(desc), false);
				
				if(TypeUtils.getType(desc) != Type.OBJECT){
					if(opcode==PUTSTATIC){
						mv.visitFieldInsn(PUTSTATIC, owner, name, desc);
		
					}else{
						mv.visitFieldInsn(PUTFIELD, owner, name, desc);					
					}
		
				}
				
			}

			
			
			
			// if it is not an object, we do a PUTFIELD to write it back.
		}
		
		private String typeToUpdateSignature(String t){
			//"(Ljava/lang/Object;Lorg/evidently/monitor/Label;)V"
			//"(CLorg/evidently/monitor/Label;)C"
			//"(ILorg/evidently/monitor/Label;)I"
			//([ILorg/evidently/monitor/Label;)[I

			if(TypeUtils.getType(t)==Type.OBJECT){
				return "(Ljava/lang/Object;Lorg/evidently/monitor/Label;)V";
			}
			
			return String.format("(%sLorg/evidently/monitor/Label;)%s", t, t);
		}
		
		private void updateField(int opcode, String owner, String name, String desc){
			
			Slot s = flowpointCollector.findSlot(currentPackage, currentClass.peek(), name);

			String flowPointName = null;

			String scope = currentPackage;
			String type = desc;
			String _currentClass = currentClass.peek();
			String _currentMethod = currentMethod;
			String _name = name;
			ASTType astType = ASTType.FIELD;
			MethodCall _lastMethodCall = lastMethodCall;

			for (Flowpoint fp : flowpoints) {
				String fpn = fp.getFlowpointFor(scope, type, _currentClass, _currentMethod, _name, astType,
						_lastMethodCall);

				if (fpn != null) {
					if (flowPointName != null) {
						throw new IllegalArgumentException(
								"Flowpoint " + fpn + " is ambiguous. Please review flowpoint defintions");
					} else {
						flowPointName = fpn;
					}
				}

			}

			updateField(s, flowPointName, opcode, owner, name, desc);

		}
		private int lastSlot;
		
		@Override
		public void visitVarInsn(int opcode, int var) {

			System.out.println(String.format("[Evidently] [TAGGING] Visiting variable instruction: opcode=%d,slot=%d",
					opcode, var));		
			lastSlot = var;

			if (var == 0) {
				System.out.println(
						"[Evidently] [TAGGING] Ignoring THIS reference (because it's probably a field reference)");
				
				super.visitVarInsn(opcode, var);
				
			} else {
				super.visitVarInsn(opcode, var);
				updateLocal(var);
			}
			
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			System.out.println(String.format(
					"[Evidently] [TAGGING] Visiting method invocation: opcode=%d,owner=%s,name=%s,desc=%s", opcode,
					owner, name, desc));
			lastMethodCall = new MethodCall(opcode, owner, name, desc, itf);
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
			System.out.println(String.format(
					"[Evidently] [TAGGING] Visiting method invocation (invoke dynamic): name=%s,desc=%s,handle=%s",
					name, desc, bsm.toString()));

			super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			System.out.println(String.format(
					"[Evidently] [TAGGING] Visiting Local Variable Decl, name=%s,desc=%s,signature=%s,index=%d", name,
					desc, signature, index));

			super.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			System.out.println(String.format(
					"[Evidently] [TAGGING] Visiting field instruction: opcode=%d,owner=%s,name=%s,desc=%s", opcode,
					owner, name, desc));

			
			super.visitFieldInsn(opcode, owner, name, desc);

			// a potential flowpoint -- also, we are only really interested in 
			if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
				updateField(opcode, owner, name, desc);
			}

		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start,
				Label[] end, int[] index, String desc, boolean visible) {

			return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
		}

	}

	}
