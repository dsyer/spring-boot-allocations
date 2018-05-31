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

import com.example.demo.DemoApplication;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@State(Scope.Benchmark)
public class LauncherState {

	private static final Logger log = LoggerFactory.getLogger(LauncherState.class);

	private DemoApplication instance = new DemoApplication();

	@Setup(Level.Trial)
	public void start() throws Exception {
		System.setProperty("server.port", "0");
	}

	public void isolated() {
		try {
			instance.isolated();
		}
		catch (Exception e) {
			log.error("Error starting context", e);
		}
	}

	public void run() {
		try {
			instance.run();
		}
		catch (Exception e) {
			log.error("Error starting context", e);
		}
	}

	@TearDown(Level.Invocation)
	public void close() {
		if (instance != null) {
			try {
				instance.close();
			}
			catch (Exception e) {
				log.error("Failed to close context", e);
			}
		}
	}

}
