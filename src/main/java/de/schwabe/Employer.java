package de.schwabe;

import de.schwabe.core.Bean;
import de.schwabe.core.Inject;
import de.schwabe.core.Named;

@Bean
@Named("queo")
public class Employer {

    @Inject
    @Named("pascalSchwabe")
    public Employee employee;
}
