package org.evidently.agent;

public class MethodCall {

	private int opcode;
	private String owner;
	private String name;
	private String desc;
	private boolean itf;
	
	public MethodCall(int opcode, String owner, String name, String desc, boolean itf){
		this.setOpcode(opcode);
		this.setOwner(owner);
		this.setName(name);
		this.setDesc(desc);
		this.setItf(itf);
	}

	public int getOpcode() {
		return opcode;
	}

	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isItf() {
		return itf;
	}

	public void setItf(boolean itf) {
		this.itf = itf;
	}
}
