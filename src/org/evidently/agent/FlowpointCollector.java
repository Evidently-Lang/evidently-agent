package org.evidently.agent;

import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.SASTORE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.evidently.agent.TypeUtils.Type;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class FlowpointCollector {

	private String clazz;
	public static String f;
	private String currentPackage;
	private Stack<String> currentClass = new Stack<String>();
	private String currentMethod;
	private String currentMethodDesc;
	private String currentMethodSignature;
	private int lastLineNumber;
	private Map<Integer,Triple<Integer,Integer,Slot>> writes = new HashMap<Integer,Triple<Integer,Integer,Slot>>();
	public Queue<Set<Integer>> methodBoundaries = new LinkedList<Set<Integer>>();
	private Set<Integer> currentMethodLines = new HashSet<Integer>();
	
	// for keeping track of the slots of locals	
	class Slot {
		public String p;       // package
		public String c;       // class
		public String m;       // method
		public String v;       // variable name
		public String varType; // the type
		public int slot;       // the slot in the instruction set
		public ASTType kind;   // what kind of thing this is (local or field)
		
		private String methodDesc;
		private String methodSignature;

		public List<String> sinks;
		public List<String> sources;
		
		public String owner;
		public String name;
		public String desc;
		public int lastSlot;
		
		public Slot(String p, String c, String m, String desc, String v, String methodDesc, String methodSignature, int slot){
			this.p = p;
			this.c = c;
			this.m = m;
			this.v = v;
			this.slot = slot;
			this.varType = desc;
			this.kind = ASTType.LOCAL;
			this.methodDesc = methodDesc;
			this.methodSignature = methodSignature;
		}
		
		public Slot(String p, String c, String v, String desc){
			this.p = p;
			this.c = c;
			this.v = v;
			this.varType = desc;
			this.kind = ASTType.FIELD;
		}
		
		public void updateSinks(List<String> s){
			sinks = new ArrayList<String>();
			sinks.addAll(s);
		}
		
		public void updateSources(List<String> s){
			sources = new ArrayList<String>();
			sources.addAll(s);
		}

		public boolean isObject(){
			Type t  = TypeUtils.getType(varType);			
			return t==Type.OBJECT;
		}

	}
	
	private List<Slot> localSlots = new ArrayList<Slot>();
	
	public Slot findSlot(String p, String c, String m, String cmd, String cms, int slot) {

		List<Slot> slots = localSlots;
		
		for (Slot s : slots) {
			if (
					(s.p!=null && s.p.equals(p)) 
					&& (s.c!=null && s.c.equals(c)) 
					&& (s.m!=null && s.m.equals(m)) 
					&& slot == s.slot
					&& (s.methodDesc!=null && s.methodDesc.equals(cmd))
					&& (!(s.methodSignature!=null) || s.methodDesc.equals(cms)) // if it's not null, do the check

					) {
				return s;
			}
		}
		
		return null;
	}
	
	public int nextLineNumber(int line)
	{
		int nextLineNumber = line;
		
		Set<Integer> keys = new TreeSet<Integer>(writes.keySet());
		for(int l : keys){
			if(l > nextLineNumber){
				nextLineNumber = l;
				break;
			}
		}
		
		if(line==nextLineNumber){
			return -1;
		}
		
		return nextLineNumber;
	}
	public boolean lineNumberWrites(int line){
		
		if(line==-1){
			return false;
		}
		
		Triple<Integer,Integer,Slot> write = getWriteAtLineNumber(line);		
		return write != null;		

	}
	public boolean nextLineNumberWrites(int currentLine){
		
		int nextLine = nextLineNumber(currentLine);
		
		return lineNumberWrites(nextLine);	
	}

	public Triple<Integer,Integer,Slot> getWriteAtLineNumber(int line){
		
		if(writes.get(line)==null){
			return null;
		}
		
		return writes.get(line);
	}

	
	public Slot findSlot(String p, String c, String name) {

		List<Slot> slots = localSlots;		
		
		for (Slot s : slots) {
			if (s.p.equals(p) && s.c.equals(c) && s.v.equals(name) && ASTType.FIELD == s.kind) {
				return s;
			}
		}

		return null;
	}

	private byte[] lastLoad;
	public FlowpointCollector(String clazz) {
		this.clazz = clazz;

		int i = clazz.lastIndexOf('/');
		currentPackage = clazz.substring(0, i).replaceAll("/", ".");
		currentClass.push(clazz.substring(i+1));		
	}

	public FlowpointCollector(String className, byte[] lastLoad) {
		this(className);
		this.lastLoad = lastLoad;
	}


	// should probably get a list of flowpoint definitions
	public void collectFlowpoints() throws IOException {
		ClassReader cr = null;
		
		if(lastLoad==null){
			cr = new ClassReader(clazz);
		}else{
			cr = new ClassReader(lastLoad);
		}
		// ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

		ClassVisitor cv = new CollectFlowpointDetails(null, clazz);
		cr.accept(cv, 0);

	}

	public List<Slot> getLocalSlots() {
		return localSlots;
	}

	public void setLocalSlots(List<Slot> localSlots) {
		this.localSlots = localSlots;
	}

	
	public class CollectFlowpointDetails extends ClassVisitor {
		private String className;

		public CollectFlowpointDetails(ClassVisitor cv, String pClassName) {
			super(Opcodes.ASM5, cv);
			className = pClassName;
		}

		
		@Override
		public void visitOuterClass(String owner, String name, String desc) {
			System.out.println(String.format("[Evidently] [FPC] Visiting outer class, owner=%s,name=%s,desc=%s", owner, name, desc));
			super.visitOuterClass(owner, name, desc);
		}


		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			System.out.println(String.format("[Evidently] [FPC] Visiting inner class, name=%s,outerName=%s,innerName=%s,access=%d", name, outerName,innerName,access));
			
			currentClass.push(currentClass.peek() + "." + innerName);
			
			super.visitInnerClass(name, outerName, innerName, access);
			
			currentClass.pop();
		}

		

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			
			getLocalSlots().add(new Slot(currentPackage, currentClass.peek(), name, desc));
			
			// register this field 
			return new FieldVisitor(Opcodes.ASM5){

				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

					if(desc.equals("Lorg/evidently/annotations/Sink;") || desc.equals("Lorg/evidently/annotations/Source;")){

						// fish out the arguments
						final List<String> args = new ArrayList<String>();
						final String annotationKind = (desc.equals("Lorg/evidently/annotations/Sink;")) ? "SINK" : "SOURCE";
						
						return new AnnotationVisitor(Opcodes.ASM5){
							@Override
							public AnnotationVisitor visitArray(String name) {
								
								return new AnnotationVisitor(Opcodes.ASM5){

									@Override
									public void visit(String name, Object value) {
										args.add((String)value); // oh, believe me, it's a String. 
										super.visit(name, value);
									}
									
								};
							}
							
							@Override
							public void visitEnd()
							{
								// save the annotation in the last declared local variable slot
								if(annotationKind.equals("SINK")){
									localSlots.get(localSlots.size()-1).updateSinks(args);
								}else{
									localSlots.get(localSlots.size()-1).updateSources(args);							
								}
							}
						};

					}else{
						return super.visitAnnotation(desc, visible);
					}
				}				
			};			
		}


		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return super.visitAnnotation(desc, visible);
		}


		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

			System.out.println(String.format("[Evidently] [FPC] visitMethod access=%d,name=%s,desc=%s,signature=%s" , access, name, desc, signature));

			
			currentMethod = name;
			currentMethodDesc = desc;
			currentMethodSignature = signature;
			
			currentMethodLines.clear();
			
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

		private int lastSlot = 0;
		private Set<Integer> initializedSlots = new HashSet<Integer>();
		private int localOffset;
		
		@Override
		public void visitVarInsn(int opcode, int var) {
			lastSlot = var;
			System.out.println(String.format("[Evidently] [FPC] Visiting variable instruction: opcode=%d,slot=%d", opcode, var));
			
			if(var==0){
				System.out.println("[Evidently] [FPC] Ignoring THIS reference (because it's probably a field reference)");
			}
			
			// is it a write?
			if(opcode >= ISTORE && opcode <= SASTORE){
				
				if(!initializedSlots.contains(var)){
					initializedSlots.add(var);
				}else{
					writes.put(lastLineNumber, new Triple<Integer,Integer,Slot>(opcode,var,null));
					localOffset++;
				}
			}
			
			super.visitVarInsn(opcode, var);
		}
		
		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			
			if(desc!=null && desc.startsWith("Lorg/aspectj")){
				super.visitFieldInsn(opcode, owner, name, desc);	
				return;
			}
			
			System.out.println(String.format(
					"[Evidently] [FPC] Visiting field instruction: opcode=%d,owner=%s,name=%s,desc=%s", opcode,
					owner, name, desc));

			super.visitFieldInsn(opcode, owner, name, desc);

			if (opcode == PUTFIELD || opcode == PUTSTATIC) {				
				Slot s = findSlot(currentPackage, currentClass.peek(), name);
				
				s.owner = owner;
				s.name = name;
				s.desc = desc;
				s.lastSlot = lastSlot;
				
				writes.put(lastLineNumber, new Triple<Integer,Integer,Slot>(opcode,-1, s));
				localOffset++;
			}
		}


		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			
			if(owner!=null && owner.startsWith("org/aspectj") || (name!=null && (name.startsWith("ajc$") || name.startsWith("aspectOf")))){
				super.visitMethodInsn(opcode, owner, name, desc, itf);
				return;
			}
		
			
			System.out.println(String.format("[Evidently] [FPC] Visiting method invocation: opcode=%d,owner=%s,name=%s,desc=%s", opcode, owner,name,desc));
			lastMethodCall = new MethodCall(opcode, owner, name, desc, itf);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
			System.out.println(String.format("[Evidently] [FPC] Visiting method invocation (invoke dynamic): name=%s,desc=%s,handle=%s", name,desc, bsm.toString()));			
			super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}
		
		

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			System.out.println(String.format("[Evidently] [FPC] Visiting Local Variable Decl, name=%s,desc=%s,signature=%s,index=%d", name, desc, signature, index));

			getLocalSlots().add(new Slot(currentPackage, currentClass.peek(), currentMethod, desc, name, currentMethodDesc, currentMethodSignature, index));
					
			super.visitLocalVariable(name, desc, signature, start, end, index);
		}


		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start,
				Label[] end, int[] index, String desc, boolean visible) {			
			
			System.out.println(String.format("[Evidently] [FPC] Visiting local variable annotation: desc=%s ", desc));

			// we are interested in sink and source annotations
			if(desc.equals("Lorg/evidently/annotations/Sink;") || desc.equals("Lorg/evidently/annotations/Source;")){

				// fish out the arguments
				final List<String> args = new ArrayList<String>();
				final String annotationKind = (desc.equals("Lorg/evidently/annotations/Sink;")) ? "SINK" : "SOURCE";
				
				return new AnnotationVisitor(Opcodes.ASM5){
					@Override
					public AnnotationVisitor visitArray(String name) {
						
						return new AnnotationVisitor(Opcodes.ASM5){

							@Override
							public void visit(String name, Object value) {
								args.add((String)value); // oh, believe me, it's a String. 
								super.visit(name, value);
							}
							
						};
					}
					
					@Override
					public void visitEnd()
					{
						// save the annotation in the last declared local variable slot
						if(annotationKind.equals("SINK")){
							localSlots.get(localSlots.size()-1).updateSinks(args);
						}else{
							localSlots.get(localSlots.size()-1).updateSources(args);							
						}
					}
				};
			}
			return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
		}

		@Override
		public void visitLineNumber(int line, Label start) {
			lastLineNumber = line;
			currentMethodLines.add(line);
			
			writes.put(lastLineNumber, null);
			super.visitLineNumber(line, start);
		}

		@Override
		public void visitEnd() {
			super.visitEnd();			
			methodBoundaries.add(new HashSet<Integer>(currentMethodLines));			
		}
		
		

	}

}
