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

package com.stratio.qa.utils;

import com.stratio.qa.cucumber.testng.ICucumberFormatter;
import com.stratio.qa.cucumber.testng.ICucumberReporter;
import com.stratio.qa.specs.BaseGSpec;
import com.stratio.qa.specs.HookGSpec;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.event.EventPublisher;
import cucumber.runtime.Match;
import gherkin.ast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CukesGHooks extends BaseGSpec implements ICucumberReporter, ICucumberFormatter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    Feature feature;

    public Scenario scenario;

    public CukesGHooks() {
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    public void uri(String uri) {
    }

    public void examples(Examples examples) {
    }

    public void startOfScenarioLifeCycle(Scenario scenario) {
    }

    public void done() {
    }

    public void close() {
    }

    public void eof() {
    }

    public void background(Background background) {
        logger.info("Background: {}", background.getName());
    }

    public void feature(Feature feature) {
        this.feature = feature;
        ThreadProperty.set("feature", feature.getName());
    }

    public void scenario(Scenario scenario) {
        this.scenario = scenario;
        if (HookGSpec.loggerEnabled) {
            logger.info("Feature/Scenario: {}/{} ", feature.getName(), scenario.getName());
        }
        HookGSpec.loggerEnabled = true;
        ThreadProperty.set("scenario", scenario.getName());
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    public void step(Step step) {
    }

    public void endOfScenarioLifeCycle(Scenario scenario) {
        if (HookGSpec.loggerEnabled) {
            logger.info(""); //empty line to split scenarios
        }
    }

    public void before(Match match, Result result) {
    }

    public void result(Result result) {
    }

    public void after(Match match, Result result) {
    }

    public void match(Match match) {
    }

    public void embedding(String mimeType, byte[] data) {
    }

    public void write(String text) {
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {

    }
}