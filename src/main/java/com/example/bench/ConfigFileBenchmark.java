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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

@Measurement(iterations = 5)
@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)
public class ConfigFileBenchmark {

	@Benchmark
	public void simple(SimpleState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void directory(DirectoryState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void file(FileState state) throws Exception {
		state.run();
	}

	@State(Scope.Thread)
	public static class FileState extends ConfigFileState {
		@Override
		protected ConfigFileApplicationListener create() {
			ConfigFileApplicationListener listener = new ConfigFileApplicationListener();
			listener.setSearchLocations("file:./src/main/resources/application.properties");
			return  listener;
		}
	}

	@State(Scope.Thread)
	public static class DirectoryState extends ConfigFileState {
		@Override
		protected ConfigFileApplicationListener create() {
			ConfigFileApplicationListener listener = new ConfigFileApplicationListener();
			listener.setSearchLocations("file:./src/main/resources/");
			return  listener;
		}
	}

	@State(Scope.Thread)
	public static class SimpleState extends ConfigFileState {
	}
	
	public static void main(String[] args) {
		new SimpleState().run();
	}

	public static abstract class ConfigFileState {

		private ConfigFileApplicationListener listener;

		public ConfigFileState() {
			this.listener = create();
		}

		protected ConfigFileApplicationListener create() {
			return new ConfigFileApplicationListener();
		}

		@TearDown(Level.Invocation)
		public void clear() {
		}

		public void run() {
			SpringApplication application = new SpringApplication(MyApplication.class);
			ConfigurableEnvironment environment = new StandardEnvironment();
			ApplicationEnvironmentPreparedEvent envPrep = new ApplicationEnvironmentPreparedEvent(application, new String[0], environment );
			listener.onApplicationEvent(envPrep);
			GenericApplicationContext context = new GenericApplicationContext();
			context.setEnvironment(environment);
			ApplicationPreparedEvent appPrep = new ApplicationPreparedEvent(application, new String[0], context);
			listener.onApplicationEvent(appPrep);
		}
	}
		
	@SpringBootApplication
	static class MyApplication {}
}
