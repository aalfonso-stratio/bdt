package com.stratio.qa.utils;

public enum ETCHOSTSManagementUtil {
    INSTANCE;

    private final ETCHOSTSManagementUtils etcHostsUtils = new ETCHOSTSManagementUtils();

    public ETCHOSTSManagementUtils getETCHOSTSManagementUtils() {
        return etcHostsUtils;
    }
}
