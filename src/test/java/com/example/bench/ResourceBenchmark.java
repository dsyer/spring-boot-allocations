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

import java.net.MalformedURLException;

import jmh.mbr.junit5.Microbenchmark;
import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

@Measurement(iterations = 5)
@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)
@Microbenchmark
public class ResourceBenchmark {

	@Benchmark
	@Testable
	public void simple(SimpleState state) throws Exception {
		state.run();
	}

	@Benchmark
	@Testable
	public void dirty(DirtyState state) throws Exception {
		state.run();
	}

	@State(Scope.Thread)
	public static class DirtyState extends ConfigFileState {

		@Override
		protected Resource create() {
			try {
				return new DirtyFileUrlResource("src/main/resources/application.properties");
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}

	}

	@State(Scope.Thread)
	public static class SimpleState extends ConfigFileState {

	}

	public static void main(String[] args) {
		new SimpleState().run();
	}

	public static abstract class ConfigFileState {

		private int count;

		protected Resource create() {
			try {
				return new FileUrlResource("src/main/resources/application.properties");
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}

		@TearDown(Level.Invocation)
		public void clear() {
		}

		public void run() {
			Resource resource = create();
			if (resource.exists()) {
				count++;
			}
		}

	}

	@SpringBootApplication
	static class MyApplication {

	}

}
