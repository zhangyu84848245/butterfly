package org.fantasy.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {

	
	public static boolean isEmpty(String str) {
		if(str == null || "".equals(str.trim()))
			return true;
		return false;
	}
	
	
//	public static String replaceEmpty(String str) {
//		return replaceEmpty(str, "");
//	}
	
	
	public static String replaceEmpty(String str, String defaultValue) {
		if(str == null || str.length() == 0)
			return defaultValue;
		return str;
	}
	
	public static boolean hasLength(CharSequence str) {
		return str != null && str.length() > 0;
	}
	
	public static String replace(String str, String oldPattern, String newPattern) {
		if(!hasLength(str) && !hasLength(oldPattern) && newPattern != null) {
			return str;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		int index = str.indexOf(oldPattern);
		int patLen = oldPattern.length();
		while(index >= 0) {
			sb.append(str.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = str.indexOf(oldPattern, pos);
		}
		sb.append(sb.substring(pos));
		return sb.toString();
	}
	
	
	public static String stringifyException(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}
	
	
	public static List<String> tokenizeToList(String str, String delimiters) {
		if(str == null) {
			return null;
		}
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(str, delimiters);
		while(st.hasMoreElements()) {
			String token = st.nextToken();
			tokens.add(token);
		}
		return tokens;
	}
	
	
	/**
	 * 去掉大于127的字符
	 * @param str
	 * @return
	 */
	public static String sanitize(String str) {
		boolean changed = false;

		StringBuilder sb = new StringBuilder();
		for (char ch : str.toCharArray()) {
			// only include ASCII chars
			if (ch < 127) {
				sb.append(ch);
			} else {
				changed = true;
			}
		}

		if (changed) {
			String newHost = sb.toString();
			return newHost;
		} else {
			return str;
		}
	}
}
