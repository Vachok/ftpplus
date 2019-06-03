// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;


/**
 Class ru.vachok.networker.net.ftp.RegRuFTPLibsUploader
 <p>
 
 @since 01.06.2019 (4:19) */
public class RegRuFTPLibsUploader implements FTPHelper, Runnable {
    
    
    private static final String FTP_SERVER = "31.31.196.85";
    
    private static final String PASSWORD_HASH = "*D0417422A75845E84F817B48874E12A21DCEB4F6";
    
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
        String dbPass = chkPass();
        if (dbPass != null) {
            this.ftpPass = dbPass;
            try {
                ftpClient.connect(getHost(), 21);
                System.out.print(ftpClient.getReplyString());
    
                FTPClientConfig config = getConf(ftpClient);
                config.setServerTimeZoneId("Europe/Moscow");
                ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                ftpClient.type(FTP.ASCII_FILE_TYPE);
                ftpClient.stru(FTP.FILE_STRUCTURE);
                ftpClient.configure(config);
    
                makeConnectionAndStoreLibs(ftpClient);
            }
            catch (IOException e) {
                System.err.println(e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
        }
        else {
            throw new AccessDeniedException("Wrong Password");
        }
    
    }
    
    protected String chkPass() {
        Properties properties = new DBRegProperties("general-pass").getProps();
        String passDB = properties.getProperty("defpassftpmd5hash");
        if (!new PasswordEncrypter().getDigiest(passDB).equals(PASSWORD_HASH)) {
            return properties.getProperty("realftppass");
        }
        else {
            return null;
        }
    }
    
    private void makeConnectionAndStoreLibs(FTPClient ftpClient) throws IOException {
        ftpClient.login("u0466446_java", ftpPass);
        ftpClient.setDefaultTimeout((int) ConstantsFor.DELAY);
        ftpClient.getStatus();
        ftpClient.changeWorkingDirectory("/lib");
        System.out.println(ftpClient.printWorkingDirectory());
        File[] libsToStore = getLibFiles();
        for (File file : libsToStore) {
            try (InputStream inputStream = new FileInputStream(file)) {
                String nameFTPFile = file.getName();
                if (nameFTPFile.contains("networker")) {
                    nameFTPFile = "n.jar";
                }
                else {
                    nameFTPFile = "ost.jar";
                }
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(nameFTPFile, inputStream);
            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    @Override public Queue<String> getContentsQueue() {
        return null;
    }
    
    private File[] getLibFiles() {
        File[] retMassive = new File[2];
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            String appVersion = "8.0.1923"; //fixme
            retMassive[0] = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\build\\libs\\networker-" + appVersion + ".jar");
            retMassive[1] = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\ostpst\\build\\libs\\ostpst-" + appVersion + ".jar");
        }
        else {
            throw new IllegalArgumentException(ConstantsFor.thisPC());
        }
        return retMassive;
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
                MessageDigest cipDigest = MessageDigest.getInstance("MD5");
                byte[] digestMD5 = cipDigest.digest(enterPassAsBytes);
                retStr = new String(digestMD5);
            }
            catch (NoSuchAlgorithmException e) {
                System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".getHashString");
                System.out.println("You need to put file, with name \"pass\", to program main directory: " + Paths.get(".").toAbsolutePath().normalize() + " , for init you new pass");
            }
            return retStr;
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
    
        private void decryptMe() throws NoSuchAlgorithmException {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        }
    }
}