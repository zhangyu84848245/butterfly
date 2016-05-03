package org.fantasy.bean.annotation.scanner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fantasy.bean.BeanCreatorCallback;
import org.fantasy.bean.BeanException;
import org.fantasy.bean.GenericBean;
import org.fantasy.bean.annotation.RpcMethod;
import org.fantasy.bean.asm.AnnotationAttributes;
import org.fantasy.bean.asm.CachedMetadataReaderFactory;
import org.fantasy.bean.asm.MetadataReader;
import org.fantasy.bean.asm.MetadataReaderFactory;
import org.fantasy.bean.asm.MethodMetadata;
import org.fantasy.bean.factory.ServiceBeanRegistry;
import org.fantasy.bean.io.AntPathMatcher;
import org.fantasy.bean.io.PathMatcher;
import org.fantasy.common.Convertor;
import org.fantasy.common.MethodDescriptor;
import org.fantasy.common.TypeResolver;
import org.fantasy.conf.Configuration;
import org.fantasy.io.DefaultResourceLoader;
import org.fantasy.io.ResourceLoader;
import org.fantasy.util.CollectionUtils;
import org.fantasy.util.Constant;

public class ClassPathScanner implements BeanScanner {

	private static final Logger LOG = Logger.getLogger(ClassPathScanner.class);
	private ServiceBeanRegistry beanRegistry;
	private Configuration conf;
	private String[] basePackages;
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private PathMatcher pathMatcher = new AntPathMatcher();
	private MetadataReaderFactory metadataReaderFactory = new CachedMetadataReaderFactory();
	
	private BeanCreatorCallback beanCallback;
	
	public ClassPathScanner(Configuration conf, ServiceBeanRegistry beanRegistry) {
		this.conf = conf;
		this.basePackages = conf.getArray(Constant.SERVICE_PATH_SCAN);
		this.beanRegistry = beanRegistry;
	}
	
	public void doScan(/**  Class<?> annotationClass  */) {
		Set<File> files = new HashSet<File>();
		try {
			for(String path : basePackages) {
				String rootDir = path.replace('.', '/');
				rootDir += "/";
				String subPattern = Constant.DEFAULT_RESOURCE_PATTERN;
				List<URL> urls = resourceLoader.getResources(rootDir);
				for(Iterator<URL> iterator = urls.iterator();iterator.hasNext();) {
					URL url = iterator.next();
					File rootDirFile = resourceLoader.getFileByURL(url);
					String fullPattern = rootDirFile.getAbsolutePath().replace(File.separatorChar, '/');
					if(!subPattern.startsWith("/")) {
						fullPattern += "/";
					}
					fullPattern = fullPattern + subPattern;
					findMatchingFiles(fullPattern, rootDirFile, files);
				}
			}
//			String annotationName = annotationClass.getName();
			for(Iterator<File> iterator = files.iterator();iterator.hasNext();) {
				File file = iterator.next();
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(file);
				TypeFilter filter = new AnnotationTypeFilter(metadataReader.getAnnotationMetadata());
				if(filter.accept(beanCallback.getAnnotationName())) {
					createBean(beanCallback, metadataReader);
				}
			}
		} catch(Exception e) {
			throw new BeanException(e);
		}
	}
	
	private void beforeCreate() {
		
	}
	
	private void afterCreate(MetadataReader metadataReader, GenericBean bean) {
		Set<MethodMetadata> methodMetadatas = metadataReader.getAnnotationMetadata().getAnnotatedMethods(RpcMethod.class.getName());
		List<MethodDescriptor> methodList = CollectionUtils.convert(methodMetadatas, new Convertor<MethodMetadata, MethodDescriptor>() {
			public MethodDescriptor convert(MethodMetadata metadata) {
				String[] types = resolveMethodDescriptor(metadata.getMethodDescriptor(), true, new TypeResolver() {
					public String resolve(char[] charArray, int index) {
						return resolveType(charArray, index);
					}
				});
				int length = types.length;
				String[] parameterTypes = new String[length - 1];
				System.arraycopy(types, 0, parameterTypes, 0, length - 1);
				return new MethodDescriptor(
						metadata.getAccess(), 
						metadata.getMethodName(), 
						metadata.getDeclaringClassName(), 
						parameterTypes, 
						types[length - 1], 
						metadata.getExceptionTypes()
				);
			}
		});
		bean.setMethodList(methodList);
	}
	
	private GenericBean createBean(BeanCreatorCallback callback, MetadataReader metadataReader) {
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadataReader.getAnnotationMetadata().getAnnotationAttributes(callback.getAnnotationName()));
		String refClassName = attributes.getString("refClass");
		String beanClassName = metadataReader.getClassMetadata().getClassName();
		String beanId = attributes.getString("id");
		GenericBean bean = null;
		try {
			bean = beanRegistry.getBean(beanId);
		} catch(BeanException e) {
			bean = null;
		}
		if(bean == null) {
			beforeCreate();
			try {
				bean = callback.create(beanId, beanClassName, refClassName);
			} finally {
				afterCreate(metadataReader, bean);
			}
			beanRegistry.registerBean(beanId, bean);
		}
		return bean;
	}
	
	private void findMatchingFiles(String fullPattern, File dir, Set<File> files) {
		File[] listFiles = resourceLoader.files(dir);
		for(File file : listFiles) {
			String currPath =  file.getAbsolutePath().replace(File.separatorChar, '/');
			if(file.isDirectory()) {
				if(pathMatcher.matchStart(fullPattern, currPath)) {
					if(!file.canRead()) {
						LOG.warn(file.getAbsolutePath() + " hasn't read permission.");
					} else {
						findMatchingFiles(fullPattern, file, files);
					}
				}
			} else if(file.isFile()) {
				if(pathMatcher.match(fullPattern, currPath)) {
					files.add(file);
				}
			}
		}
	}
	
	
	
	
	private String[] resolveMethodDescriptor(String methodDescriptor, boolean hasBracket, TypeResolver typeResolver) {
		char[] charArray = methodDescriptor.toCharArray();
		int i = 0;
		if(hasBracket) {
			char leftBracket = charArray[0];
			if(leftBracket != '(') {
				throw new IllegalArgumentException("Method descriptor must be start with (.");
			}
			i++;
		}
		List<String> types = new ArrayList<String>();
		for(; i < charArray.length; i++) {
			char c = charArray[i];
			if(hasBracket && c == ')')
				continue;
			if(c == 'L' || c == '[') {
				String type = typeResolver.resolve(charArray, i);
				types.add(type);
				i += (type.length() - 1);
				continue;
			}
			types.add(String.valueOf(c));
		}
		return types.toArray(new String[types.size()]);
	}
	
	private String resolveType(char[] charArray, int index) {
		int start = index;
		char c = charArray[start];
		StringBuilder builder = new StringBuilder();
		while(true) {
			switch(c) {
				// Ljava/lang/String;
				case 'L': 
					builder.append('L');
					char s = charArray[++start];
					while(s != ';') {
						builder.append(s);
						s = charArray[++start];
					}
					builder.append(s);
					return builder.toString();
				case '[':
					builder.append('[');
					c = charArray[++start];
				default:
			}
		}
	}

	public void setBeanCallback(BeanCreatorCallback beanCallback) {
		this.beanCallback = beanCallback;
	}

}
