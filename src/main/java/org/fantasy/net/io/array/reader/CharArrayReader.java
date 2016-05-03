package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class CharArrayReader implements ArrayReader<Character> {

	public Character[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Character[] charArray = new Character[length];
		for(int i = 0; i < length; i++)
			charArray[i] = oin.readChar();
		return charArray;
	}

}
