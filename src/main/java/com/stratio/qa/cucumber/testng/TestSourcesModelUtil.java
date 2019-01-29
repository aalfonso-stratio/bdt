package com.stratio.qa.cucumber.testng;

/**
 * TestSourcesModel class (Singleton).
 */
public enum TestSourcesModelUtil {
    INSTANCE;

    private final TestSourcesModel testSourcesModel = new TestSourcesModel();

    public TestSourcesModel getTestSourcesModel() {
        return testSourcesModel;
    }
}
