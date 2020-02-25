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


/**
 * Generic /etc/hosts management Specs.
 *
 * @see <a href="ETCHOSTSManagementSpec-annotations.html">Vault Steps &amp; Matching Regex</a>
 */
public class ETCHOSTSManagementSpec extends BaseGSpec {

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public ETCHOSTSManagementSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * If there is no previous backup of /etc/hosts, it will create it and add the new entry,
     * otherwise it will only add the new entry.
     *
     * @param hostname  hostname to be added to /etc/hosts
     * @param ip    ip address to be added to /etc/hosts
     * @param remote    whether we want to change /etc/hosts locally or in a remote system
     * @param sshConnectionId   previously saved ssh connection where to perform the action
     * @throws Exception
     */
    @Given("^I save host '(.+?)' with ip '(.+?)' in /etc/hosts( in the ssh connection)?( with id '(.+?)')?$")
    public void addEntryHostsFile(String hostname, String ip, String remote, String sshConnectionId) throws Exception {
        String backupCommand = "sudo cp " + commonspec.getETCHOSTSManagementUtils().getFile() + " " + commonspec.getETCHOSTSManagementUtils().getBackupFile();
        String checkBackupCommand = "if [ ! -f " + commonspec.getETCHOSTSManagementUtils().getBackupFile() + " ]; then " + backupCommand  + "; fi";
        String addCommand = "echo \"" + ip + "   " + hostname + "\" | sudo tee -a " + commonspec.getETCHOSTSManagementUtils().getFile();

        // Try to acquire lock
        commonspec.getETCHOSTSManagementUtils().acquireLock(remote, sshConnectionId);

        // We want to save in remote machines's /etc/hosts
        if (remote != null) {
            commonspec.executeCommand(checkBackupCommand, sshConnectionId, 0, null);
            commonspec.executeCommand(addCommand, sshConnectionId, 0, null);
            // We want to save in local system /etc/hosts
        } else {
            commonspec.runLocalCommand(checkBackupCommand);
            commonspec.runLocalCommand(addCommand);
        }
    }

    /**
     * If /etc/hosts backup file, restore it, otherwise do nothing
     *
     * @param remote    whether we want to restore /etc/hosts locally or in a remote system
     * @param sshConnectionId   previously saved ssh connection where to perform the action
     * @throws Exception
     */
    @Given("^I restore /etc/hosts( in the ssh connection)?( with id '(.+?)')?$")
    public void restoreHostsFile(String remote, String sshConnectionId) throws Exception {
        String restoreCommand = "sudo mv " + commonspec.getETCHOSTSManagementUtils().getBackupFile() + " " + commonspec.getETCHOSTSManagementUtils().getFile();
        String checkRestoreCommand = "if [ -f " + commonspec.getETCHOSTSManagementUtils().getBackupFile() + " ] && [ -f " + commonspec.getETCHOSTSManagementUtils().getLockFile() + " ] ; then " + restoreCommand  + "; fi";

        // We want to restore remote machines's /etc/hosts
        if (remote != null) {
            commonspec.executeCommand(checkRestoreCommand, sshConnectionId, 0, null);
            // We want to restore local system /etc/hosts
        } else {
            commonspec.runLocalCommand(checkRestoreCommand);
        }

        // Release lock
        commonspec.getETCHOSTSManagementUtils().releaseLock(remote, sshConnectionId);
    }

}
