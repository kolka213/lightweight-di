package de.schwabe.demo;


import de.schwabe.core.Inject;
import de.schwabe.core.Named;

public class NamedConsumer {
    @Inject
    @Named("myDemo")  // Erwartet eine Instanz von DemoImpl2
    public Demo demo;
}
