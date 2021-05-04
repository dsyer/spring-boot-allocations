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

import java.io.Closeable;
import java.io.IOException;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Dave Syer
 *
 */
@RestController
public class AutoApplication implements Runnable, Closeable,
		ApplicationContextInitializer<GenericApplicationContext> {

	public static final String MARKER = "Benchmark app started";

	private ConfigurableApplicationContext context;

	@GetMapping
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		AutoApplication bean = new AutoApplication();
		bean.run();
		System.err.println("Started: " + (System.currentTimeMillis() - t0) + "ms");
		if (Boolean.getBoolean("demo.close")) {
			bean.close();
		}
	}

	@Override
	public void close() throws IOException {
		if (context != null) {
			context.close();
		}
	}

	@Override
	public void run() {
		SpringApplication application = new SpringApplication(AutoApplication.class) {
			@Override
			protected void load(ApplicationContext context, Object[] sources) {
				// We don't want the annotation bean definition reader
				// super.load(context, sources);
			}
		};
		application.addInitializers(this);
		application.setApplicationContextFactory(ApplicationContextFactory
				.of(() -> new ReactiveWebServerApplicationContext()));
		this.context = application.run();
		System.err.println(MARKER);
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
		if (beanFactory != null) {
			if (!(beanFactory
					.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
				beanFactory
						.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
			}
			// N.B. ContextAnnotationAutowireCandidateResolver is normal, but that's more
			// expensive
			// (checks for @Lazy)
			if (!(beanFactory
					.getAutowireCandidateResolver() instanceof QualifierAnnotationAutowireCandidateResolver)) {
				beanFactory.setAutowireCandidateResolver(
						new QualifierAnnotationAutowireCandidateResolver());
			}
			beanFactory.addBeanPostProcessor(
					beanFactory.createBean(AutowiredAnnotationBeanPostProcessor.class));
		}
		AutoConfigurationPackages.register(context,
				ClassUtils.getPackageName(getClass()));
		ConfigurationPropertiesBindingPostProcessor.register(context);
		context.addBeanFactoryPostProcessor(new AutoConfigurations(context));
		context.registerBean(AutoApplication.class, () -> this);
	}

}
