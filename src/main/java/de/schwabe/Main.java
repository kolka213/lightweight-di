package de.schwabe;

import de.schwabe.core.Container;

public class Main {
    public static void main(String[] args) {
        Container container = new Container();
        container.addPackage("de.schwabe");
        container.excludePackage("de.schwabe.injection");
        container.start();
    }
}