package org.fantasy.bean.proxy;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.fantasy.bean.proxy.asm.ClassFile;
import org.fantasy.bean.proxy.asm.ProxyClassGenerator;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.Constant;
import org.fantasy.util.ReflectionUtils;

public class ProxyCreator extends ClassLoader {

	private static final Logger LOG = Logger.getLogger(ProxyCreator.class);
	private static Map<ClassLoader, Map<String, Object>> loaderToCache = new WeakHashMap<ClassLoader, Map<String,Object>>();
	private static final Object PENDING_MARK = new Object();
	
	
	public static Class<?> getProxyClass(final Class<?> targetClass) {
		ClassLoader loader = targetClass.getClassLoader();
		if(loader == null)
			loader = ClassUtils.getClassLoader();
		String className = targetClass.getName();
		ProtectionDomain protectionDomain = AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {
			public ProtectionDomain run() {
				return targetClass.getProtectionDomain();
			}
		});
		// 此种缓存的写法很好
		Map<String, Object> cache = null;
		synchronized(loaderToCache) {
			cache = loaderToCache.get(loader);
			if(cache == null) {
				cache = new HashMap<String, Object>();
				loaderToCache.put(loader, cache);
			}
		}
		
		Class<?> proxyClass = null;
		synchronized(cache) {
			do {
				Object value = cache.get(className);
				if(value instanceof Reference) {
					proxyClass = (Class<?>)((Reference)value).get();
				}
				if(proxyClass != null) {
					return proxyClass;
				} else if(value == PENDING_MARK) {
					try {
						cache.wait();
					} catch (InterruptedException e) {
						
					}
					continue;
				} else {
					cache.put(className, PENDING_MARK);
					break;
				}
			} while(true);
		}
		
		try {
			ClassFile classFile = ProxyClassGenerator.generateByteCode(targetClass);
			proxyClass = ReflectionUtils.defineClass(classFile.getClassName(), classFile.getByteCodes(), loader, protectionDomain);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			synchronized(cache) {
				if(proxyClass != null) {
					cache.put(className, new WeakReference<Object>(proxyClass));
				} else {
					cache.remove(className);
				}
				cache.notifyAll();
			}
		}
		return proxyClass;
	}
	
	
	
	public static Object newProxyInstance(Class<?> targetClass, MethodInterceptor interceptor, Object ... args) {
		if(interceptor == null)
			LOG.warn("Interceptor is null, proxy has no effect, just invoke self");
		if(targetClass == null)
			throw new IllegalArgumentException(targetClass.getName());
		Class<?> proxyClass = getProxyClass(targetClass);
		try {
			setInterceptor(proxyClass, interceptor);
			Constructor<?> cons = null;
			Class<?>[] classArgs = Constant.CLASS_EMPTY_ARGS;
			if(args != null && args.length != 0) {
				classArgs = new Class[args.length];
				for(int i = 0; i < args.length; i++)
					classArgs[i] = args[i].getClass();
			}
			cons = proxyClass.getDeclaredConstructor(classArgs);
			return cons.newInstance(args);
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				setInterceptor(proxyClass, null);
			} catch (NoSuchMethodException e) {
				// ignore
			}
		}
	}
	
	
	private static void setInterceptor(Class<?> proxyClass, MethodInterceptor interceptor) throws NoSuchMethodException {
		Method method = proxyClass.getDeclaredMethod("setInterceptor", new Class[]{MethodInterceptor.class});
		method.setAccessible(true);
		try {
			method.invoke(null, interceptor);
		} catch (IllegalAccessException e) {
			// ignore
			// because of public 
		} catch (IllegalArgumentException e) {
			// ignore
			// because of fixed args
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	public static void main(String[] args) {
		
		
//		MethodInterceptor mi = new MethodInterceptor() {
//			
//
//			public Object intercept(ProxyInvoker invoker, Method method, Object... args) throws Throwable {
//					
//				System.out.println("intercept");
//				return invoker.invoke(null, method, args);
//			}
//
//			public void afterReturning(Object target, Object returnValue, Method method, Object... args) throws Throwable {
//					
//				System.out.println("afterReturning");
//				
//			}
//
//			public Object getTargetObject() {
//				
//				return null;
//			}
//
//			public void before(Object target, Method method, Object... args)
//					throws Throwable {
//				System.out.println("before");
//				
//			}
//			
//			
//		};
//		FMyTemplate0.setInterceptor(mi);
//		FMyTemplate0 my = new FMyTemplate0();
//		my.foo("hello");
		
		MyTemplate temp = (MyTemplate)ProxyCreator.newProxyInstance(MyTemplate.class, new MethodInterceptor() {
			

			public Object intercept(ProxyInvoker invoker, Method method, Object... args) throws Throwable {
					
				System.out.println("intercept");
				return invoker.invoke(null, method, args);
			}

			public void afterReturning(Object target, Object returnValue, Method method, Object... args) throws Throwable {
					
				System.out.println("afterReturning");
				
			}

			public Object getTargetObject() {
				
				return null;
			}

			public void before(Object target, Method method, Object... args)
					throws Throwable {
				System.out.println("before");
				
			}
			
			
		});
		
		temp.foo("hello");
	}
	*/
}
