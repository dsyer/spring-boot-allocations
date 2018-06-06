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
package com.example.bench;

import com.example.config.FasterMetadataReaderFactory;
import com.example.demo.DemoApplication;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

@Measurement(iterations = 5)
@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)
public class MetadataBenchmark {

	@Benchmark
	public void caching(CachingState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void kryo(KryoState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void reference(ReferenceState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void simple(SimpleState state) throws Exception {
		state.run();
	}

	@State(Scope.Thread)
	public static class KryoState extends MetadataState {
		protected MetadataReaderFactory createFactory() {
			return new FasterMetadataReaderFactory();
		}
	}

	@State(Scope.Thread)
	public static class CachingState extends MetadataState {
		protected MetadataReaderFactory createFactory() {
			return new CachingMetadataReaderFactory();
		}
	}

	@State(Scope.Thread)
	public static class ReferenceState extends MetadataState {
		protected MetadataReaderFactory createFactory() {
			return new ConcurrentReferenceCachingMetadataReaderFactory();
		}
	}

	@State(Scope.Thread)
	public static class SimpleState extends MetadataState {
		protected MetadataReaderFactory createFactory() {
			return new SimpleMetadataReaderFactory();
		}
	}
	
	public static void main(String[] args) {
		new SimpleState().run();
	}

	public static abstract class MetadataState {

		MetadataReaderFactory factory;

		public MetadataState() {
			this.factory = createFactory();
		}

		protected abstract MetadataReaderFactory createFactory();

		@TearDown(Level.Invocation)
		public void clear() {
			if (factory instanceof CachingMetadataReaderFactory) {
				((CachingMetadataReaderFactory) factory).clearCache();
			}
			if (factory instanceof ConcurrentReferenceCachingMetadataReaderFactory) {
				((ConcurrentReferenceCachingMetadataReaderFactory) factory).clearCache();
			}
		}

		public void run() {
			ConfigurationClassPostProcessor processor = new ConfigurationClassPostProcessor();
			processor.setMetadataReaderFactory(factory);
			@SuppressWarnings("resource")
			MyContext context = new MyContext();
			context.register(DemoApplication.class);
			context.postProcessBeanFactory(context.getBeanFactory());
			processor.postProcessBeanDefinitionRegistry((BeanDefinitionRegistry) context.getBeanFactory());
			processor.postProcessBeanFactory(context.getBeanFactory());
		}

	}
	
	static class MyContext extends AnnotationConfigReactiveWebServerApplicationContext {
		@Override
		public void postProcessBeanFactory(
				ConfigurableListableBeanFactory beanFactory) {
			super.postProcessBeanFactory(beanFactory);
		}
	}
}
