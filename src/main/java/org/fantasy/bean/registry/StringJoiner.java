package org.fantasy.bean.registry;

import java.util.Iterator;

import org.fantasy.util.Constant;



public class StringJoiner implements Joiner<String> {
	
	private StringBuilder builder = new StringBuilder();
	public static final char SLASH = '/';

	public StringJoiner() {
	}
	
	public StringJoiner(boolean added) {
		builder.append(Constant.ZOOKEEPER_REGISTRY_ROOT);
	}

	public Joiner<String> join(Iterable<String> iterable) {
		for(Iterator<String> iterator = iterable.iterator();iterator.hasNext();) {
			builder.append(iterator.next()).append(SLASH);
		}
		return this;
	}


	public Joiner<String> join(String key) {
		builder.append(key);
		return this;
	}

	public String toString() {
		return builder.toString();
	}

	public Joiner<String> join(Joiner<String> joiner) {
		builder.append(joiner.toString());
		return this;
	}

	public Joiner<String> join(String key, boolean useSeparator) {
		builder.append(key);
		if(useSeparator)
			builder.append(SLASH);
		return this;
	}

	
}
