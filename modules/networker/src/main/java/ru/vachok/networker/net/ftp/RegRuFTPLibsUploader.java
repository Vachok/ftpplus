// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


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
@SuppressWarnings("ClassUnconnectedToPackage") public class RegRuFTPLibsUploader implements LibsHelp, Runnable {
    
    
    private static final String FTP_SERVER = "31.31.196.85";
    
    public void setFtpPass(String ftpPass) {
        this.ftpPass = ftpPass;
    }
    
    private String ftpPass = "null";
    
    private MessageToUser messageToUser = new MessageSwing();
    
    @SuppressWarnings("SpellCheckingInspection") private static final String PASSWORD_HASH = "*D0417422A75845E84F817B48874E12A21DCEB4F6";
    
    @Override public void run() {
        AppComponents.threadConfig().thrNameSet("ftp");
        try {
            String connectTo = uploadLibs();
            messageToUser.infoTimer(Math.toIntExact(ConstantsFor.DELAY * 2), connectTo);
        }
        catch (AccessDeniedException e) {
            messageToUser.error(e.getMessage() + " " + getClass().getSimpleName() + ".run");
        }
    }
    
    @Override public String uploadLibs() throws AccessDeniedException {
        FTPClient ftpClient = new FTPClient();
        String dbPass = null;
        try {
            dbPass = chkPass();
        }
        catch (AuthenticationException e) {
            messageToUser.error(e.getMessage());
        }
        if (ConstantsFor.thisPC().toLowerCase().contains("home") | ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.HOSTNAME_DO213) && dbPass != null) {
            return continueConnect(dbPass, ftpClient);
        }
        else {
            throw new AccessDeniedException("Wrong Password");
        }
    }
    
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
    
    @Override public Queue<String> getContentsQueue() {
        throw new IllegalComponentStateException("04.06.2019 (17:25)");
    }
    
    protected String chkPass() throws AuthenticationException {
        Properties properties = new DBRegProperties("general-pass").getProps();
        String passDB = properties.getProperty("defpassftpmd5hash");
        if (!getDigest(passDB).equals(PASSWORD_HASH)) {
            return properties.getProperty("realftppass");
        }
        else {
            throw new AuthenticationException("WRONG PASSWORD");
        }
    }
    
    private String continueConnect(String dbPass, FTPClient ftpClient) {
        this.ftpPass = dbPass;
        try {
            ftpClient.connect(getHost(), ConstantsFor.FTP_PORT);
            FTPClientConfig config = new FTPClientConfig();
            config.setServerTimeZoneId("Europe/Moscow");
            ftpClient.configure(config);
            return makeConnectionAndStoreLibs(ftpClient);
        }
        catch (IOException e) {
            return e.getMessage();
        }
    }
    
    String makeConnectionAndStoreLibs(FTPClient ftpClient) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
    
        ftpClient.login("u0466446_java", ftpPass);
        System.out.println(ftpClient.getReplyString());
    
        ftpClient.setAutodetectUTF8(true);
        System.out.println(ftpClient.getReplyString());
        
        ftpClient.changeWorkingDirectory("/lib");
        System.out.println(ftpClient.getReplyString());
        
        File[] libsToStore = getLibFiles();
        for (File file : libsToStore) {
            stringBuilder.append(uploadFile(file, ftpClient));
        }
        return stringBuilder.toString();
    }
    
    String uploadFile(File file, FTPClient ftpClient) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(file.getAbsolutePath()).append(" local file. ");
        try (InputStream inputStream = new FileInputStream(file)) {
    
            String nameFTPFile = getName(file);
            stringBuilder.append(nameFTPFile).append(" remote name.\n");
    
            ftpClient.enterLocalPassiveMode();
            stringBuilder.append(ftpClient.getReplyString()).append("PassiveMode".toLowerCase()).append(".\n");
    
            stringBuilder.append("Is file stored to server: ");
            boolean isStore = ftpClient.storeFile(nameFTPFile, inputStream);
            stringBuilder.append(isStore).append(", reply: ").append(ftpClient.getReplyString()).append("\n");
        }
        catch (IOException e) {
            return e.getMessage();
        }
        System.out.println("stringBuilder = " + stringBuilder);
        return stringBuilder.toString();
    }
    
    private String getName(File file) {
        String nameFTPFile = file.getName();
        if (nameFTPFile.contains("networker")) {
            nameFTPFile = "n.jar";
        }
        else {
            nameFTPFile = "ost.jar";
        }
        return nameFTPFile;
    }
    
    private FTPClientConfig getConf(FTPClient ftpClient) {
        FTPClientConfig ftpClientConfig = new FTPClientConfig();
        ftpClientConfig.setUnparseableEntries(true);
        return ftpClientConfig;
    }
    
    File[] getLibFiles() {
        File[] retMassive = new File[2];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyw");
        String format = simpleDateFormat.format(new Date());
        String appVersion = "8.0." + format;
        Path pathRoot = Paths.get(".").toAbsolutePath().normalize();
        String fileSeparator = System.getProperty(ConstantsFor.PRSYS_SEPARATOR);
        retMassive[0] = new File(pathRoot + fileSeparator + ConstantsFor.PR_APP_BUILD + fileSeparator + "libs" + fileSeparator + "networker-" + appVersion + ".jar");
        retMassive[1] = new File(pathRoot + fileSeparator + ConstantsFor.PROGNAME_OSTPST
            .replace("-", "") + fileSeparator + ConstantsFor.PR_APP_BUILD + fileSeparator + "libs" + fileSeparator + ConstantsFor.PROGNAME_OSTPST + appVersion + ".jar");
        return retMassive;
    }
    
    protected InetAddress getHost() {
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
    
    private String getDigest(String chkStr) {
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
    
}