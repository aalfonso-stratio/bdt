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

package com.stratio.qa.cucumber.testng;

import cucumber.api.event.TestSourceRead;
import gherkin.*;
import gherkin.ast.*;
import gherkin.pickles.PickleStep;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestSourcesModel {
    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap();

    private final Map<String, GherkinDocument> pathToAstMap = new HashMap();

    private final Map<String, Map<Integer, TestSourcesModel.AstNode>> pathToNodeMap = new HashMap();

    private final Map<String, Map<Integer, PickleStep>> replacedStepsMap = new HashMap();

    public TestSourcesModel() {
    }

    public static boolean isBackgroundStep(TestSourcesModel.AstNode astNode) {
        return astNode.parent.node instanceof Background;
    }

    public void addTestSourceReadEvent(String path, TestSourceRead event) {
        this.pathToReadEventMap.put(path, event);
    }

    Feature getFeature(String path) {
        if (!this.pathToAstMap.containsKey(path)) {
            this.parseGherkinSource(path);
        }

        return this.pathToAstMap.containsKey(path) ? ((GherkinDocument) this.pathToAstMap.get(path)).getFeature() : null;
    }

    public TestSourcesModel.AstNode getAstNode(String path, int line) {
        if (!this.pathToNodeMap.containsKey(path)) {
            this.parseGherkinSource(path);
        }

        return this.pathToNodeMap.containsKey(path) ? (TestSourcesModel.AstNode) ((Map) this.pathToNodeMap.get(path)).get(line) : null;
    }

    public String getKeywordFromSource(String uri, int stepLine) {
        Feature feature = this.getFeature(uri);
        if (feature != null) {
            TestSourceRead event = this.getTestSourceReadEvent(uri);
            String trimmedSourceLine = event.source.split("\n")[stepLine - 1].trim();
            GherkinDialect dialect = (new GherkinDialectProvider(feature.getLanguage())).getDefaultDialect();
            Iterator var7 = dialect.getStepKeywords().iterator();

            while (var7.hasNext()) {
                String keyword = (String) var7.next();
                if (trimmedSourceLine.startsWith(keyword)) {
                    return keyword;
                }
            }
        }

        return "";
    }

    private TestSourceRead getTestSourceReadEvent(String uri) {
        return this.pathToReadEventMap.containsKey(uri) ? (TestSourceRead) this.pathToReadEventMap.get(uri) : null;
    }

    public String getFeatureName(String uri) {
        Feature feature = this.getFeature(uri);
        return feature != null ? feature.getName() : "";
    }

    private void parseGherkinSource(String path) {
        if (this.pathToReadEventMap.containsKey(path)) {
            Parser<GherkinDocument> parser = new Parser(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();

            try {
                GherkinDocument gherkinDocument = (GherkinDocument) parser.parse(((TestSourceRead) this.pathToReadEventMap.get(path)).source, matcher);
                this.pathToAstMap.put(path, gherkinDocument);
                Map<Integer, TestSourcesModel.AstNode> nodeMap = new HashMap();
                TestSourcesModel.AstNode currentParent = new TestSourcesModel.AstNode(gherkinDocument.getFeature(), (TestSourcesModel.AstNode) null);
                Iterator var7 = gherkinDocument.getFeature().getChildren().iterator();

                while (var7.hasNext()) {
                    ScenarioDefinition child = (ScenarioDefinition) var7.next();
                    this.processScenarioDefinition(nodeMap, child, currentParent);
                }

                this.pathToNodeMap.put(path, nodeMap);
            } catch (ParserException var9) {
                ;
            }

        }
    }

    private void processScenarioDefinition(Map<Integer, TestSourcesModel.AstNode> nodeMap, ScenarioDefinition child, TestSourcesModel.AstNode currentParent) {
        TestSourcesModel.AstNode childNode = new TestSourcesModel.AstNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        Iterator var5 = child.getSteps().iterator();

        while (var5.hasNext()) {
            Step step = (Step) var5.next();
            nodeMap.put(step.getLocation().getLine(), new TestSourcesModel.AstNode(step, childNode));
        }

        if (child instanceof ScenarioOutline) {
            this.processScenarioOutlineExamples(nodeMap, (ScenarioOutline) child, childNode);
        }

    }

    private void processScenarioOutlineExamples(Map<Integer, TestSourcesModel.AstNode> nodeMap, ScenarioOutline scenarioOutline, TestSourcesModel.AstNode childNode) {
        Iterator var4 = scenarioOutline.getExamples().iterator();

        while (var4.hasNext()) {
            Examples examples = (Examples) var4.next();
            TestSourcesModel.AstNode examplesNode = new TestSourcesModel.AstNode(examples, childNode);
            TableRow headerRow = examples.getTableHeader();
            TestSourcesModel.AstNode headerNode = new TestSourcesModel.AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);

            for (int i = 0; i < examples.getTableBody().size(); ++i) {
                TableRow examplesRow = (TableRow) examples.getTableBody().get(i);
                Node rowNode = new TestSourcesModel.ExamplesRowWrapperNode(examplesRow, i);
                TestSourcesModel.AstNode expandedScenarioNode = new TestSourcesModel.AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    public void addReplacedStep(String feature, int stepLine, PickleStep replacedStep) {
        Map<Integer, PickleStep> featureMap = replacedStepsMap.get(feature) != null ? replacedStepsMap.get(feature) : new HashMap<>();
        featureMap.put(stepLine, replacedStep);
        replacedStepsMap.put(feature, featureMap);
    }

    public PickleStep getReplacedStep(String feature, int stepLine) {
        return replacedStepsMap.get(feature) != null ? replacedStepsMap.get(feature).get(stepLine) : null;
    }

    public class AstNode {
        final Node node;

        final TestSourcesModel.AstNode parent;

        AstNode(Node node, TestSourcesModel.AstNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }

    class ExamplesRowWrapperNode extends Node {
        final int bodyRowIndex;

        ExamplesRowWrapperNode(Node examplesRow, int bodyRowIndex) {
            super(examplesRow.getLocation());
            this.bodyRowIndex = bodyRowIndex;
        }
    }
}
