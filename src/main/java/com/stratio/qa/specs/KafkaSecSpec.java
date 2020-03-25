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

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import org.apache.zookeeper.KeeperException;
import org.assertj.core.api.Assertions;

import java.net.UnknownHostException;

/**
 * Generic Kafka Sec Specs.
 *
 * @see <a href="KafkaSecSpec-annotations.html">Kafka Sec Steps &amp; Matching Regex</a>
 */
public class KafkaSecSpec extends BaseGSpec {

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public KafkaSecSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * Open connection to Kafka cluster
     *
     * @param brokersUrl    brokers URL to connect to cluster
     */
    @Given("I open connection to kafka with url {string}")
    public void openKafkaConnection(String brokersUrl) {
        commonspec.getKafkaSecUtils().createConnection(brokersUrl);
    }

    /**
     * Open secured connection to Kafka cluster
     *
     * @param brokersUrl    brokers URL to connect to cluster
     * @param keystore      keystore for secured connection
     * @param keypass       key password for secured connection
     * @param truststore    truststore for secured connection
     * @param trustpass     password for secured connection
     */
    @Given("^I open connection to kafka with url '(.+?)' with keyStorePath '(.+?)' and keyStorePassword '(.+?)' and trustStorePath '(.+?)' and trustStorePassword '(.+?)'$")
    public void openSecuredKafkaConnection(String brokersUrl, String keystore, String keypass, String truststore, String trustpass) {
        commonspec.getKafkaSecUtils().createConnection(brokersUrl, keystore, keypass, truststore, trustpass);
    }

    /**
     * Close previously opened connection to Kafka cluster
     */
    @Given("^I close Kafka connection$")
    public void closeKafkaConnection() {
        commonspec.getKafkaSecUtils().closeConnection();
    }

    @Given("^I wait '(\\d+)' seconds, checking every '(\\d+)' seconds for kafka server availability$")
    public void checkKafkaReady(Integer timeout, Integer wait) throws Exception {
        commonspec.getKafkaSecUtils().checkKafkaReady(timeout, wait);
    }

    /**
     * Delete topic in Kafka cluster
     *
     * @param topic topic to delete
     * @throws Exception
     */
    @When("^I delete topic '(.+?)'$")
    public void deleteTopic(String topic) throws Exception {
        commonspec.getKafkaSecUtils().deleteTopic(topic);
    }

    /**
     * Create topic with/without partitions
     *
     * @param topic         topic to be created
     * @param partitions    number of partitions to be created (optional)
     * @throws Exception
     */
    @When("^I create topic '(.+?)'( with '(.+?)' partitions)?$")
    public void createTopic(String topic, String partitions) throws Exception {
        commonspec.getKafkaSecUtils().createTopic(topic, partitions);
    }

    /**
     * Send a message to a topic
     *
     * @param message       message to be sent
     * @param topic         topic to send message to
     * @param partition     partition where to store message (optional)
     * @throws Exception
     */
    @When("^I send message '(.+?)' to topic '(.+?)'( in partition '(.+?)')?$")
    public void produceMessage(String message, String topic, String partition) throws Exception {
        commonspec.getKafkaSecUtils().sendMessage(topic, partition, message);
    }

    /**
     * Send transactional message to topic
     *
     * @param topic topic to send transactional messages to
     * @throws Exception
     */
    @When("^I send transactional messages to topic '(.+?)'$")
    public void produceTransactionalMessage(String topic) throws Exception {
        commonspec.getKafkaSecUtils().sendTransactionalMessages(topic);
    }

    /**
     * Check topic exists in Kafka cluster
     *
     * @param topic topic to be checked
     * @throws Exception
     */
    @Then("^topic '(.+?)' exists?$")
    public void checkTopicExists(String topic) throws Exception {
        commonspec.getKafkaSecUtils().checkTopicExists(topic);
    }

    /**
     * Check topic does not exist in Kafka cluster
     *
     * @param topic topic to be checked
     * @throws Exception
     */
    @Then("^topic '(.+?)' does not exist?$")
    public void checkTopicDoesNotExist(String topic) throws Exception {
        commonspec.getKafkaSecUtils().checkTopicDoesNotExist(topic);
    }

    /**
     * Check whether a topic contains message in a particular partition or not
     *
     * @param topic         topic where to look for message
     * @param message       message to look for
     * @param partition     partition where to look for message (optional)
     * @throws Exception
     */
    @Then("^topic '(.+?)' contains message '(.+?)'( in partition '(.+?)')?$")
    public void containsMessage(String topic, String message, String partition) throws Exception {
        commonspec.getKafkaSecUtils().containsMessage(topic, partition, message);
    }

