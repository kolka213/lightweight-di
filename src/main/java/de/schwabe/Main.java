package de.schwabe;

import de.schwabe.core.Container;
import de.schwabe.core.Named;

public class Main {
    public static void main(String[] args) {
        Container container = new Container();
        container.addPackage("de.schwabe");
        container.excludePackage("de.schwabe.injection");
        container.start();

        Employer employer = container.getBeanByType(Employer.class);
        String employerName = employer.getClass().getAnnotation(Named.class).value();
        String employeeName = employer.employee.getClass().getAnnotation(Named.class).value();
        System.out.println(employeeName + " -> " + employerName);

    }
}