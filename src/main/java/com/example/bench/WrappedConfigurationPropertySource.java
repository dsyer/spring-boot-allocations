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

import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.core.env.ConfigurableEnvironment;

public class WrappedConfigurationPropertySource implements ConfigurationPropertySource {

	private ConfigurableEnvironment environment;
	private Origin origin;

	public WrappedConfigurationPropertySource(ConfigurableEnvironment environment) {
		this.environment = environment;
		this.origin = Origin.from(environment);
	}

	@Override
	public ConfigurationProperty getConfigurationProperty(
			ConfigurationPropertyName name) {
		String key = name.toString();
		if (this.environment.containsProperty(key)) {
			return new ConfigurationProperty(name,
					(Object) this.environment.getProperty(key), this.origin);
		}
		return null;
	}

	@Override
	public Object getUnderlyingSource() {
		return environment;
	}

}