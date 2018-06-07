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

package com.example.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.asm.Type;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MethodMetadataReadingVisitor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
public class CopyMetadataReader implements MetadataReader {

	private String path;

	private CopyAnnotationMetadata metadata;

	@SuppressWarnings("unused")
	private CopyMetadataReader() {
	}

	public CopyMetadataReader(MetadataReader copy) {
	}

	public CopyMetadataReader(ClassPathResource resource,
			AnnotationMetadataReadingVisitor annotationMetadata) {
		path = resource.getPath();
		metadata = new CopyAnnotationMetadata(annotationMetadata);
	}

	@Override
	public Resource getResource() {
		return new ClassPathResource(path);
	}

	@Override
	public ClassMetadata getClassMetadata() {
		return metadata;
	}

	@Override
	public AnnotationMetadata getAnnotationMetadata() {
		return metadata;
	}

	static class CopyAnnotationMetadata implements ClassMetadata, AnnotationMetadata {

		private ClassLoader classLoader = getClass().getClassLoader();
		private String className;
		private String enclosingClassName;
		private String[] interfaceNames;
		private String[] memberClassNames;
		private String superClassName;
		private boolean enclosingClass;
		private boolean superClass;
		private boolean abstractness;
		private boolean annotation;
		private boolean concrete;
		private boolean finalness;
		private boolean independenct;
		private boolean interfaceness;
		private Set<String> annotationTypes;
		private Map<String, Set<String>> metaAnnotationMap;
		private LinkedMultiValueMap<String, AnnotationAttributes> attributesMap;
		private Set<MethodMetadata> methodMetadataSet;

		@SuppressWarnings("unused")
		private CopyAnnotationMetadata() {
		}

		@SuppressWarnings("unchecked")
		CopyAnnotationMetadata(AnnotationMetadataReadingVisitor annotationMetadata) {
			className = annotationMetadata.getClassName();
			enclosingClassName = annotationMetadata.getEnclosingClassName();
			interfaceNames = annotationMetadata.getInterfaceNames();
			memberClassNames = annotationMetadata.getMemberClassNames();
			superClassName = annotationMetadata.getSuperClassName();
			enclosingClass = annotationMetadata.hasEnclosingClass();
			superClass = annotationMetadata.hasSuperClass();
			abstractness = annotationMetadata.isAbstract();
			annotation = annotationMetadata.isAnnotation();
			concrete = annotationMetadata.isConcrete();
			finalness = annotationMetadata.isFinal();
			independenct = annotationMetadata.isIndependent();
			interfaceness = annotationMetadata.isInterface();
			annotationTypes = annotationMetadata.getAnnotationTypes();
			metaAnnotationMap = (Map<String, Set<String>>) getField(annotationMetadata,
					"metaAnnotationMap");
			attributesMap = AnnotationReadingVisitorUtils.attributesMap(
					(LinkedMultiValueMap<String, AnnotationAttributes>) getField(
							annotationMetadata, "attributesMap"));
			methodMetadataSet = MethodReadingVisitorUtils
					.methodMetadata((Set<MethodMetadata>) getField(annotationMetadata,
							"methodMetadataSet"));

		}

		private Object getField(Object target, String name) {
			Field field = ReflectionUtils.findField(target.getClass(), name);
			ReflectionUtils.makeAccessible(field);
			return ReflectionUtils.getField(field, target);
		}

		@Override
		public boolean isAnnotated(String annotationName) {
			return (!AnnotationUtils.isInJavaLangAnnotationPackage(annotationName)
					&& this.attributesMap.containsKey(annotationName));
		}

		@Override
		@Nullable
		public AnnotationAttributes getAnnotationAttributes(String annotationName) {
			return getAnnotationAttributes(annotationName, false);
		}

		@Override
		@Nullable
		public AnnotationAttributes getAnnotationAttributes(String annotationName,
				boolean classValuesAsString) {
			AnnotationAttributes raw = AnnotationReadingVisitorUtils
					.getMergedAnnotationAttributes(this.attributesMap,
							this.metaAnnotationMap, annotationName);
			if (raw == null) {
				return null;
			}
			return AnnotationReadingVisitorUtils.convertClassValues(
					"class '" + getClassName() + "'", this.classLoader, raw,
					classValuesAsString);
		}

		@Override
		@Nullable
		public MultiValueMap<String, Object> getAllAnnotationAttributes(
				String annotationName) {
			return getAllAnnotationAttributes(annotationName, false);
		}

