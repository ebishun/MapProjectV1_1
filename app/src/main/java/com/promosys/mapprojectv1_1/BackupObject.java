package com.promosys.mapprojectv1_1;

/**
 * Created by Fimrware 2 on 4/14/2017.
 */

public class BackupObject {

    String backupName,backupDirectory;

    public BackupObject(){}

    public BackupObject(String backupName,String backupDirectory){
        this.backupName = backupName;
        this.backupDirectory = backupDirectory;
    }

    public String getBackupName() {
        return backupName;
    }

    public void setBackupName(String backupName) {
        this.backupName = backupName;
    }

    public String getBackupDirectory() {
        return backupDirectory;
    }

    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }
}
