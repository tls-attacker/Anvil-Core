package de.rub.nds.anvilcore.junit.simpletest;

public class SimpleTestManager {
    private int remainingTests;
    private final String testMethod;
    private final String testClass;

    public SimpleTestManager(int remainingTests, String testMethod, String testClass) {
        this.remainingTests = remainingTests;
        this.testMethod = testMethod;
        this.testClass = testClass;
    }

    public synchronized void testCompleted() {
        remainingTests--;
    }

    public synchronized boolean allTestsFinished() {
        return remainingTests == 0;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public String getTestClass() {
        return testClass;
    }
}
