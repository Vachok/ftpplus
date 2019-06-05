package ru.vachok.networker.net.libswork;


import org.apache.commons.net.ftp.FTPClient;


/**
 @since 05.06.2019 (15:55) */
public class CoverReportUpdate extends RegRuFTPLibsUploader {
    
    
    private FTPClient ftpClient;
    
    
    @Override public void run() {
        this.ftpClient = getFtpClient();
    }
    
    
}
