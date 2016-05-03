package org.fantasy.bean.io;

public interface PathMatcher {
	
	public boolean match(String pattern, String path);

	public boolean matchStart(String pattern, String path);

}
