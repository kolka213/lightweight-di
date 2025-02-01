package de.schwabe;

import de.schwabe.core.Bean;
import de.schwabe.core.Inject;
import de.schwabe.core.Named;

@Bean
public class InjectionTargetNamed {
    @Inject
    @Named("a")
    public Integer valueA = 1;
    @Inject
    @Named("b")
    public Integer valueB;
}