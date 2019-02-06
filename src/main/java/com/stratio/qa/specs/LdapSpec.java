/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.qa.specs;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.mongodb.DBObject;
import com.stratio.qa.assertions.DBObjectsAssert;
import com.stratio.qa.utils.PreviousWebElements;
import com.stratio.qa.utils.ThreadProperty;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.formatter.model.DataTableRow;
import org.apache.zookeeper.KeeperException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Fail;
import org.assertj.core.api.WritableAssertionInfo;
import org.json.JSONArray;
import org.ldaptive.LdapAttribute;
import org.ldaptive.SearchRequest;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

import com.jayway.jsonpath.JsonPath;

import static com.stratio.qa.assertions.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

/**
 * Generic LDAP Specs.
 */
public class LdapSpec extends BaseGSpec {

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public LdapSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * Connect to LDAP.
     */
    @Given("^I connect to LDAP$")
    public void connectLDAP() {
        commonspec.getLdapUtils().connect();
    }

    /**
     * Search for a LDAP object
     */
    @When("^I search in LDAP using the filter '(.+?)' and the baseDn '(.+?)'$")
    public void searchLDAP(String filter, String baseDn) throws Exception {
        this.commonspec.setPreviousLdapResults(commonspec.getLdapUtils().search(new SearchRequest(baseDn, filter)));
    }

    /**
     * Checks if the previous LDAP search contained a single Entry with a specific attribute and an expected value
     *
     * @param attributeName The name of the attribute to look for in the LdapEntry
     * @param expectedValue The expected value of the attribute
     */
    @Then("^the LDAP entry contains the attribute '(.+?)' with the value '(.+?)'$")
    public void ldapEntryContains(String attributeName, String expectedValue) {
        if (this.commonspec.getPreviousLdapResults().isPresent()) {
            Assertions.assertThat(this.commonspec.getPreviousLdapResults().get().getEntry().getAttribute(attributeName).getStringValues()).contains(expectedValue);
        } else {
            fail("No previous LDAP results were stored in memory");
        }
    }

}
