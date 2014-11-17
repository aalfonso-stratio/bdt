package com.stratio.assertions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.stratio.tests.utils.HttpResponse;
/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 *
 */
public class Assertions extends org.assertj.core.api.Assertions {
/**
 * Check if two HttpResponse are equals.
 * @param actual
 * @return
 */
    public static HttpResponseAssert assertThat(HttpResponse actual) {
        return new HttpResponseAssert(actual);
    }
    /**
     * Check if two WebElements are equals.
     * @param actual
     * @return
     */
    public static SeleniumAssert assertThat(WebElement actual) {
        return new SeleniumAssert(actual);
    }
    /**
     * Check if two WebDrivers are equals.
     * @param actual
     * @return
     */
    public static SeleniumAssert assertThat(WebDriver actual) {
        return new SeleniumAssert(actual);
    }

}