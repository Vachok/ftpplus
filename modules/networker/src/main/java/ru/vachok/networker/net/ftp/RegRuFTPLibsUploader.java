// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;

import javax.naming.AuthenticationException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    
    private MessageToUser messageToUser = new MessageSwing();
    
    private String ftpPass;
    
    @Override public void run() {
        AppComponents.threadConfig().thrNameSet("ftp");
        try {
            String connectTo = connectTo();
            messageToUser.infoTimer(30, connectTo);
        }
        catch (AccessDeniedException | ConnectException e) {
            messageToUser.error(e.getMessage() + " " + getClass().getSimpleName() + ".run");
        }
    }
    
    @Override public String connectTo() throws AccessDeniedException, ConnectException {
        FTPClient ftpClient = new FTPClient();
        String dbPass = null;
        try {
            dbPass = chkPass();
        }
        catch (AuthenticationException e) {
            messageToUser.error(e.getMessage());
        }
        if (ConstantsFor.thisPC().toLowerCase().contains("home") | ConstantsFor.thisPC().toLowerCase().contains("do0213") && dbPass != null) {
            this.ftpPass = dbPass;
            try {
                ftpClient.connect(getHost(), 21);
                FTPClientConfig config = getConf(ftpClient);
                config.setServerTimeZoneId("Europe/Moscow");
                ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                ftpClient.type(FTP.ASCII_FILE_TYPE);
                ftpClient.stru(FTP.FILE_STRUCTURE);
                ftpClient.configure(config);
    
                return makeConnectionAndStoreLibs(ftpClient);
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        else {
            throw new AccessDeniedException("Wrong Password");
        }
        
        throw new ConnectException("Some problems with connection...");
    }
    
    @Override public Queue<String> getContentsQueue() {
        throw new IllegalComponentStateException("04.06.2019 (17:25)");
    }
    
    protected String chkPass() throws AuthenticationException {
        Properties properties = new DBRegProperties("general-pass").getProps();
        String passDB = properties.getProperty("defpassftpmd5hash");
        if (!new PasswordEncrypter().getDigiest(passDB).equals(PASSWORD_HASH)) {
            return properties.getProperty("realftppass");
        }
        else {
            throw new AuthenticationException("WRONG PASSWORD");
        }
    }
    
    private String makeConnectionAndStoreLibs(FTPClient ftpClient) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ftpClient.login("u0466446_java", ftpPass);
        ftpClient.setDefaultTimeout((int) ConstantsFor.DELAY);
        ftpClient.changeWorkingDirectory("/lib");
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
                boolean storeFile = ftpClient.storeFile(nameFTPFile, inputStream);
                SimpleDateFormat timeValFormat = new SimpleDateFormat("YYYYMMDDhhmmss");
                ftpClient.mfmt(nameFTPFile, timeValFormat.format(new Date()));
                stringBuilder.append(nameFTPFile).append(" ").append(storeFile).append(" ").append(ftpClient.getStatus(nameFTPFile)).append("\n");
            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        
        return stringBuilder.toString();
    }
    
    private File[] getLibFiles() {
        File[] retMassive = new File[2];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyw");
        String format = simpleDateFormat.format(new Date());
        String appVersion = "8.0." + format;
        Path pathRoot = Paths.get(".").toAbsolutePath().normalize();
        String fileSeparator = System.getProperty("file.separator");
        retMassive[0] = new File(pathRoot + fileSeparator + "build" + fileSeparator + "libs" + fileSeparator + "networker-" + appVersion + ".jar");
        retMassive[1] = new File(pathRoot + fileSeparator + "ostpst" + fileSeparator + "build" + fileSeparator + "libs" + fileSeparator + "ostpst-" + appVersion + ".jar");
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