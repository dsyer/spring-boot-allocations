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

package com.example.auto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.example.auto.AutoConfigurations.EnableActuatorAutoConfigurations;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

@Configuration
@EnableActuatorAutoConfigurations
class AutoConfigurations extends AutoConfigurationImportSelector
		implements BeanDefinitionRegistryPostProcessor {

	private GenericApplicationContext context;

	public AutoConfigurations(GenericApplicationContext applicationContext) {
		this.context = applicationContext;
		setBeanFactory(applicationContext.getDefaultListableBeanFactory());
		setBeanClassLoader(applicationContext.getClassLoader());
		setEnvironment(applicationContext.getEnvironment());
		setResourceLoader(applicationContext);
	}

	public String[] config() {
		return selectImports(new StandardAnnotationMetadata(AutoConfigurations.class));
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Inherited
	@AutoConfigurationPackage
	static @interface EnableActuatorAutoConfigurations {
		Class<?>[] exclude() default {};

		String[] excludeName() default {};
	}

	@Override
	protected Class<?> getAnnotationClass() {
		return EnableActuatorAutoConfigurations.class;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		try {
			register(registry, this.context.getDefaultListableBeanFactory());
		}
		catch (BeansException e) {
			throw e;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new BeanCreationException("Cannot register from " + getClass(), e);
		}
	}

	protected void register(BeanDefinitionRegistry registry,
			ConfigurableListableBeanFactory factory) throws Exception {
		ConditionEvaluator evaluator = new ConditionEvaluator(registry, getEnvironment(),
				getResourceLoader());
		for (String config : config()) {
			Class<?> type = ClassUtils.resolveClassName(config, getBeanClassLoader());
			StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(type);
			if (evaluator.shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN)) {
				continue;
			}
			register(registry, evaluator, type, metadata);
		}
	}

	private void register(BeanDefinitionRegistry registry, ConditionEvaluator evaluator,
			Class<?> type, StandardAnnotationMetadata metadata) {
		for (Class<?> nested : type.getClasses()) {
			if (Modifier.isStatic(nested.getModifiers())) {
				try {
					StandardAnnotationMetadata nestedMetadata = new StandardAnnotationMetadata(
							nested);
					if (nestedMetadata.hasAnnotation(Configuration.class.getName())) {
						if (!evaluator.shouldSkip(nestedMetadata,
								ConfigurationPhase.REGISTER_BEAN)) {
							register(registry, evaluator, nested, nestedMetadata);
						}
					}
				}
				catch (ArrayStoreException e) {
					// TODO: use ASM to avoid this?
				}
			}
		}
		if (metadata.hasAnnotation(Import.class.getName())) {
			Object[] props = (Object[]) metadata
					.getAnnotationAttributes(Import.class.getName()).get("value");
			if (props != null && props.length > 0) {
				for (Object object : props) {
					Class<?> imported = (Class<?>) object;
					if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(imported)) {
						ImportBeanDefinitionRegistrar registrar = (ImportBeanDefinitionRegistrar) getBeanFactory()
								.createBean(imported);
						registrar.registerBeanDefinitions(metadata, registry);
					}
					try {
						StandardAnnotationMetadata nestedMetadata = new StandardAnnotationMetadata(
								imported);
						if (!registry.containsBeanDefinition(imported.getName())
								&& !evaluator.shouldSkip(nestedMetadata,
										ConfigurationPhase.REGISTER_BEAN)) {
							register(registry, evaluator, imported, nestedMetadata);
						}
					}
					catch (ArrayStoreException e) {
						// TODO: use ASM to avoid this?
					}
				}
			}
		}
		if (metadata.hasAnnotation(EnableConfigurationProperties.class.getName())) {
			Object[] props = (Object[]) metadata.getAnnotationAttributes(
					EnableConfigurationProperties.class.getName()).get("value");
			if (props != null && props.length > 0) {
				for (Object object : props) {
					Class<?> prop = (Class<?>) object;
					String name = prop.getName();
					if (!registry.containsBeanDefinition(name)) {
						registry.registerBeanDefinition(name, BeanDefinitionBuilder
								.genericBeanDefinition(prop).getRawBeanDefinition());
					}
				}
			}
		}
		registry.registerBeanDefinition(type.getName(),
				BeanDefinitionBuilder.rootBeanDefinition(type).getRawBeanDefinition());
		Set<MethodMetadata> methods = metadata.getAnnotatedMethods(Bean.class.getName());
		Map<String, MethodMetadata> beans = new HashMap<>();
		for (MethodMetadata method : methods) {
			beans.put(method.getMethodName(), method);
		}
		for (Method method : ReflectionUtils.getUniqueDeclaredMethods(type)) {
			if (AnnotationUtils.findAnnotation(method, Bean.class) != null) {
				register(registry, evaluator, type, method, beans.get(method.getName()));
			}
		}
	}

	private void register(BeanDefinitionRegistry registry, ConditionEvaluator evaluator,
			Class<?> type, Method method, MethodMetadata metadata) {
		try {
			if (!evaluator.shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN)) {
				Class<?> beanClass = method.getReturnType();
				Supplier<?> supplier = () -> {
					Object[] args = params(method, getBeanFactory());
					ReflectionUtils.makeAccessible(method);
					return ReflectionUtils.invokeMethod(method,
							getBeanFactory().getBean(type), args);
				};
				RootBeanDefinition definition = new RootBeanDefinition();
				definition.setTargetType(beanClass);
				definition.setInstanceSupplier(supplier);
				registry.registerBeanDefinition(method.getName(), definition);
			}
		}
		catch (ArrayStoreException e) {
			// TODO: use ASM to avoid this?
		}
	}

	private Object[] params(Method method, ConfigurableListableBeanFactory factory) {
		Object[] params = new Object[method.getParameterCount()];
		for (int i = 0; i < params.length; i++) {
			// TODO: deal with required flag
			params[i] = factory.resolveDependency(
					new DependencyDescriptor(new MethodParameter(method, i), false),
					method.getName());
		}
		return params;
	}

}