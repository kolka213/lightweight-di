package de.schwabe.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Container {
    private final HashMap<String, Object> beans = new HashMap<>();
    private Set<String> packagesToScan = new HashSet<>();

    public void addBean(String beanName, Object bean) {
        this.beans.put(beanName, bean);
    }

    public void addPackage(String packageName) {
        this.packagesToScan.add(packageName);
    }

    public void start() {

    }

    public <T> T getBeanByType(Class<T> beanType) {
        return this.beans.values()
                .stream()
                .filter(beanType::isInstance)
                .map(beanType::cast)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bean of class %s not found".formatted(beanType.getName())));
    }
}
