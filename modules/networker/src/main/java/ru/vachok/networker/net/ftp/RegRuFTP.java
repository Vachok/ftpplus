// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import ru.vachok.networker.ConstantsFor;
import sun.net.ftp.FtpClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Queue;
import java.util.Scanner;


/**
 Class ru.vachok.networker.net.ftp.RegRuFTP
 <p>
 
 @since 01.06.2019 (4:19) */
public class RegRuFTP implements FTPHelper {
    
    
    private static final String FTP_SERVER = "31.31.196.85";
    
    private static final String FTP_PASS = "36e42yoak8";
    
    @Override public void connectTo() {
        FTPClient ftpClient = new FTPClient();
        FtpClient.TransferType binaryTrans = FtpClient.TransferType.BINARY;
        chkPass();
        try {
            ftpClient.connect(getHost(), 21);
            ftpClient.configure(getConf(ftpClient));
            ftpClient.login("u0466446_java", FTP_PASS);
            ftpClient.setDefaultTimeout((int) ConstantsFor.DELAY);
            for (FTPFile listDirectory : ftpClient.listDirectories()) {
                System.out.println(listDirectory);
            }
            for (FTPFile ftpFile : ftpClient.listFiles()) {
                System.out.println("ftpFile = " + ftpFile);
            }
            ;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Override public Queue<String> getContentsQueue() {
        return null;
    }
    
    private void chkPass() {
        try (InputStream inputStream = new FileInputStream("pass");
             Scanner scanner = new Scanner(inputStream)
        ) {
            String passFile = scanner.nextLine();
            if (!getDigiest(passFile).equals(getDigiest(null))) {
                System.out.println(passFile);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private String getDigiest(String chkStr) {
        if (chkStr == null) {
            chkStr = FTP_PASS;
        }
        byte[] dBytes = chkStr.getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            dBytes = messageDigest.digest(dBytes);
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }
        return new String(dBytes);
    }
    
    private FTPClientConfig getConf(FTPClient ftpClient) {
        FTPClientConfig ftpClientConfig = new FTPClientConfig();
        ftpClientConfig.setUnparseableEntries(true);
        try {
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return ftpClientConfig;
    }
    
    private InetAddress getHost() {
        InetAddress ftpAddr = InetAddress.getLoopbackAddress();
        try {
            byte[] addressBytes = InetAddress.getByName(FTP_SERVER).getAddress();
            
            ftpAddr = InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        return ftpAddr;
    }
}