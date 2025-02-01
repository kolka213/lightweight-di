package de.schwabe;

import de.schwabe.core.Container;
import de.schwabe.demo.AmbiguousConsumer;
import de.schwabe.demo.DemoImpl1;
import de.schwabe.demo.DemoImpl2;
import de.schwabe.demo.NamedConsumer;
import de.schwabe.injection.InjectionTargetNamed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyInjectionTest {

    @Test
    public void testInjectWithNamedInjection() {
        Integer beanA = 1;
        Integer beanB = 0;
        Container container = new Container();
        container.addPackage("de.schwabe.injection");
        container.addBean("a", beanA);
        container.addBean("b", beanB);
        container.start();
        InjectionTargetNamed injectionTargetNamed =
                container.getBeanByType(InjectionTargetNamed.class);
        assertEquals(beanA, injectionTargetNamed.valueA);
        assertEquals(beanB, injectionTargetNamed.valueB);
    }

    @Test
    void testNamedInjection() {
        Container container = new Container();
        container.addPackage("de.schwabe.demo");

        container.addBean("demoImpl1", new DemoImpl1());
        container.addBean("myDemo", new DemoImpl2());
        container.addBean("namedConsumer", new NamedConsumer());

        container.start();

        NamedConsumer consumer = container.getBeanByType(NamedConsumer.class);

        assertNotNull(consumer.demo);
        assertInstanceOf(DemoImpl2.class, consumer.demo, "NamedConsumer should get DemoImpl2 injected");
    }

    @Test
    void testAmbiguousInjectionShouldFail() {
        Container container = new Container();
        container.addPackage("de.schwabe.demo");

        container.addBean("demoImpl1", new DemoImpl1());
        container.addBean("demoImpl2", new DemoImpl2());

        assertThrows(RuntimeException.class, () -> container.addBean("ambiguousConsumer", new AmbiguousConsumer()),
                "Ambiguous injection should fail");
    }

    @Test
    void testDuplicateNamedBeansShouldFail() {
        Container container = new Container();
        container.addPackage("de.schwabe.demo");

        container.addBean("myDemo", new DemoImpl1());

        assertThrows(RuntimeException.class, () -> container.addBean("myDemo", new DemoImpl2()),
                "Duplicate named beans should fail");
    }
}
