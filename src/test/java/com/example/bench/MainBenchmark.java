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

import com.example.auto.AutoApplication;
import com.example.boot.BootApplication;
import com.example.func.BuncApplication;
import com.example.func.CuncApplication;
import com.example.func.FuncApplication;
import com.example.manual.ManualApplication;
import jmh.mbr.junit5.Microbenchmark;
import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.SingleShotTime)
@Microbenchmark
public class MainBenchmark {

	@Benchmark
	@Testable
	public void demo(LauncherState state) throws Exception {
		state.isolated();
	}

	@Benchmark
	@Testable
	public void boot(LauncherState state) throws Exception {
		state.setMainClass(BootApplication.class);
		state.isolated();
	}

	@Benchmark
	@Testable
	public void manual(LauncherState state) throws Exception {
		state.setMainClass(ManualApplication.class);
		state.isolated();
	}

	@Benchmark
	@Testable
	public void cunc(LauncherState state) throws Exception {
		state.setMainClass(CuncApplication.class);
		state.isolated();
	}

	@Benchmark
	@Testable
	public void bunc(LauncherState state) throws Exception {
		state.setMainClass(BuncApplication.class);
		state.isolated();
	}

	@Benchmark
	@Testable
	public void func(LauncherState state) throws Exception {
		state.setMainClass(FuncApplication.class);
		state.isolated();
	}

	@Benchmark
	@Testable
	public void auto(LauncherState state) throws Exception {
		state.setMainClass(AutoApplication.class);
		state.isolated();
	}

	@Benchmark
	@Testable
	public void shared(LauncherState state) throws Exception {
		state.shared();
	}

}
