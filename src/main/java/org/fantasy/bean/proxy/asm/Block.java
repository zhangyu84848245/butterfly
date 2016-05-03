package org.fantasy.bean.proxy.asm;

import org.objectweb.asm.Label;

public class Block {

	private Label start = new Label();
	private Label end = new Label();
	private Label handler = new Label();
	private MethodMetadataWriter mg;
	
	public Block(MethodMetadataWriter mg) {
		this.mg = mg;
	}

	public Label getStart() {
		return start;
	}

	public Label getEnd() {
		return end;
	}

	public Label getHandler() {
		return handler;
	}
	
	public void start() {
		mg.mark(start);
	}
	
	public void end() {
		mg.mark(end);
	}
	
	public void handler() {
		mg.mark(handler);
	}
	
	
//	public void end() {
//		if (end != null) {
//			throw new IllegalStateException("end of label already set");
//		}
//		this.end = mg.createAndVisitLabel();
//	}
//	
//	public Label getStart() {
//		return start;
//	}
//	public Label getEnd() {
//		return end;
//	}
//	public MethodGenerator getMethodGenerator() {
//		return mg;
//	}
	
	
	
}
