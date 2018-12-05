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

package com.stratio.qa.aspects;

import cucumber.runner.Runner;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleTag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
public class IgnoreTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("execution (* cucumber.runner.Runner.runPickle(..)) && "
            + "args (pickle)")
    protected void addIgnoreTagPointcutScenario(PickleEvent pickle) {
    }

    /**
     * @param pjp ProceedingJoinPoint
     * @param pickle pickle
     * @throws Throwable exception
     */
    @Around(value = "addIgnoreTagPointcutScenario(pickle)")
    public void aroundAddIgnoreTagPointcut(ProceedingJoinPoint pjp, PickleEvent pickle) throws Throwable {
        Runner runner = (Runner) pjp.getThis();

        Class<?> sc = runner.getClass();
        Method tt = sc.getDeclaredMethod("buildBackendWorlds");
        tt.setAccessible(true);
        tt.invoke(runner);

        String scenarioName = pickle.pickle.getName();
        List<PickleTag> pickleTagList = pickle.pickle.getTags();
        List<String> tagList = new ArrayList<>();
        for (PickleTag pt:pickleTagList) {
            tagList.add(pt.getName());
        }

        ignoreReasons exitReason = manageTags(tagList, scenarioName);
        if (exitReason.equals(ignoreReasons.NOREASON)) {
            logger.error("Scenario '" + scenarioName + "' failed due to wrong use of the @ignore tag.");
        }

        if ((!(exitReason.equals(ignoreReasons.NOTIGNORED))) && (!(exitReason.equals(ignoreReasons.NOREASON)))) {
            tt = sc.getDeclaredMethod("disposeBackendWorlds");
            tt.setAccessible(true);
            tt.invoke(runner);
        } else {
            pjp.proceed();
        }
    }

    public ignoreReasons manageTags(List<String> tagList, String scenarioName) {
        ignoreReasons exit = ignoreReasons.NOTIGNORED;
        if (tagList.contains("@ignore")) {
            exit = ignoreReasons.NOREASON;
            for (String tag: tagList) {
                Pattern pattern = Pattern.compile("@tillfixed\\((.*?)\\)");
                Matcher matcher = pattern.matcher(tag);
                if (matcher.find()) {
                    String ticket = matcher.group(1);
                    logger.warn("Scenario '" + scenarioName + "' ignored because of ticket: " + ticket + "\n");
                    exit = ignoreReasons.JIRATICKET;
                }
            }
            if (tagList.contains("@envCondition")) {
                exit = ignoreReasons.ENVCONDITION;
            }
            if (tagList.contains("@unimplemented")) {
                logger.warn("Scenario '" + scenarioName + "' ignored because it is not yet implemented.\n");
                exit = ignoreReasons.UNIMPLEMENTED;
            }
            if (tagList.contains("@manual")) {
                logger.warn("Scenario '" + scenarioName + "' ignored because it is marked as manual test.\n");
                exit = ignoreReasons.MANUAL;
            }
            if (tagList.contains("@toocomplex")) {
                logger.warn("Scenario '" + scenarioName + "' ignored because the test is too complex.\n");
                exit = ignoreReasons.TOOCOMPLEX;
            }
        }
        return exit;
    }

    public enum ignoreReasons { NOTIGNORED, ENVCONDITION, UNIMPLEMENTED, MANUAL, TOOCOMPLEX, JIRATICKET, NOREASON }
}