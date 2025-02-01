package de.schwabe.core;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Container {
    private final HashMap<String, Object> beans = new HashMap<>();
    private final Set<String> packagesToScan = new HashSet<>();

    public void addBean(String beanName, Object bean) {
        this.beans.put(beanName, bean);
    }

    public void addPackage(String packageName) {
        this.packagesToScan.add(packageName);
    }

    public void start() {
        this.packagesToScan.add(this.getClass().getPackageName());

        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(this.packagesToScan.toArray(new String[0])));

        Set<Class<?>> reflectedBeans = new HashSet<>(reflections.get(Scanners.TypesAnnotated.with(Bean.class).asClass()));
        for (Class<?> beanClass : reflectedBeans) {
            try {
                Object bean = beanClass.getDeclaredConstructor().newInstance();

                Set<Field> injectedFields = ReflectionUtils.getFields(beanClass, field -> field.isAnnotationPresent(Inject.class));
                for (Field field : injectedFields) {
                    try {
                        Named namedAnnotation = field.getAnnotation(Named.class);
                        Class<?> classType = field.getType();
                        this.injectField(bean, field);
                        if (namedAnnotation != null) {
                            this.beans.put(namedAnnotation.value(), field.get(bean));
                        } else {
                            this.beans.put(classType.getSimpleName(), field.get(bean));
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error injecting field %s".formatted(field.getName()), e);
                    }
                }
                Named namedAnnotation = beanClass.getAnnotation(Named.class);
                if (namedAnnotation != null) {
                    this.beans.put(namedAnnotation.value(), bean);
                } else {
                    this.beans.put(beanClass.getSimpleName(), bean);
                }

            } catch (Exception e) {
                throw new RuntimeException("Error instantiating bean %s".formatted(beanClass.getName()), e);
            }
        }
    }

    private void injectField(Object bean, Field field) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        Named namedAnnotation = field.getAnnotation(Named.class);
        Object dependency;
        if (namedAnnotation != null) {
            dependency = this.beans.get(namedAnnotation.value());
            if (dependency == null) {
                dependency = findCompatibleBean(fieldType);
            }
        } else {
            dependency = findCompatibleBean(fieldType);
        }

        if (dependency == null) {
            throw new RuntimeException("No suitable bean found for injection: " + fieldType.getName());
        }

        field.setAccessible(true);
        field.set(bean, dependency);
    }

    private Object findCompatibleBean(Class<?> fieldType) {
        for (Object bean : this.beans.values()) {
            if (fieldType.isAssignableFrom(bean.getClass())) {
                return bean;
            }
        }
        return null;
    }


    public <T> T getBeanByType(Class<T> beanType) {
        Named namedAnnotation = beanType.getAnnotation(Named.class);
        if (namedAnnotation != null) {
            return beanType.cast(this.beans.get(namedAnnotation.value()));
        }
        return this.beans.values().stream().filter(beanType::isInstance).map(beanType::cast).findFirst().orElseThrow(() -> new RuntimeException("Bean of class %s not found".formatted(beanType.getName())));
    }
}
