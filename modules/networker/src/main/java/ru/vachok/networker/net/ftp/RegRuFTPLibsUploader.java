// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import sun.net.ftp.FtpClient;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Queue;
import java.util.Scanner;


/**
 Class ru.vachok.networker.net.ftp.RegRuFTPLibsUploader
 <p>
 
 @since 01.06.2019 (4:19) */
public class RegRuFTPLibsUploader implements FTPHelper, Runnable {
    
    
    private static final String FTP_SERVER = "31.31.196.85";
    
    private static final String PASSWORD_HASH = "J#��=�>�/{����\b��L�Al�\u0004e��ڏ�\u0004���N�����X�\u0018q��M�xTɾ\u001D������2�'�\b�";
    
    private String ftpPass;
    
    @Override public void run() {
        AppComponents.threadConfig().thrNameSet("ftp");
        try {
            connectTo();
        }
        catch (AccessDeniedException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".run");
        }
    }
    
    @Override public void connectTo() throws AccessDeniedException {
        FTPClient ftpClient = new FTPClient();
        FtpClient.TransferType binaryTrans = FtpClient.TransferType.BINARY;
        String dbPass = chkPass();
        if (dbPass != null) {
            this.ftpPass = dbPass;
            try {
                ftpClient.connect(getHost(), 21);
                ftpClient.configure(getConf(ftpClient));
                ftpClient.login("u0466446_java", ftpPass);
                ftpClient.setDefaultTimeout((int) ConstantsFor.DELAY);
                for (FTPFile listDirectory : ftpClient.listDirectories()) {
                    System.out.println(listDirectory);
                }
                for (FTPFile ftpFile : ftpClient.listFiles()) {
                    System.out.println("ftpFile = " + ftpFile);
                }
                ;
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        else {
            throw new AccessDeniedException("Wrong Password");
        }
        throw new IllegalComponentStateException("03.06.2019 (21:47)");
    }
    
    @Override public Queue<String> getContentsQueue() {
        return null;
    }
    
    protected String chkPass() {
        String passDB = new DBRegProperties("general-pass").getProps().getProperty("libsFTPPass");
        if (!getDigiest(passDB).equals(PASSWORD_HASH)) {
            return passDB;
        }
        else {
            return null;
        }
    }
    
    private String getDigiest(String chkStr) {
        if (chkStr == null) {
            chkStr = ftpPass;
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
    
    class PasswordEncrypter {
        
        
        String getHashString(String cipherName) {
            String retStr = "Please, enter a pass, and I'll gives you a hash:\n";
            try (Scanner scanner = new Scanner("initpass")) {
                byte[] enterPassAsBytes = scanner.nextLine().getBytes();
                MessageDigest SHADigest = MessageDigest.getInstance("SHA-512");
                byte[] digestSHA512 = SHADigest.digest(enterPassAsBytes);
                retStr = new String(digestSHA512);
            }
            catch (NoSuchAlgorithmException e) {
                System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".getHashString");
            }
            return retStr;
        }
    }
}