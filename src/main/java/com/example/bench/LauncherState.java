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

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import com.example.demo.DemoApplication;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.ClassUtils;

@State(Scope.Benchmark)
public class LauncherState implements Runnable, Closeable {

	private static final Logger log = LoggerFactory.getLogger(LauncherState.class);

	private Closeable instance;

	private URLClassLoader loader;

	private ClassLoader orig;

	private Thread runThread;

	protected Throwable error;

	private long timeout = 120000;

	private Class<?> mainClass = DemoApplication.class;

	public void setMainClass(Class<?> mainClass) {
		this.mainClass = mainClass;
	}

	@Setup(Level.Trial)
	public void start() throws Exception {
		System.setProperty("server.port", "0");
	}
	
	public void shared() throws Exception {
		instance = (Closeable) mainClass.getConstructor().newInstance();
		run();
	}

	public void isolated() throws Exception {
		Class<?> mainClass = loadMainClass(this.mainClass);
		instance = (Closeable) mainClass.getConstructor().newInstance();
		this.runThread = new Thread(() -> {
			try {
				run();
			}
			catch (Throwable ex) {
				error = ex;
			}
		});
		this.runThread.start();
		try {
			this.runThread.join(timeout);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	@TearDown(Level.Invocation)
	public void close() throws IOException {
		// CachedIntrospectionResults.clearClassLoader(getClass().getClassLoader());
		if (instance != null) {
			instance.close();
		}
		if (runThread != null) {
			runThread.setContextClassLoader(null);
			runThread = null;
		}
		if (orig != null) {
			ClassUtils.overrideThreadContextClassLoader(orig);
		}
		if (loader != null) {
			try {
				loader.close();
				loader = null;
			}
			catch (Exception e) {
				log.error("Failed to close loader", e);
			}
		}
		System.gc();
	}

	@Override
	public void run() {
		((Runnable) instance).run();
	}

	private Class<?> loadMainClass(Class<?> type) throws ClassNotFoundException {
		URL[] urls = ((URLClassLoader) getClass().getClassLoader()).getURLs();
		loader = new URLClassLoader(urls, getClass().getClassLoader().getParent());
		orig = ClassUtils.overrideThreadContextClassLoader(loader);
		return loader.loadClass(type.getName());
	}

}
