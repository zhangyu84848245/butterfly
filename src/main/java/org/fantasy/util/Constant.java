package org.fantasy.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class Constant {

	public static final String MAGIC = "fantasy";
	public static final String VERSION = "1.0";
	public static final int MAX_BUFFER_SIZE = 64 * 1024;
	public static final String SERVICE_PATH_SCAN = "rpc.service.path.scan";
	public static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	public static final String DEFAULT_PATH_SEPARATOR = "/";
	// 星号和问号
	public static final Pattern ASTERISK_QUESTION_MARK_PATTERN = Pattern.compile("\\*|\\?");
	
	// unsafe 序列化时使用的常量
	// type
	public final static byte TYPE_STRING = (byte)0x10;
	public final static byte TYPE_ARRAY = (byte)0x11;
	public static final byte TYPE_ENUM = (byte)0x12;
	public static final byte TYPE_COLLECTION = (byte)0x13;
	public static final byte TYPE_MAP = (byte)0x14;
	public static final byte TYPE_OBJECT = (byte)0x15;
	public static final byte TYPE_NULL = (byte)0x16;
	// 2015-05-03 添加
	public static final byte TYPE_CLASS = (byte)0x17;
	public static final byte TYPE_DATE = (byte)0x18;
	//
	public static final byte CLASS_DESC = (byte)0x20;
	public final static byte WRITE_OBJECT_METHOD = 0x21;
	public static final byte READ_OBJECT_METHOD = 0x22;
		
	public static final byte WRITE_END = 0x47;
	
	
	public static final Map<String, Character> WRAPPER_CLASSES = new HashMap<String, Character>(8, 1.0F);
	static {
		WRAPPER_CLASSES.put("java.lang.Boolean", 'Z');
		WRAPPER_CLASSES.put("java.lang.Byte", 'B');
		WRAPPER_CLASSES.put("java.lang.Character", 'C');
		WRAPPER_CLASSES.put("java.lang.Short", 'S');
		WRAPPER_CLASSES.put("java.lang.Integer", 'I');
		WRAPPER_CLASSES.put("java.lang.Long", 'J');
		WRAPPER_CLASSES.put("java.lang.Float", 'F');
		WRAPPER_CLASSES.put("java.lang.Double", 'D');
	}

	public static final String DEFAULT_CONFIG_LOCATION = "default.conf.properties";
	
	public static final int PING_VALUE = -1;
	
	
	// zk
	
	public static final String ZOOKEEPER_REGISTRY_ROOT = "/fantasy/butterfly";
	public static final char SLASH = '/';
	
	
	
	// proxy
	
	public static final String PROXY_PACKAGE_NAME = "org.fantasy.bean.proxy.";
	public static final String DOLLAR  = "$$";
	public static final String UNDERLINE = "_";
	public static final String METHOD_STR = "METHOD";
	public static final String METHOD_PROXY_STR = "METHOD_PROXY";
	public static final String PROXY_STR = "Proxy";
	
	
	// 
	
	public static final Class<?>[] CLASS_EMPTY_ARGS = new Class[0];
	public static final Object[] OBJECT_EMPTY_ARGS = new Object[0];
	public static final String SOURCE_FILE = "<generated>";
	public static final String CONSTRUCTOR_NAME = "<init>";
	
	
	// 30秒
	public static final int DEFAULT_HEARTBEAT_INTERVAL = 30 * 1000;
	
	// timeout
	// 1分钟
	public static final int DEFAULT_CALL_TIMEOUT = 60 * 1000;
	// 1分钟
	public static final int DEFAULT_CLIENT_CONNECT_TIMEOUT = 60 * 1000;
	
	public static final String BIND_ADDRESS_KEY = "socket.bind.address";
	public static final String BIND_PORT_KEY = "socket.bind.port";
	
	
	// load balancer
	public static final String LOADBALANCE_TYPE_KEY = "server.loadbalance.type";

}
