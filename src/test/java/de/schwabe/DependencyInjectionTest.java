package de.schwabe;

import de.schwabe.core.Container;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DependencyInjectionTest {

    @Test
    public void testInjectWithNamedInjection() {
        Integer beanA = 1;
        Integer beanB = 0;
        Container container = new Container();
        container.addPackage("de.schwabe");
        container.addBean("a", beanA);
        container.addBean("b", beanB);
        container.start();
        InjectionTargetNamed injectionTargetNamed =
                container.getBeanByType(InjectionTargetNamed.class);
        assertEquals(beanA, injectionTargetNamed.valueA);
        assertEquals(beanB, injectionTargetNamed.valueB);
    }
}
