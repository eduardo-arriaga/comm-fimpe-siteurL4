package com.idear.fimpe.enums;

import java.io.File;

public enum Folder {
    XML("xml" + File.separator),
    SENT_FILES("sent_files" + File.separator),
    DAT_FILES("dat_files" + File.separator),
    SENT_FILES_LP("sent_files_lp" + File.separator),
    DAT_FILES_LP("dat_files_lp" + File.separator),
    INPUT("cifrado" + File.separator + "input" + File.separator),
    OUTPUT("cifrado" + File.separator + "output" + File.separator),
    UPLOAD("upload" + File.separator),
    DOWNLOAD("download" + File.separator),
    KEYPAIR("keypair" + File.separator),

    OK_ACK("ok_acuses" + File.separator),

    ERROR_ACK("error_acuses" + File.separator),
    OK_ACK_LP("ok_acuses_lp" + File.separator),
    ERROR_ACK_LP("error_acuses_lp" + File.separator),

    LAM("lam" + File.separator),
    REPORTS("reports" + File.separator),
    REPORTS_LP("reports_lp" + File.separator),
    REMOTE_RECHARGES("remote_recharges" + File.separator);

    private String path;

    Folder(String path){
        this.path = path;
    }

    public String getPath(){
        return path;
    }
}