		@Override
		@Nullable
		public MultiValueMap<String, Object> getAllAnnotationAttributes(
				String annotationName, boolean classValuesAsString) {
			MultiValueMap<String, Object> allAttributes = new LinkedMultiValueMap<>();
			List<AnnotationAttributes> attributes = this.attributesMap
					.get(annotationName);
			if (attributes == null) {
				return null;
			}
			for (AnnotationAttributes raw : attributes) {
				for (Map.Entry<String, Object> entry : AnnotationReadingVisitorUtils
						.convertClassValues("class '" + getClassName() + "'",
								this.classLoader, raw, classValuesAsString)
						.entrySet()) {
					allAttributes.add(entry.getKey(), entry.getValue());
				}
			}
			return allAttributes;
		}

		@Override
		public Set<String> getAnnotationTypes() {
			return annotationTypes;
		}

		@Override
		public Set<String> getMetaAnnotationTypes(String annotationName) {
			return this.metaAnnotationMap.get(annotationName);
		}

		@Override
		public boolean hasAnnotation(String annotationName) {
			return annotationTypes.contains(annotationName);
		}

		@Override
		public boolean hasMetaAnnotation(String metaAnnotationType) {
			Collection<Set<String>> allMetaTypes = this.metaAnnotationMap.values();
			for (Set<String> metaTypes : allMetaTypes) {
				if (metaTypes.contains(metaAnnotationType)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasAnnotatedMethods(String annotationName) {
			for (MethodMetadata methodMetadata : this.methodMetadataSet) {
				if (methodMetadata.isAnnotated(annotationName)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
			Set<MethodMetadata> annotatedMethods = new LinkedHashSet<>(4);
			for (MethodMetadata methodMetadata : this.methodMetadataSet) {
				if (methodMetadata.isAnnotated(annotationName)) {
					annotatedMethods.add(methodMetadata);
				}
			}
			return annotatedMethods;
		}

		@Override
		public String getClassName() {
			return className;
		}

		@Override
		public boolean isInterface() {
			return interfaceness;
		}

		@Override
		public boolean isAnnotation() {
			return annotation;
		}

		@Override
		public boolean isAbstract() {
			return abstractness;
		}

		@Override
		public boolean isConcrete() {
			return concrete;
		}

		@Override
		public boolean isFinal() {
			return finalness;
		}

		@Override
		public boolean isIndependent() {
			return independenct;
		}

		@Override
		public boolean hasEnclosingClass() {
			return enclosingClass;
		}

		@Override
		public String getEnclosingClassName() {
			return enclosingClassName;
		}

		@Override
		public boolean hasSuperClass() {
			return superClass;
		}

		@Override
		public String getSuperClassName() {
			return superClassName;
		}

		@Override
		public String[] getInterfaceNames() {
			return interfaceNames;
		}

		@Override
		public String[] getMemberClassNames() {
			return memberClassNames;
		}

	}
}

abstract class AnnotationReadingVisitorUtils {

	public static LinkedMultiValueMap<String, AnnotationAttributes> attributesMap(
			LinkedMultiValueMap<String, AnnotationAttributes> values) {
		LinkedMultiValueMap<String, AnnotationAttributes> result = new LinkedMultiValueMap<String, AnnotationAttributes>(
				4);
		if (values != null) {
			for (Entry<String, List<AnnotationAttributes>> entry : values.entrySet()) {
				List<AnnotationAttributes> list = new ArrayList<>(
						entry.getValue().size());
				for (AnnotationAttributes item : entry.getValue()) {
					list.add(copy(item));
				}
				result.put(entry.getKey(), list);
			}
		}
		return result;
	}

	private static AnnotationAttributes copy(AnnotationAttributes original) {
		AnnotationAttributes result = new AnnotationAttributes(original);
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof AnnotationAttributes) {
				value = copy((AnnotationAttributes) value);
			}
			else if (value instanceof AnnotationAttributes[]) {
				AnnotationAttributes[] values = (AnnotationAttributes[]) value;
				for (int i = 0; i < values.length; i++) {
					values[i] = copy(values[i]);
				}
				value = values;
			}
			else if (value instanceof Type) {
				value = new CopyType(((Type) value).getClassName());
			}
			else if (value instanceof Type[]) {
				Type[] array = (Type[]) value;
				Object[] convArray = new CopyType[array.length];
				for (int i = 0; i < array.length; i++) {
					convArray[i] = new CopyType(array[i].getClassName());
				}
				value = convArray;
			}
			entry.setValue(value);
		}
		return result;
	}

	public static AnnotationAttributes convertClassValues(Object annotatedElement,
			@Nullable ClassLoader classLoader, AnnotationAttributes original,
			boolean classValuesAsString) {

		AnnotationAttributes result = new AnnotationAttributes(original);
		AnnotationUtils.postProcessAnnotationAttributes(annotatedElement, result,
				classValuesAsString);

		for (Map.Entry<String, Object> entry : result.entrySet()) {
			try {
				Object value = entry.getValue();
				if (value instanceof AnnotationAttributes) {
					value = convertClassValues(annotatedElement, classLoader,
							(AnnotationAttributes) value, classValuesAsString);
				}
				else if (value instanceof AnnotationAttributes[]) {
					AnnotationAttributes[] values = (AnnotationAttributes[]) value;
					for (int i = 0; i < values.length; i++) {
						values[i] = convertClassValues(annotatedElement, classLoader,
								values[i], classValuesAsString);
					}
					value = values;
				}
				else if (value instanceof CopyType) {
					value = (classValuesAsString ? ((CopyType) value).getClassName()
							: ClassUtils.forName(((CopyType) value).getClassName(),
									classLoader));
				}
				else if (value instanceof CopyType[]) {
					CopyType[] array = (CopyType[]) value;
					Object[] convArray = (classValuesAsString ? new String[array.length]
							: new Class<?>[array.length]);
					for (int i = 0; i < array.length; i++) {
						convArray[i] = (classValuesAsString ? array[i].getClassName()
								: ClassUtils.forName(array[i].getClassName(),
										classLoader));
					}
					value = convArray;
				}
				else if (classValuesAsString) {
					if (value instanceof Class) {
						value = ((Class<?>) value).getName();
					}
					else if (value instanceof Class[]) {
						Class<?>[] clazzArray = (Class<?>[]) value;
						String[] newValue = new String[clazzArray.length];
						for (int i = 0; i < clazzArray.length; i++) {
							newValue[i] = clazzArray[i].getName();
						}
						value = newValue;
					}
				}
				entry.setValue(value);
			}
			catch (Throwable ex) {
				// Class not found - can't resolve class reference in annotation
				// attribute.
				result.put(entry.getKey(), ex);
			}
		}

		return result;
	}

	/**
	 * Retrieve the merged attributes of the annotation of the given type, if any, from
	 * the supplied {@code attributesMap}.
	 * <p>
	 * Annotation attribute values appearing <em>lower</em> in the annotation hierarchy
	 * (i.e., closer to the declaring class) will override those defined <em>higher</em>
	 * in the annotation hierarchy.
	 * @param attributesMap the map of annotation attribute lists, keyed by annotation
	 * type name
	 * @param metaAnnotationMap the map of meta annotation relationships, keyed by
	 * annotation type name
	 * @param annotationName the fully qualified class name of the annotation type to look
	 * for
	 * @return the merged annotation attributes, or {@code null} if no matching annotation
	 * is present in the {@code attributesMap}
	 * @since 4.0.3
	 */
	@Nullable
	public static AnnotationAttributes getMergedAnnotationAttributes(
			LinkedMultiValueMap<String, AnnotationAttributes> attributesMap,
			Map<String, Set<String>> metaAnnotationMap, String annotationName) {

		// Get the unmerged list of attributes for the target annotation.
		List<AnnotationAttributes> attributesList = attributesMap.get(annotationName);
		if (attributesList == null || attributesList.isEmpty()) {
			return null;
		}

		// To start with, we populate the result with a copy of all attribute values
		// from the target annotation. A copy is necessary so that we do not
		// inadvertently mutate the state of the metadata passed to this method.
		AnnotationAttributes result = new AnnotationAttributes(attributesList.get(0));

		Set<String> overridableAttributeNames = new HashSet<>(result.keySet());
		overridableAttributeNames.remove(AnnotationUtils.VALUE);

		// Since the map is a LinkedMultiValueMap, we depend on the ordering of
		// elements in the map and reverse the order of the keys in order to traverse
		// "down" the annotation hierarchy.
		List<String> annotationTypes = new ArrayList<>(attributesMap.keySet());
		Collections.reverse(annotationTypes);

		// No need to revisit the target annotation type:
		annotationTypes.remove(annotationName);

		for (String currentAnnotationType : annotationTypes) {
			List<AnnotationAttributes> currentAttributesList = attributesMap
					.get(currentAnnotationType);
			if (!ObjectUtils.isEmpty(currentAttributesList)) {
				Set<String> metaAnns = metaAnnotationMap.get(currentAnnotationType);
				if (metaAnns != null && metaAnns.contains(annotationName)) {
					AnnotationAttributes currentAttributes = currentAttributesList.get(0);
					for (String overridableAttributeName : overridableAttributeNames) {
						Object value = currentAttributes.get(overridableAttributeName);
						if (value != null) {
							// Store the value, potentially overriding a value from an
							// attribute
							// of the same name found higher in the annotation hierarchy.
							result.put(overridableAttributeName, value);
						}
					}
				}
			}
		}

		return result;
	}

}

abstract class MethodReadingVisitorUtils {

	public static Set<MethodMetadata> methodMetadata(Set<MethodMetadata> field) {
		HashSet<MethodMetadata> result = new HashSet<>(field.size());
		for (MethodMetadata methodMetadata : field) {
			result.add(copy(methodMetadata));
		}
		return result;
	}

	private static MethodMetadata copy(MethodMetadata methodMetadata) {
		if (methodMetadata instanceof MethodMetadataReadingVisitor) {
			return new CopyMethodMetadata((MethodMetadataReadingVisitor) methodMetadata);
		}
		return methodMetadata;
	}

	static class CopyMethodMetadata implements MethodMetadata {

		private ClassLoader classLoader = getClass().getClassLoader();

		private Map<String, Set<String>> metaAnnotationMap;

		private LinkedMultiValueMap<String, AnnotationAttributes> attributesMap;

		private String methodName;

		private boolean abstractness;

		private boolean finalness;

		private boolean overridable;

		private boolean staticness;

		private String declaringClassName;

		private String returnTypeName;

		@SuppressWarnings("unused")
		private CopyMethodMetadata() {
		}

		@SuppressWarnings("unchecked")
		public CopyMethodMetadata(MethodMetadataReadingVisitor methodMetadata) {
			methodName = methodMetadata.getMethodName();
			abstractness = methodMetadata.isAbstract();
			finalness = methodMetadata.isFinal();
			overridable = methodMetadata.isOverridable();
			staticness = methodMetadata.isStatic();
			declaringClassName = methodMetadata.getDeclaringClassName();
			returnTypeName = methodMetadata.getReturnTypeName();
			metaAnnotationMap = (Map<String, Set<String>>) getField(methodMetadata,
					"metaAnnotationMap");
			attributesMap = AnnotationReadingVisitorUtils.attributesMap(
					(LinkedMultiValueMap<String, AnnotationAttributes>) getField(
							methodMetadata, "attributesMap"));

		}

		private Object getField(Object target, String name) {
			Field field = ReflectionUtils.findField(target.getClass(), name);
			ReflectionUtils.makeAccessible(field);
			return ReflectionUtils.getField(field, target);
		}

		@Override
		public String getMethodName() {
			return this.methodName;
		}

		@Override
		public boolean isAbstract() {
			return this.abstractness;
		}

		@Override
		public boolean isStatic() {
			return this.staticness;
		}

		@Override
		public boolean isFinal() {
			return this.finalness;
		}

		@Override
		public boolean isOverridable() {
			return this.overridable;
		}

		@Override
		public boolean isAnnotated(String annotationName) {
			return this.attributesMap.containsKey(annotationName);
		}

		@Override
		@Nullable
		public AnnotationAttributes getAnnotationAttributes(String annotationName) {
			return getAnnotationAttributes(annotationName, false);
		}

		@Override
		@Nullable
		public AnnotationAttributes getAnnotationAttributes(String annotationName,
				boolean classValuesAsString) {
			AnnotationAttributes raw = AnnotationReadingVisitorUtils
					.getMergedAnnotationAttributes(this.attributesMap,
							this.metaAnnotationMap, annotationName);
			if (raw == null) {
				return null;
			}
			return AnnotationReadingVisitorUtils.convertClassValues(
					"method '" + getMethodName() + "'", this.classLoader, raw,
					classValuesAsString);
		}

		@Override
		@Nullable
		public MultiValueMap<String, Object> getAllAnnotationAttributes(
				String annotationName) {
			return getAllAnnotationAttributes(annotationName, false);
		}

		@Override
		@Nullable
		public MultiValueMap<String, Object> getAllAnnotationAttributes(
				String annotationName, boolean classValuesAsString) {
			if (!this.attributesMap.containsKey(annotationName)) {
				return null;
			}
			MultiValueMap<String, Object> allAttributes = new LinkedMultiValueMap<>();
			List<AnnotationAttributes> attributesList = this.attributesMap
					.get(annotationName);
			if (attributesList != null) {
				for (AnnotationAttributes annotationAttributes : attributesList) {
					AnnotationAttributes convertedAttributes = AnnotationReadingVisitorUtils
							.convertClassValues("method '" + getMethodName() + "'",
									this.classLoader, annotationAttributes,
									classValuesAsString);
					convertedAttributes.forEach(allAttributes::add);
				}
			}
			return allAttributes;
		}

		@Override
		public String getDeclaringClassName() {
			return this.declaringClassName;
		}

		@Override
		public String getReturnTypeName() {
			return this.returnTypeName;
		}

	}

}

class CopyType {

	private String name;

	@SuppressWarnings("unused")
	private CopyType() {
	}

	public CopyType(String name) {
		this.name = name;
	}

	public String getClassName() {
		return name;
	}

}