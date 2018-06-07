/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.config;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
public class MetadataReaderInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		context.addBeanFactoryPostProcessor(new SpecialPostProcessor());
		overrideMetadataReaderFactory(context);
	}

	private void overrideMetadataReaderFactory(ConfigurableApplicationContext context) {
		Object object = getField(context, "scanner");
		if (object instanceof ClassPathBeanDefinitionScanner) {
			ClassPathBeanDefinitionScanner scanner = (ClassPathBeanDefinitionScanner) object;
			scanner.setMetadataReaderFactory(new FasterMetadataReaderFactory());
		}
	}

	private Object getField(Object target, String name) {
		Field field = ReflectionUtils.findField(target.getClass(), name);
		ReflectionUtils.makeAccessible(field);
		return ReflectionUtils.getField(field, target);
	}

}

class SpecialPostProcessor
		implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

	public static final String BEAN_NAME = "org.springframework.boot.autoconfigure."
			+ "internalCachingMetadataReaderFactory";

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 100;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		RootBeanDefinition definition = new RootBeanDefinition(
				FasterMetadataReaderFactoryBean.class);
		registry.registerBeanDefinition(BEAN_NAME, definition);
	}

}

class FasterMetadataReaderFactoryBean
		implements FactoryBean<ConcurrentReferenceCachingMetadataReaderFactory>,
		BeanClassLoaderAware, ApplicationListener<ContextRefreshedEvent> {

	private ConcurrentReferenceCachingMetadataReaderFactory metadataReaderFactory;

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.metadataReaderFactory = new FasterMetadataReaderFactory(classLoader);
	}

	@Override
	public ConcurrentReferenceCachingMetadataReaderFactory getObject() throws Exception {
		return this.metadataReaderFactory;
	}

	@Override
	public Class<?> getObjectType() {
		return CachingMetadataReaderFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.metadataReaderFactory.clearCache();
	}

}
