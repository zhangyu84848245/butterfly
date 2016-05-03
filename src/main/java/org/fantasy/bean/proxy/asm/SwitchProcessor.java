package org.fantasy.bean.proxy.asm;

import org.objectweb.asm.Label;

public interface SwitchProcessor {
	
	public void processCase(int index, Label switchLabel);
	
	public void processDefault(Label switchLabel);
	
}
