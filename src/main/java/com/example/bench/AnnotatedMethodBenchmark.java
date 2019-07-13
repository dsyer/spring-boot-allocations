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

import org.springframework.context.annotation.Bean;
import org.springframework.core.type.AnnotationMetadata;

@Measurement(iterations = 5)
@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)
public class AnnotatedMethodBenchmark {

	@Benchmark
	public void count10(SimpleState state) throws Exception {
		state.setTarget(Bogus1.class);
		state.run();
	}

	@Benchmark
	public void count20(SimpleState state) throws Exception {
		state.setTarget(Bogus2.class);
		state.run();
	}

	@Benchmark
	public void count30(SimpleState state) throws Exception {
		state.setTarget(Bogus3.class);
		state.run();
	}

	@State(Scope.Thread)
	public static class SimpleState extends AnnotatedMethodState {
	}
	
	public static void main(String[] args) {
		SimpleState state = new SimpleState();
		state.setTarget(Bogus3.class);
		state.run();
	}

	public static abstract class AnnotatedMethodState {

		private Class<?> target = Bogus1.class;

		public void setTarget(Class<?> target) {
			this.target = target;
		}

		@TearDown(Level.Invocation)
		public void clear() {
		}

		public void run() {
			if (AnnotationMetadata.introspect(target).hasAnnotatedMethods(Bean.class.getName())) {
				throw new IllegalStateException();
			}
		}

	}

	static class Bogus1 {
		public void method0() {}
		public void method1() {}
		public void method2() {}
		public void method3() {}
		public void method4() {}
		public void method5() {}
		public void method6() {}
		public void method7() {}
		public void method8() {}
		public void method9() {}
	}

	static class Bogus2 {
		public void method0() {}
		public void method1() {}
		public void method2() {}
		public void method3() {}
		public void method4() {}
		public void method5() {}
		public void method6() {}
		public void method7() {}
		public void method8() {}
		public void method9() {}
		public void method10() {}
		public void method11() {}
		public void method12() {}
		public void method13() {}
		public void method14() {}
		public void method15() {}
		public void method16() {}
		public void method17() {}
		public void method18() {}
		public void method19() {}
	}

	static class Bogus3 {
		public void method0() {}
		public void method1() {}
		public void method2() {}
		public void method3() {}
		public void method4() {}
		public void method5() {}
		public void method6() {}
		public void method7() {}
		public void method8() {}
		public void method9() {}
		public void method10() {}
		public void method11() {}
		public void method12() {}
		public void method13() {}
		public void method14() {}
		public void method15() {}
		public void method16() {}
		public void method17() {}
		public void method18() {}
		public void method19() {}
		public void method30() {}
		public void method31() {}
		public void method32() {}
		public void method33() {}
		public void method34() {}
		public void method35() {}
		public void method36() {}
		public void method37() {}
		public void method38() {}
		public void method39() {}
	}
}
