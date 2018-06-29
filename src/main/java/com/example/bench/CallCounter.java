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

import java.util.LinkedHashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author Dave Syer
 *
 */
@Aspect
public class CallCounter {

	private Map<String, Integer> counts = new LinkedHashMap<>();

	@Around("execution(* org.springframework.asm.Type.getInternalName())")
	public Object count(ProceedingJoinPoint jp) throws Throwable {
		String result = (String) jp.proceed();
		counts.compute(result, (k, v) -> v!=null ? v+1 : 1);
		return result;
	}

	public void log() {
		counts.entrySet()
				.forEach(e -> System.err.println(e.getKey() + ": " + e.getValue()));
		System.err.println(
				"Count: " + counts.values().stream().mapToInt(Integer::intValue).sum());
	}

	static CallCounter instance = new CallCounter();

	public static CallCounter aspectOf() {
		return instance;
	}
}
