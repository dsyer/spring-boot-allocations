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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

@Measurement(iterations = 5)
@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)
public class BinderBenchmark {

	@Benchmark
	public void direct(DirectState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void map(MapState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void wrapped(WrappedState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void binder(BinderState state) throws Exception {
		state.run();
	}

	@State(Scope.Thread)
	public static class DirectState extends EnvironmentState {
		private String[] value;

		public void run() {
			value = environment.getProperty("spring.profiles.active", String[].class);
			assert value.length == 2;
		}
	}

	@State(Scope.Thread)
	public static class MapState extends EnvironmentState {
		private String[] value;
		private MapConfigurationPropertySource source;

		protected void init() {
			super.init();
			Map<String, String> map = new HashMap<>();
			map.put("spring.profiles.active", "one,two");
			source = new MapConfigurationPropertySource(map);
		}

		public void run() {
			Binder binder = new Binder(source);
			value = binder.bind("spring.profiles.active", String[].class).get();
			assert value.length == 2;
		}
	}

	@State(Scope.Thread)
	public static class WrappedState extends EnvironmentState {
		private String[] value;
		private ConfigurationPropertySource source;

		protected void init() {
			super.init();
			source = new WrappedConfigurationPropertySource(environment);
		}

		public void run() {
			Binder binder = new Binder(source);
			value = binder.bind("spring.profiles.active", String[].class).get();
			assert value.length == 2;
		}
	}

	@State(Scope.Thread)
	public static class BinderState extends EnvironmentState {
		private String[] value;

		public void run() {
			Binder binder = Binder.get(this.environment);
			value = binder.bind("spring.profiles.active", String[].class).get();
			assert value.length == 2;
		}
	}

	public static void main(String[] args) {
		new WrappedState().run();
	}

	public static abstract class EnvironmentState {

		ConfigurableEnvironment environment;

		public EnvironmentState() {
			init();
		}

		protected void init() {
			environment = new StandardEnvironment();
			MutablePropertySources propertySources = environment.getPropertySources();
			propertySources.addLast(new MapPropertySource("profiles",
					Collections.singletonMap("spring.profiles.active", "one,two")));
		};

	}

}
