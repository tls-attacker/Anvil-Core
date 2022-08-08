package de.rub.nds.anvilcore.teststate.reporting;

public class DefaultTestSummaryFactory implements TestSummaryFactory{
    @Override
    public TestSummary getInstance() {
        return new TestSummary();
    }
}
