package org.fantasy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.fantasy.common.Convertor;
import org.fantasy.common.Filter;
import org.fantasy.common.TypeResolver;


public class CollectionUtils {

	public static <O, T> List<T> convert(Collection<O> collection, Convertor<O, T> convertor) {
		if(collection == null)
			return (List<T>)Collections.EMPTY_LIST;
		if(convertor == null)
			throw new IllegalArgumentException("Convertor is illegal.");
		List<T> result = new ArrayList<T>();
		for(Iterator<O> iterator = collection.iterator();iterator.hasNext();) {
			O o = iterator.next();
			result.add(convertor.convert(o));
		}
		return result;
	}
	
	
	public static <O> List<O> filter(List<O> c, Filter<O> filter) {
		for(Iterator<O> iterator = c.iterator();iterator.hasNext();) {
			O o = iterator.next();
			if(filter.accept(o))
				iterator.remove();
		}
		return c;
	}

}
