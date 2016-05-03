package org.fantasy.bean.proxy.asm;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class LocalVariablesSorter extends MethodVisitor {

	
	private static class State {
		int[] mapping = new int[40];
		int nextLocal;
	}
	
	protected final int firstLocal;
	private final State state;
	
	/**
	 * 
	 * @param access
	 * @param desc
	 * @param mv
	 */
	public LocalVariablesSorter(int access,  Type[] argsTypes, MethodVisitor mv) {
		super(Opcodes.ASM5, mv);
		state = new State();
		// 静态的方法从第一个参数开始
		// 普通方法从第二参数开始, 第一个参数是实例对象的引用
		/**
		 * 文档如下:
		 * Local Variables
		 * Each frame (§2.6) contains an array of variables known as its local variables. 
		 * The length of the local variable array of a frame is determined at compile-time and supplied in the binary representation of a class or interface along with the code for the method associated with the frame (§4.7.3).
		 * A single local variable can hold a value of type boolean, byte, char, short, int, float, reference, or returnAddress. A pair of local variables can hold a value of type long or double.
		 * Local variables are addressed by indexing. The index of the first local variable is zero. An integer is considered to be an index into the local variable array if and only if that integer is between zero and one less than the size of the local variable array.
		 * A value of type long or type double occupies two consecutive local variables. Such a value may only be addressed using the lesser index. For example, a value of type double stored in the local variable array at index n actually occupies the local variables with indices n and n+1; however, the local variable at index n+1 cannot be loaded from. It can be stored into. However, doing so invalidates the contents of local variable n.
		 * The Java Virtual Machine does not require n to be even. In intuitive terms, values of types long and double need not be 64-bit aligned in the local variables array. Implementors are free to decide the appropriate way to represent such values using the two local variables reserved for the value.
		 *************************************
		 * The Java Virtual Machine uses local variables to pass parameters on method invocation. 
		 * On class method invocation, any parameters are passed in consecutive local variables starting from local variable 0. 
		 * On instance method invocation, local variable 0 is always used to pass a reference to the object on which the instance method is being invoked (this in the Java programming language). 
		 * Any parameters are subsequently passed in consecutive local variables starting from local variable 1.
		 *************************************
		 */
		state.nextLocal = (Opcodes.ACC_STATIC & access) != 0 ? 0 : 1;
		
		for(int i = 0; i < argsTypes.length; i++) {
			state.nextLocal += argsTypes[i].getSize();
		}
		firstLocal = state.nextLocal;
	}
	
	
	public int newLocal(final int size) {
		int var = state.nextLocal;
		state.nextLocal += size;
		return var;
	}
}
