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

import com.stratio.qa.specs.CommonG;

import io.cucumber.datatable.DataTable;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ZookeeperSecUtils {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperSecUtils.class);

    private String zk_hosts;

    private int timeout;

    private ExponentialBackoffRetry retryPolicy;

    private CuratorFramework curatorZkClient;

    public ZookeeperSecUtils() {
        this.timeout = System.getProperty("ZK_SESSION_TIMEOUT_MS") != null ? Integer.parseInt(System.getProperty("ZK_SESSION_TIMEOUT_MS")) : 30000;
        this.retryPolicy = new ExponentialBackoffRetry(1000, 3);
    }

//    public void connectZk(String hosts, String jaasPath, String krb5Path) throws InterruptedException {
//        this.zk_hosts = hosts;
//        this.curatorZkClient = CuratorFrameworkFactory.builder().connectString(this.zk_hosts).retryPolicy(this.retryPolicy).connectionTimeoutMs(this.timeout).build();
//
//        if (jaasPath != null && krb5Path != null) {
//            System.setProperty("java.security.auth.login.config", jaasPath);
//            System.setProperty("java.security.krb5.conf", krb5Path);
//        }
//
//        this.curatorZkClient.start();
//        this.curatorZkClient.blockUntilConnected();
//    }

    public void connectZk(String hosts) throws InterruptedException {
        this.zk_hosts = hosts;
        this.curatorZkClient = CuratorFrameworkFactory.builder().connectString(this.zk_hosts).retryPolicy(this.retryPolicy).connectionTimeoutMs(this.timeout).build();

        this.curatorZkClient.start();
        this.curatorZkClient.blockUntilConnected();
    }

    private void createJaasConfFile(String keytabPath, String principal) throws Exception {
        Path pathIN = Paths.get("./target/test-classes/schemas/jaas.conf.template");
        Path pathOUT = Paths.get("/tmp/jaas.conf");
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(pathIN), charset);
        content = content.replaceAll("keytabPath", keytabPath);
        content = content.replaceAll("principalValue", principal);
        Files.write(pathOUT, content.getBytes(charset));
    }

    public void connectZk(String hosts, String keytabPath, String principal, String krb5Path) throws Exception {
        this.zk_hosts = hosts;
        this.curatorZkClient = CuratorFrameworkFactory.builder().connectString(this.zk_hosts).retryPolicy(this.retryPolicy).connectionTimeoutMs(this.timeout).build();

        createJaasConfFile(keytabPath, principal);
        System.setProperty("java.security.auth.login.config", "/tmp/jaas.conf");
        System.setProperty("java.security.krb5.conf", krb5Path);

        this.curatorZkClient.start();
        this.curatorZkClient.blockUntilConnected();
    }

    public String zRead(String path) throws Exception {
        logger.debug("Trying to read data at {}", path);
        byte[] b;
        String data;

        b = this.curatorZkClient.getData().forPath(path);
        if (b == null) {
            data = "";
        } else {
            data = new String(b, StandardCharsets.UTF_8);
        }

        logger.debug("Requested path {} contains {}", path, data);

        return data;
    }

    public void zCreate(String path, String document, boolean isEphemeral) throws Exception {
        byte[] bDoc;

        if (document == null) {
            bDoc = "".getBytes(StandardCharsets.UTF_8);
        } else {
            bDoc = document.getBytes(StandardCharsets.UTF_8);
        }

        if (isEphemeral) {
            this.curatorZkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, bDoc);
        } else {
            this.curatorZkClient.create().withMode(CreateMode.PERSISTENT).forPath(path, bDoc);
        }
    }

    public void write(String path, String data) throws Exception {
        this.curatorZkClient.setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
    }

    public Boolean isConnected() {
        return ((this.curatorZkClient != null) && (this.curatorZkClient.getZookeeperClient().isConnected()));
    }

    public Boolean exists(String path) throws Exception {
        return this.curatorZkClient.checkExists().forPath(path) != null;
    }

    public void delete(String path) throws Exception {
        this.curatorZkClient.delete().forPath(path);
    }

    public void disconnect() throws InterruptedException {
        this.curatorZkClient.getZookeeperClient().close();
    }
}
