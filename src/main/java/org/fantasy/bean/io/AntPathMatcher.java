package org.fantasy.bean.io;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fantasy.util.Constant;
import org.fantasy.util.StringUtils;

public class AntPathMatcher implements PathMatcher {

//	public boolean isPattern(String path) {
//		return false;
//	}

	public boolean match(String pattern, String path) {
		return doMatch(pattern, path, true);
	}

	public boolean matchStart(String pattern, String path) {
		return doMatch(pattern, path, false);
	}

//	public static void main(String[] args) {
////		String pattern = "org/fantasy/bean/provide/**/*.class";
////		String path = "org/fantasy/bean/provide/Foo.class";
//		String pattern = "org/fantasy/bean/provide/**/**/zz/rr/**/*.class";
//		String path = "org/fantasy/bean/provide/oo/xx/zz/rr/Foo.class";
//		AntPathMatcher pathMatcher = new AntPathMatcher();
//		pathMatcher.doMatch(pattern, path, true);
//		
////		System.out.println("hello".substring(0, 0));
//	}
	
	private boolean doMatch(String pattern, String path, boolean fullMatch) {
		if(pattern.startsWith(Constant.DEFAULT_PATH_SEPARATOR) != path.startsWith(Constant.DEFAULT_PATH_SEPARATOR)) {
			return false;
		}
		List<String> patternTokens = StringUtils.tokenizeToList(pattern, Constant.DEFAULT_PATH_SEPARATOR);
		List<String> pathTokens = StringUtils.tokenizeToList(path, Constant.DEFAULT_PATH_SEPARATOR);
		
		int pattStartIndex = 0;
		int pattEndIndex = patternTokens.size() - 1;
		int pathStartIndex = 0;
		int pathEndIndex = pathTokens.size() -1;
		
		while(pattStartIndex <= pattEndIndex && pathStartIndex <= pathEndIndex) {
			String pattDir = patternTokens.get(pattStartIndex);
			if ("**".equals(pattDir)) {
				break;
			}
//			if(!pattDir.equals(pathTokens.get(pathStartIndex))) {
			if(!matchStrings(pattDir, pathTokens.get(pathStartIndex))) {
				return false;
			}
			pattStartIndex++;
			pathStartIndex++;
		}
		//com/xxx/yyy/**/*.class
		//com/xxx/yyy
		if(pathStartIndex > pathEndIndex) {
			if(pathStartIndex > pathEndIndex) {
				return (
					pattern.endsWith(Constant.DEFAULT_PATH_SEPARATOR) ? path.endsWith(Constant.DEFAULT_PATH_SEPARATOR) : !path.endsWith(Constant.DEFAULT_PATH_SEPARATOR)
				);
			}
			if(!fullMatch) {
				return true;
			}
			if (pattStartIndex == pattEndIndex - 1 && patternTokens.get(pattStartIndex).equals("*") && path.endsWith(Constant.DEFAULT_PATH_SEPARATOR)) {
				return true;
			}
			for (int i = pattStartIndex; i <= pattEndIndex; i++) {
				if (!patternTokens.get(i).equals("**")) {
					return false;
				}
			}
			return true;
		//com/xxx/yyy
		//com/xxx/yyy/zz/Foo.class
		} else if(pattStartIndex >= pattEndIndex) {
			return false;
		//com/xxx/yyy/**/*.class
		//com/xxx/yyy/**/Foo.class
		} else if (!fullMatch && "**".equals(patternTokens.get(pattStartIndex))) {
			return true;
		}
		
		while(pattStartIndex <= pattEndIndex && pathStartIndex <= pathEndIndex) {
			// 反过来循环
			String pattDir = patternTokens.get(pattEndIndex);
			if ("**".equals(pattDir)) {
				break;
			}
			if(!matchStrings(pattDir, pathTokens.get(pathEndIndex))) {
				return false;
			}
			pattEndIndex--;
			pathEndIndex--;
		}
		// org/fantasy/bean/provide/**/*.class
		// org/fantasy/bean/provide/Foo.class
		if (pathStartIndex >= pathEndIndex) {
			for (int i = pathStartIndex; i <= pathEndIndex; i++) {
				if (!patternTokens.get(i).equals("**")) {
					return false;
				}
			}
			return true;
		}
		// org/fantasy/bean/provide/**/**/zz/rr/**/*.class
		// org/fantasy/bean/provide/oo/xx/zz/rr/Foo.class
		while (pattStartIndex != pattEndIndex && pathStartIndex <= pathEndIndex) {
			int patIdxTmp = -1;
			for (int i = pattStartIndex + 1; i <= pattEndIndex; i++) {
				if (patternTokens.get(i).equals("**")) {
					patIdxTmp = i;
					break;
				}
			}
			if (patIdxTmp == pattStartIndex + 1) {
				// '**/**' situation, so skip one
				pattStartIndex++;
				continue;
			}
			// Find the pattern between padIdxStart & padIdxTmp in str between strIdxStart & strIdxEnd
			int patLength = (patIdxTmp - pattStartIndex - 1);
			int strLength = (pathEndIndex - pathStartIndex + 1);
			int foundIdx = -1;

			strLoop:
			for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					String subPat = patternTokens.get(pattStartIndex + j + 1);
					String subStr = pathTokens.get(pathStartIndex + i + j);
					if (!matchStrings(subPat, subStr)) {
						continue strLoop;
					}
				}
				foundIdx = pathStartIndex + i;
				break;
			}
			if (foundIdx == -1) {
				return false;
			}
			pattStartIndex = patIdxTmp;
			pathStartIndex = foundIdx + patLength;
		}
		for (int i = pattStartIndex; i <= pattEndIndex; i++) {
			if (!patternTokens.get(i).equals("**")) {
				return false;
			}
		}
		return true;
	}
	
	
	private boolean matchStrings(String pattern, String str) {
		StringBuilder patternBuilder = new StringBuilder();
		Matcher matcher = Constant.ASTERISK_QUESTION_MARK_PATTERN.matcher(pattern);
		int end = 0;
		while(matcher.find()) {
			patternBuilder.append(pattern.substring(end, matcher.start()));
			String match = matcher.group();
			if("?".equals(match)) {
				patternBuilder.append(".");
			} else if("*".equals(match)) {
				patternBuilder.append(".*");
			}
			end = matcher.end();
		}
		patternBuilder.append(pattern.substring(end, pattern.length()));
		Pattern p = Pattern.compile(patternBuilder.toString());
		Matcher m = p.matcher(str);
		return m.matches();
	}

}
