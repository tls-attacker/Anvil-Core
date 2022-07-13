/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.anvilcore.junit.simpletest;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;


public class SimpleTestManagerContainer {
    
    private static SimpleTestManagerContainer reference;
    
    private List<SimpleTestManager> testManagers;
    
    private SimpleTestManagerContainer(){ 
        testManagers = new ArrayList<>();
    }
    
    public static SimpleTestManagerContainer getInstance() {
        if(reference == null) {
            reference = new SimpleTestManagerContainer();
        }
        return reference;
    }
    
    public SimpleTestManager getManagerByExtension(ExtensionContext extensionContext){
        for(SimpleTestManager manager: testManagers) {
            if(manager.getTestClass().equals(extensionContext.getRequiredTestClass().getName()) 
                    && manager.getTestMethod().equals(extensionContext.getRequiredTestMethod().getName())){
                return manager;
            }
        }
        return null;
    }
    
    public void addManagerByExtensionContext(ExtensionContext extensionContext, int testCount){
        SimpleTestManager presentManager = getManagerByExtension(extensionContext);
        if(presentManager == null) {
            testManagers.add(new SimpleTestManager(testCount, extensionContext.getRequiredTestMethod().getName(), extensionContext.getRequiredTestClass().getName()));
        } else {
            throw new RuntimeException("Attempted to add another SimpleTestManager for already registered test!");
        }
    }

}
