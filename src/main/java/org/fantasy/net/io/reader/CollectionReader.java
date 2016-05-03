package org.fantasy.net.io.reader;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fantasy.net.io.unsafe.ObjectInput;

public class CollectionReader extends AbstractReader<Collection<? extends Serializable>> {

	
	public Collection<? extends Serializable> read(ObjectInput oin) throws IOException, ClassNotFoundException {
		String className = oin.readUTF();
		int size = oin.readInt();
		Collection<Serializable> c = null;
		Class<?> clazz = Class.forName(className);
		if(!clazz.isInterface()) {
			try {
				Constructor cons = clazz.getConstructor(new Class[]{int.class});
				c = (Collection<Serializable>)cons.newInstance(size);
			} catch (Exception e) {
				try {
					c = (Collection<Serializable>) clazz.newInstance();
				} catch (Exception e1) {
				}
			}
		}
		
		if(c != null) {
			
		} else if(SortedSet.class.isAssignableFrom(clazz)) {
			c = new TreeSet<Serializable>();
		} else if(Set.class.isAssignableFrom(clazz)) {
			c = new HashSet<Serializable>();
		} else if(List.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)) {
			c = new ArrayList<Serializable>();
		} else {
			throw new InternalError("Unable to create an instance " + className);
		}
		
		for(int i = 0; i < size; i++)
			c.add((Serializable)oin.readObject());
		
		return c;
	}


	
}
