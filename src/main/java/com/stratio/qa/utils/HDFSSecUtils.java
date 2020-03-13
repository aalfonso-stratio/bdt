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

import com.stratio.qa.assertions.Assertions;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class HDFSSecUtils {

    private final Logger logger = LoggerFactory.getLogger(KafkaSecUtils.class);

    private ByteArrayOutputStream outputStream;

    private ByteArrayOutputStream errorStream;

    private Configuration conf = new Configuration();

    public HDFSSecUtils() {
        conf.set("hadoop.home.dir", "/");
    }

    public void createSecuredHDFSConnection(String coreSite, String hdfsSite, String krb5Conf, String sslClient, String hdfsHost, String keytabPath, String truststorePath, String realm) throws Exception {
        // Check that ssl-config.xml file provided points to truststore path provided
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(sslClient);

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        XPathExpression expr = xpath.compile("/configuration/property[name='ssl.client.truststore.location']/value/text()");
        String result = expr.evaluate(doc);

        Assertions.assertThat(result).as("Truststore path specified in ssl-client.xml: " + result + " is different from provided one: " + truststorePath).isEqualTo(truststorePath);

        conf.addResource(new Path("file:///" + coreSite));
        conf.addResource(new Path("file:///" + hdfsSite));

        System.setProperty("java.security.krb5.conf", krb5Conf);

        conf.addResource(new Path("file:///" + sslClient));
        conf.set("fs.defaultFS", "hdfs://" + hdfsHost + "/");

        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("hdfs/" + hdfsHost + "@" + realm, keytabPath);
    }

    public void closeConnection() {
        conf.clear();
        conf = null;
    }

    public void writeToHDFS(String sourcePath, String destinationPath) throws IOException {
        Path inputPath = new Path(sourcePath);
        Path outputPath = new Path(destinationPath);

        FileSystem fileSystem = FileSystem.get(conf);
        fileSystem.copyFromLocalFile(inputPath, outputPath);
        fileSystem.close();
    }

    public void readFromHDFS(String hdfsStorePath, String localSystemPath) throws IOException {
        Path hdfsPath = new Path(hdfsStorePath);
        Path localPath = new Path(localSystemPath);

        FileSystem fileSystem = FileSystem.get(conf);
        fileSystem.copyToLocalFile(hdfsPath, localPath);
        fileSystem.close();
    }

    public void deleteHDFSDirectory(String hdfsStorePath) throws IOException {
        Path hdfsPath = new Path(hdfsStorePath);

        FileSystem fileSystem = FileSystem.get(conf);

        if (fileSystem.exists(hdfsPath)) {
            fileSystem.delete(hdfsPath, true);
            logger.info("Directory: {} deleted successfully." + hdfsPath);
        } else {
            logger.info("Input Directory: {} does not exists." + hdfsPath);
        }
    }

    public void fileExists(String hdfsStorePath) throws IOException {
        Path hdfsPath = new Path(hdfsStorePath);

        FileSystem fileSystem = FileSystem.get(conf);

        Assertions.assertThat(fileSystem.exists(hdfsPath)).as("File: " + hdfsStorePath + " does not exist.").isTrue();
    }

    public void fileDoesNotExist(String hdfsStorePath) throws IOException {
        Path hdfsPath = new Path(hdfsStorePath);

        FileSystem fileSystem = FileSystem.get(conf);

        Assertions.assertThat(fileSystem.exists(hdfsPath)).as("File: " + hdfsStorePath + " exists.").isFalse();
    }
}
