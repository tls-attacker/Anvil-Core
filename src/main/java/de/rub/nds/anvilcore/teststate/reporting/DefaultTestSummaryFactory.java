package de.rub.nds.anvilcore.teststate.reporting;

public class DefaultTestSummaryFactory implements AnvilReportFactory{
    @Override
    public AnvilReport getInstance() {
        return new AnvilReport();
    }
}
