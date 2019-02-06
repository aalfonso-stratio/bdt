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

import com.stratio.qa.utils.ThreadProperty;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Aspect
public class RunOnTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("execution (* cucumber.runtime.Runtime.matchesFilters(..)) && " +
              "args(pickleEvent)")
    protected void AddRunOnTagPointcutScenario(PickleEvent pickleEvent) {
    }

    /**
     * Allows conditional scenario execution.
     * If the scenario contains the following tag:
     * <dl>
     *    <dt>\@runOnEnv(param)</dt>
     *<dd>The scenario will only be executed if the param is defined when test is launched. Configuration map object.
     * More than one param can be passed in the tag. To do so, the params must be comma separated:
     * \@runOnEnv(param): The scenario will only be executed if the param is defined when test is launched.
     * \@runOnEnv(param1,param2,param3): The scenario will only be executed if ALL the params are defined.
     * </dd>
     * </dl>
     * Additionally, if the scenario contains the following tag:
     * <dl>
     *    <dt>\@skipOnEnv(param)</dt>
     *<dd>The scenario will be omitted if the param is defined when test is launched.
     * More than one param can be passed in the tag. To do so, the params must be comma separated.
     * The scenario will omitted if ANY of params are defined. (OR)</dd>
     *
     *<dd>Or in separated lines to force ALL of the params to be defined in order to omit the execution</dd>
     *    <dt>  \@skipOnEnv(param1)
     *          \@skipOnEnv(param2)
     *          \@skipOnEnv(param3)</dt>
     *<dd>The scenario will omitted if ALL of params are defined. (AND)</dd>
     *</dl>
     *
     * @param pickleEvent pickleEvent
     * @throws Throwable exception
     */
    @Around(value = "AddRunOnTagPointcutScenario(pickleEvent)")
    public boolean aroundAddRunOnTagPointcut(ProceedingJoinPoint pjp, PickleEvent pickleEvent) throws Throwable {
        int line = 0;
        if (!pickleEvent.pickle.getLocations().isEmpty()) {
            line = pickleEvent.pickle.getLocations().get(0).getLine();
        }
        Boolean exit = tagsIteration(pickleEvent.pickle.getTags(), line);
        if (exit) {
            ThreadProperty.set("skippedOnParams" + pickleEvent.pickle.getName() + line, "true");
        }
        return (Boolean) pjp.proceed();
    }

    public boolean tagsIteration(List<PickleTag> tags, Integer line) throws Exception {
        PickleLocation pickleLocation = new PickleLocation(line, 0);
        for (PickleTag tag : tags) {
            if (tag.getName().contains("@runOnEnv")) {
                if (!checkParams(getParams(tag.getName()))) {
                    tags.add(new PickleTag(pickleLocation, "@ignore"));
                    tags.add(new PickleTag(pickleLocation, "@envCondition"));
                    return true;
                }
            } else if (tag.getName().contains("@skipOnEnv")) {
                if (checkParams(getParams(tag.getName()))) {
                    tags.add(new PickleTag(pickleLocation, "@ignore"));
                    tags.add(new PickleTag(pickleLocation, "@envCondition"));
                    return true;
                }
            }
        }
        return false;
    }

    /*
    * Returns a string array of params
    */
    public String[][] getParams(String s) throws Exception {
        String delimeters = ",|&&|\\|\\|";
        String[] val = s.substring((s.lastIndexOf("(") + 1), (s.length()) - 1).split(delimeters);
        if (val[0].startsWith("@")) {
            throw new Exception ("Error while parsing params. Format is: \"runOnEnv(PARAM)\", but found: " + s);
        }

        String[] ops;
        // Only valid operators: AND -> &&; OR -> ||
        // Valid variables names and values can only contain: characters, numbers, underscores, hyphens, dots and equal
        if (s.contains("&&") || s.contains("||")) {
            ops = s.substring((s.lastIndexOf("(") + 1), (s.length()) - 1).split("[a-zA-Z._\\-0-9=<>]+");
            if (ops.length > 0) {
                ops = Arrays.copyOfRange(ops, 1, ops.length);
            }
            if (ops.length != val.length - 1) {
                throw new Exception("Error in expression. Number of conditional operators plus 1 should be equal to the number of expressions.");
            }
            for (String op: ops) {
                if (!("||".equals(op) || "&&".equals(op))) {
                    throw new Exception("Error in conditional operators. Operators should be && or ||.");
                }
            }
        } else {
            ops = new String[] {};
        }

        String[][] result = new String[][] {val, ops};
        return result;
    }

   /*
    * Checks if every param in the array of strings is defined
    */
    public boolean checkParams(String[][] params) throws Exception {
        if ("".equals(params[0][0])) {
            throw new Exception("Error while parsing params. Params must be at least one");
        }

        boolean result = true;
        // Si no hay operadores, hacemos validación normal (por defecto AND)
        if (params[1].length == 0) {
            for (int i = 0; i < params[0].length; i++) {
                if (params[0][i].contains("=")) {
                    String param = params[0][i].split("=")[0];
                    String value = params[0][i].split("=")[1];

                    if (System.getProperty(param, "").isEmpty()) {
                        return false;
                    }

                    if (!System.getProperty(param).equals(value)) {
                        return false;
                    }
                } else if (params[0][i].contains(">")) {
                    String param = params[0][i].split(">")[0];
                    String value = params[0][i].split(">")[1];

                    if (System.getProperty(param, "").isEmpty()) {
                        return false;
                    }

                    if (!(System.getProperty(param).compareTo(value) > 0)) {
                        return false;
                    }
                } else if (params[0][i].contains("<")) {
                    String param = params[0][i].split("<")[0];
                    String value = params[0][i].split("<")[1];

                    if (System.getProperty(param, "").isEmpty()) {
                        return false;
                    }

                    if (!(System.getProperty(param).compareTo(value) < 0)) {
                        return false;
                    }
                } else {
                    if (System.getProperty(params[0][i], "").isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            // Tenemos expresión condicional
            // Primer elemento
            if (params[0][0].contains("=")) {
                String param = params[0][0].split("=")[0];
                String value = params[0][0].split("=")[1];
                if (System.getProperty(param, "").isEmpty()) {
                    result = false;
                }

                if (!value.equals(System.getProperty(param))) {
                    result = false;
                }
            } else if (params[0][0].contains(">")) {
                String param = params[0][0].split(">")[0];
                String value = params[0][0].split(">")[1];
                if (System.getProperty(param, "").isEmpty() || !(value.compareTo(System.getProperty(param)) < 0)) {
                    result =  false;
                }
            } else if (params[0][0].contains("<")) {
                String param = params[0][0].split("<")[0];
                String value = params[0][0].split("<")[1];
                if (System.getProperty(param, "").isEmpty() || !(value.compareTo(System.getProperty(param)) > 0)) {
                    result = false;
                }
            } else {
                if (System.getProperty(params[0][0], "").isEmpty()) {
                    result = false;
                }
            }

            // Elementos intermedios
            for (int j = 1; j < params[0].length - 1; j++) {
                if (params[0][j].contains("=")) {
                    String param = params[0][j].split("=")[0];
                    String value = params[0][j].split("=")[1];
                    if (System.getProperty(param, "").isEmpty()) {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else if (!System.getProperty(param).equals(value)) {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }
                } else if (params[0][j].contains(">")) {
                    String param = params[0][j].split(">")[0];
                    String value = params[0][j].split(">")[1];
                    if (System.getProperty(param, "").isEmpty()) {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else if (!(System.getProperty(param).compareTo(value) > 0)) {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }
                } else if (params[0][j].contains("<")) {
                    String param = params[0][j].split("<")[0];
                    String value = params[0][j].split("<")[1];
                    if (System.getProperty(param, "").isEmpty()) {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else if (!(System.getProperty(param).compareTo(value) < 0)) {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }
                } else {
                    if (System.getProperty(params[0][j], "").isEmpty()) {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][j - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }

                }
            }

            // Último elemento
            if (params[0].length > 1) {
                if (params[0][params[0].length - 1].contains("=")) {
                    String param = params[0][params[0].length - 1].split("=")[0];
                    String value = params[0][params[0].length - 1].split("=")[1];
                    if (System.getProperty(param, "").isEmpty()) {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else if (!value.equals(System.getProperty(param))) {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }
                } else if (params[0][params[0].length - 1].contains(">")) {
                    String param = params[0][params[0].length - 1].split(">")[0];
                    String value = params[0][params[0].length - 1].split(">")[1];
                    if (System.getProperty(param, "").isEmpty()) {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else if (!(value.compareTo(System.getProperty(param)) < 0)) {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }
                } else if (params[0][params[0].length - 1].contains("<")) {
                    String param = params[0][params[0].length - 1].split("<")[0];
                    String value = params[0][params[0].length - 1].split("<")[1];
                    if (System.getProperty(param, "").isEmpty()) {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else if (!(value.compareTo(System.getProperty(param)) > 0)) {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }
                } else {
                    if (System.getProperty(params[0][params[0].length - 1], "").isEmpty()) {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && false;
                        } else {
                            result = result || false;
                        }
                    } else {
                        if ("&&".equals(params[1][params[1].length - 1])) {
                            result = result && true;
                        } else {
                            result = result || true;
                        }
                    }
                }
            }
            return result;
        }
    }
}
