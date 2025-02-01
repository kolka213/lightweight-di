package de.schwabe.core;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Field;
import java.util.*;

public class Container {
    private final HashMap<String, Object> beans = new HashMap<>();
    private final Set<String> packagesToScan = new HashSet<>();
    private final FilterBuilder filterBuilder = new FilterBuilder();

    public void addBean(String beanName, Object bean) {
        if (Number.class.isAssignableFrom(bean.getClass())) {
            this.registerBean(beanName, bean);
        } else {
            this.instantiateAndRegisterBean(beanName, bean.getClass());
        }
    }

    public void addPackage(String packageName) {
        this.packagesToScan.add(packageName);
        this.filterBuilder.includePackage(packageName);
    }

    public void start() {
        this.addPackage(this.getClass().getPackageName());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(this.packagesToScan.toArray(new String[0]))
                .filterInputsBy(this.filterBuilder)
        );

        Set<Class<?>> reflectedBeans = new HashSet<>(reflections.get(Scanners.TypesAnnotated.with(Bean.class).asClass()));
        for (Class<?> beanClass : reflectedBeans) {
            instantiateAndRegisterBean(null, beanClass);
        }
    }

    private void instantiateAndRegisterBean(String beanName, Class<?> beanClass) {
        try {
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            injectFields(bean, beanClass);
            registerBean(beanName, bean);
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating bean %s".formatted(beanClass.getName()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void injectFields(Object bean, Class<?> beanClass) throws IllegalAccessException {
        Set<Field> injectedFields = ReflectionUtils.getFields(beanClass, field -> field.isAnnotationPresent(Inject.class));
        for (Field field : injectedFields) {
            injectField(bean, field);
        }
    }

    private void registerBean(String beanName, Object bean) {
        beanName = Objects.requireNonNullElseGet(beanName, () -> bean.getClass().getSimpleName());
        Object previousObject = this.beans.putIfAbsent(beanName, bean);
        if (Objects.nonNull(previousObject)) {
            throw new RuntimeException("Bean with name %s already exists".formatted(beanName));
        }
    }

    private void injectField(Object bean, Field field) throws IllegalAccessException {
        Named namedAnnotation = field.getAnnotation(Named.class);
        Object dependency = (namedAnnotation != null) ? this.beans.get(namedAnnotation.value()) : findCompatibleBean(field.getType());

        if (dependency == null) {
            dependency = findCompatibleBean(field.getType());
        }

        if (dependency == null) {
            throw new RuntimeException("No suitable bean found for injection: " + field.getType().getName());
        }

        field.setAccessible(true);
        field.set(bean, dependency);
    }

    private Object findCompatibleBean(Class<?> fieldType) {
        List<Object> compatibleBeans = this.beans.values().stream()
                .filter(bean -> fieldType.isAssignableFrom(bean.getClass()))
                .toList();

        if (compatibleBeans.size() == 1) return compatibleBeans.getFirst();
        else if (compatibleBeans.isEmpty()) return null;
        else throw new RuntimeException("Ambiguous injection for type %s".formatted(fieldType.getName()));
    }

    public <T> T getBeanByType(Class<T> beanType) {
        Named namedAnnotation = beanType.getAnnotation(Named.class);
        if (namedAnnotation != null) {
            return beanType.cast(this.beans.get(namedAnnotation.value()));
        }
        return this.beans.values().stream()
                .filter(beanType::isInstance)
                .map(beanType::cast)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bean of class %s not found".formatted(beanType.getName())));
    }
}