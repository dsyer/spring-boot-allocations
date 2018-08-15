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

import org.aspectj.lang.Aspects;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.PublicConfigurationClassEnhancer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;

@Measurement(iterations = 5)
@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)
public class BeanCreationBenchmark {

	@Benchmark
	public void simple(SimpleState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void reflect(ReflectState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void proxy(ProxyState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void cglib(CglibState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void enhnc(EnhancerState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void proce(ProcessorState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void unpro(UnprocessorState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void funcs(FuncState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void bare(BareState state) throws Exception {
		state.run();
	}

	public static void main(String[] args) {
		// FuncState state = new FuncState();
		// UnprocessorState state = new UnprocessorState();
		ProcessorState state = new ProcessorState();
		state.start();
		//SimpleState state = new SimpleState();
		state.run();
		Aspects.aspectOf(CallCounter.class).log();
	}

	@State(Scope.Thread)
	public static class BareState {

		public void run() {
			MyConfiguration config = new MyConfiguration();
			MyBean bean = config.bean(config.foo());
			assert bean.getFoo() != null;
		}
	}

	@State(Scope.Thread)
	public static class ReflectState {
		public void run() {
			MyBean bean;
			try {
				Foo foo = Foo.class.getConstructor(String.class).newInstance("bar");
				bean = MyBean.class.getConstructor(Foo.class).newInstance(foo);
				assert bean.getFoo() != null;
			}
			catch (Exception e) {
			}
		}
	}

	@State(Scope.Thread)
	public static class ProxyState {
		private Foo foo = new Foo("bar");

		public void run() {
			ProxyFactory factory = new ProxyFactory(new MyBean());
			factory.setProxyTargetClass(false);
			Bean bean;
			try {
				bean = (Bean) factory.getProxy();
				bean.setFoo(foo);
				assert bean.getFoo() != null;
			}
			catch (Exception e) {
			}
		}
	}

	@State(Scope.Thread)
	public static class CglibState {
		private Foo foo = new Foo("bar");

		public void run() {
			ProxyFactory factory = new ProxyFactory(new MyBean());
			factory.setProxyTargetClass(true);
			MyBean bean;
			try {
				bean = (MyBean) factory.getProxy();
				bean.setFoo(foo);
				assert bean.getFoo() != null;
			}
			catch (Exception e) {
			}
		}
	}

	@State(Scope.Thread)
	public static class EnhancerState {

		public void run() {
			Class<?> enhanced = new PublicConfigurationClassEnhancer().enhance(
					MyConfiguration.class, MyConfiguration.class.getClassLoader());
			MyConfiguration config = (MyConfiguration) BeanUtils
					.instantiateClass(enhanced);
			assert config.foo() != null;
			assert config.bean(config.foo()).getFoo() != null;
		}
	}

	@State(Scope.Thread)
	public static class ProcessorState {

		private GenericApplicationContext registry;

		@Setup(Level.Invocation)
		public void start() {
			ConfigurationClassPostProcessor processor = new ConfigurationClassPostProcessor();
			processor.setBeanClassLoader(MyConfiguration.class.getClassLoader());
			processor.setEnvironment(new StandardEnvironment());
			registry = new GenericApplicationContext();
			registry.setClassLoader(MyConfiguration.class.getClassLoader());
			registry.registerBean(AutowiredAnnotationBeanPostProcessor.class);
			registry.registerBean(MyConfiguration.class);
			registry.addBeanFactoryPostProcessor(processor);
		}

		@TearDown(Level.Invocation)
		public void clear() {
			if (registry != null) {
				registry.close();
			}
		}

		public void run() {
			registry.refresh();
			MyConfiguration config = registry.getBean(MyConfiguration.class);
			Foo foo = config.foo();
			assert foo != null;
			assert foo == config.foo();
			assert config.bean(foo).getFoo() != null;
		}
	}

	@State(Scope.Thread)
	public static class UnprocessorState {

		private GenericApplicationContext registry;

		public UnprocessorState() {
		}

		@Setup(Level.Invocation)
		public void start() {
			registry = new GenericApplicationContext();
			registry.registerBean(MyConfiguration.class);
		}

		@TearDown(Level.Invocation)
		public void clear() {
			if (registry != null) {
				registry.close();
			}
		}

		public void run() {
			registry.refresh();
			MyConfiguration config = registry.getBean(MyConfiguration.class);
			Foo foo = config.foo();
			assert foo != null;
			assert foo != config.foo();
			assert config.bean(foo).getFoo() != null;
		}
	}

	@State(Scope.Thread)
	public static class FuncState {

		private GenericApplicationContext registry;

		public FuncState() {
		}

		@Setup(Level.Invocation)
		public void start() {
			registry = new GenericApplicationContext();
			new FuncConfiguration().initialize(registry);
		}

		@TearDown(Level.Invocation)
		public void clear() {
			if (registry != null) {
				registry.close();
			}
		}

		public void run() {
			registry.refresh();
			Foo foo = registry.getBean(Foo.class);
			MyBean bean = registry.getBean(MyBean.class);
			assert foo != null;
			assert foo == bean.getFoo();
		}
	}

	@State(Scope.Thread)
	public static class SimpleState {

		@TearDown(Level.Invocation)
		public void clear() {
		}

		public void run() {
			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			factory.registerBeanDefinition("foo",
					BeanDefinitionBuilder.genericBeanDefinition(Foo.class)
							.addConstructorArgReference("value").getBeanDefinition());
			factory.registerSingleton("value", "bar");
			MyBean bean = factory.createBean(MyBean.class);
			assert bean.getFoo() != null;
		}

	}

	interface Bean {
		void setFoo(Foo foo);

		Foo getFoo();
	}

	static class MyBean implements Bean {
		@Autowired
		private Foo foo;

		public MyBean() {
		}

		public MyBean(Foo foo) {
			this.foo = foo;
		}

		public void setFoo(Foo foo) {
			this.foo = foo;
		}

		public Foo getFoo() {
			return this.foo;
		}
	}

	@Configuration
	static class MyConfiguration {
		@org.springframework.context.annotation.Bean
		public MyBean bean(Foo foo) {
			return new MyBean(foo);
		}

		@org.springframework.context.annotation.Bean
		public Foo foo() {
			return new Foo(value());
		}

		@org.springframework.context.annotation.Bean
		public String value() {
			return "bar";
		}
	}

	static class FuncConfiguration
			implements ApplicationContextInitializer<GenericApplicationContext> {
		@Override
		public void initialize(GenericApplicationContext registry) {
			MyConfiguration config = new MyConfiguration();
			registry.registerBean(String.class, () -> config.value());
			registry.registerBean(Foo.class, () -> config.foo());
			registry.registerBean(MyBean.class,
					() -> config.bean(registry.getBean(Foo.class)));
		}
	}
}

class Foo {

	private String value;

	public Foo() {
	}

	public Foo(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Foo [value=" + this.value + "]";
	}

}