    /**
     * Check whether a topic contains the transactional message generated or not
     *
     * @param topic         topic where to look for message
     * @throws Exception
     */
    @Then("^topic '(.+?)' contains transactional messages?$")
    public void containsTransactionalMessage(String topic) throws Exception {
        commonspec.getKafkaSecUtils().containsTransactionalMessages(topic);
    }

    /**
     * Check topic contains a specific number of messages
     *
     * @param topic         topic where to look for messages
     * @param numMessages   number of expected messages
     * @param partition     partition where to look for messages (optional)
     * @throws Exception
     */
    @Then("^topic '(.+?)' contains '(.+?)' messages( in partition '(.+?)')?$")
    public void containsNMessagesInTopic(String topic, String numMessages, String partition) throws Exception {
        commonspec.getKafkaSecUtils().containsNMessagesInTopic(topic, numMessages, partition, null);
    }

    /**
     * Check topic contains a specific number of messages
     *
     * @param topic         topic where to look for messages
     * @param numMessages   number of expected messages
     * @param partition     partition where to look for messages (optional)
     * @param values        values to be checked
     * @throws Exception
     */
    @Then("^topic '(.+?)' contains '(.+?)' messages( in partition '(.+?)')? with values:$")
    public void containsNMessagesInTopic(String topic, String numMessages, String partition, DataTable values) throws Exception {
        commonspec.getKafkaSecUtils().containsNMessagesInTopic(topic, numMessages, partition, values);
    }

    /**
     * Check that I do not have authorization to create topic in kafka cluster
     *
     * @param topic         topic to be created
     * @param brokersUrl    Kafka cluster URL
     * @param keystore      keystore for secured connection
     * @param keypass       key password for secured connection
     * @param truststore    truststore for secured connection
     * @param trustpass     password for secured connection
     * @throws Exception
     */
    @Then("^I cannot create topic '(.+?)' with url '(.+?)' with keyStorePath '(.+?)' and keyStorePassword '(.+?)' and trustStorePath '(.+?)' and trustStorePassword '(.+?)'$")
    public void cannotCreateTopic(String topic, String brokersUrl, String keystore, String keypass, String truststore, String trustpass) throws Exception {
        commonspec.getKafkaSecUtils().cannotCreateTopic(topic, null, brokersUrl, keystore, keypass, truststore, trustpass);
    }

    /**
     * Check that I do not have authorization to send messages to topic
     * @param message       message to be sent
     * @param topic         topic where to send message
     * @param brokersUrl    Kafka cluster URL
     * @param keystore      keystore for secured connection
     * @param keypass       key password for secured connection
     * @param truststore    truststore for secured connection
     * @param trustpass     password for secured connection
     * @throws Exception
     */
    @Then("^I cannot send message '(.+?)' to topic '(.+?)' with url '(.+?)' with keyStorePath '(.+?)' and keyStorePassword '(.+?)' and trustStorePath '(.+?)' and trustStorePassword '(.+?)'$")
    public void cannotSendMessage(String message, String topic, String brokersUrl, String keystore, String keypass, String truststore, String trustpass) throws Exception {
        commonspec.getKafkaSecUtils().cannotSendMessage(topic, null, message, brokersUrl, keystore, keypass, truststore, trustpass);
    }

    /**
     * Check that I do not have authorization to send transactional messages to topic
     * @param topic         topic where to send message
     * @param brokersUrl    Kafka cluster URL
     * @param keystore      keystore for secured connection
     * @param keypass       key password for secured connection
     * @param truststore    truststore for secured connection
     * @param trustpass     password for secured connection
     * @throws Exception
     */
    @Then("^I cannot send transactional messages to topic '(.+?)' with url '(.+?)' with keyStorePath '(.+?)' and keyStorePassword '(.+?)' and trustStorePath '(.+?)' and trustStorePassword '(.+?)'$")
    public void cannotSendTransactionalMessages(String message, String topic, String brokersUrl, String keystore, String keypass, String truststore, String trustpass) throws Exception {
        commonspec.getKafkaSecUtils().cannotSendTransactionalMessages(topic, brokersUrl, keystore, keypass, truststore, trustpass);
    }

    /**
     * Check that of partitions in a topic matches
     *
     * @param topic             topic to be checked
     * @param numPartitions     expected number of partitions
     * @throws Exception
     */
    @Then("^number of partitions in topic '(.+?)' is '(.+?)'$")
    public void checkNumberPartitionsIsN(String topic, String numPartitions) throws Exception {
        commonspec.getKafkaSecUtils().numbersOfPartitionsIsN(topic, numPartitions);
    }
}
