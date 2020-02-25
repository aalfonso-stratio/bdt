package com.stratio.qa.utils;

import com.stratio.qa.specs.CommonG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ETCHOSTSManagementUtils {

    private final Logger logger = LoggerFactory.getLogger(ETCHOSTSManagementUtils.class);

    private CommonG comm = new CommonG();

    private final String file = "/etc/hosts";

    private final String backupFile = "/etc/hosts.bdt";

    private final String lockFile;

    private final int loops;

    private final int wait_time;

    private boolean lockAcquired;


    public ETCHOSTSManagementUtils() {
        lockFile = file + ".lock." + obtainPID();
        loops = System.getProperty("LOCK_LOOPS") != null ? Integer.parseInt(System.getProperty("LOCK_LOOPS")) : 3;
        wait_time = System.getProperty("LOCK_WAIT_TIME_MS") != null ? Integer.parseInt(System.getProperty("LOCK_WAIT_TIME_MS")) : 5000; // default: 5 seconds
        lockAcquired = false;
    }

    public String getFile() {
        return file;
    }

    public String getBackupFile() {
        return backupFile;
    }

    public String getLockFile() {
        return lockFile;
    }

    public boolean getLockAcquired() {
        return lockAcquired;
    }

    public void setLockAcquired(boolean acquired) {
        lockAcquired = acquired;
    }

    private String obtainPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = vmName.substring(0, vmName.indexOf("@"));

        return pid;
    }

    public void acquireLock(String remote, String sshConnectionId) throws Exception {
        String createLockCommand = "if [ ! -f " + lockFile + " ]; then sudo touch " + lockFile + "; fi";
        String checkLockCommand = "if [ ! -f " + backupFile + " ] || ([ -f " + backupFile + " && -f " + lockFile + " ]); then " + createLockCommand + " && echo 0; else echo 1; fi";
        int iterator = 1;

        while (!lockAcquired && iterator <= loops) {
            // Remote system
            if (remote != null) {
                comm.executeCommand(checkLockCommand, sshConnectionId, 0, null);
                // Lock acquired
                if (comm.getCommandResult().matches("0")) {
                    lockAcquired = true;
                    logger.info("Lock ACQUIRED in file: " + file + " with lock file: " + lockFile);
                    // Lock not acquired, we have to wait
                } else {
                    logger.debug("Not possible to acquire lock over file: " + file + ". Waiting... " + iterator + "/" + loops);
                    iterator = iterator + 1;
                    Thread.sleep(wait_time);
                }
                // Local system
            } else {
                comm.runLocalCommand(checkLockCommand);
                // Lock acquired
                if (comm.getCommandResult().matches("0")) {
                    lockAcquired = true;
                    logger.info("Lock ACQUIRED in file: " + file + " with lock file: " + lockFile);
                    // Lock not acquired, we have to wait
                } else {
                    logger.debug("Not possible to acquire lock over file: " + file + ". Waiting... " + iterator + "/" + loops);
                    iterator = iterator + 1;
                    Thread.sleep(wait_time);
                }
            }
        }

        // If we have reached this point without having acquired the lock, we have a problem
        assertThat(lockAcquired).as("It has not been possible to acquire lock over file: " + file).isTrue();
    }

    public void releaseLock(String remote, String sshConnectionId) throws Exception {
        String removeLockCommand = "sudo rm " + lockFile;
        String checkLockCommand = "if [ -f " + lockFile + " ]; then " + removeLockCommand + " && echo 0; else echo 1; fi";

        // Remote system
        if (remote != null) {
            comm.executeCommand(removeLockCommand, sshConnectionId, 0, null);
            // Lock not acquired
            if (comm.getCommandResult().matches("1")) {
                throw new Exception("File was not locked by this process: " + file);
            }
            // Local system
        } else {
            comm.runLocalCommand(checkLockCommand);
            // Lock not acquired
            if (comm.getCommandResult().matches("1")) {
                throw new Exception("File was not locked by this process: " + file);
            }
        }

        logger.info("Lock RELEASED in file: " + file + " with lock file: " + lockFile);
    }

    public void forceReleaseLock(String sshConnectionId) {
        try {
            if (sshConnectionId != null) {
                logger.debug("Checking left behind changes in /etc/hosts in connection: " + sshConnectionId);
                releaseLock("remote", sshConnectionId);
            } else {
                logger.debug("Checking left behind changes in /etc/hosts locally");
                releaseLock(null, sshConnectionId);
            }
        } catch (Exception e) {
            logger.debug("Nothing to be cleaned for this execution.");
        }
    }
}
