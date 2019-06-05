// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.libswork;


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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 Class ru.vachok.networker.net.libswork.RegRuFTPLibsUploader
 <p>
 
 @since 01.06.2019 (4:19) */
@SuppressWarnings("ClassUnconnectedToPackage") public class RegRuFTPLibsUploader implements LibsHelp, Runnable {
    
    
    private static final String FTP_SERVER = "31.31.196.85";
    
    @SuppressWarnings("SpellCheckingInspection") private static final String PASSWORD_HASH = "*D0417422A75845E84F817B48874E12A21DCEB4F6";
    
    private static final Pattern COMPILE = Pattern.compile("-", Pattern.LITERAL);
    
    private static MessageToUser messageToUser = new MessageSwing();
    
    private static FTPClient ftpClient = new FTPClient();
    
    private String ftpPass;
    
    
    {
        try {
            ftpPass = chkPass();
        }
        catch (AuthenticationException e) {
            messageToUser.error(getClass().getSimpleName(), "INITIALIZE AUTH ERROR", e.getMessage());
        }
    }
    
    
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
        String dbPass = "Wrong pass";
        try {
            dbPass = chkPass();
        }
        catch (AuthenticationException e) {
            messageToUser.error(e.getMessage());
        }
        if (ConstantsFor.thisPC().toLowerCase().contains("home") | ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.HOSTNAME_DO213) && dbPass != null) {
            this.ftpPass = dbPass;
            return makeConnectionAndStoreLibs();
        }
        else {
            throw new AccessDeniedException("Wrong Password");
        }
    }
    
    @Override public Queue<String> getContentsQueue() {
        throw new IllegalComponentStateException("04.06.2019 (17:25)");
    }
    
    File[] getLibFiles() {
        File[] retMassive = new File[2];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyw");
        String format = simpleDateFormat.format(new Date());
        String appVersion = "8.0." + format;
        Path pathRoot = Paths.get(".").toAbsolutePath().normalize();
        String fileSeparator = System.getProperty(ConstantsFor.PRSYS_SEPARATOR);
        retMassive[0] = new File(pathRoot + fileSeparator + ConstantsFor.PR_APP_BUILD + fileSeparator + "libs" + fileSeparator + "networker-" + appVersion + ".jar");
        retMassive[1] = new File(pathRoot + fileSeparator + COMPILE.matcher(ConstantsFor.PROGNAME_OSTPST).replaceAll(Matcher
            .quoteReplacement("")) + fileSeparator + ConstantsFor.PR_APP_BUILD + fileSeparator + "libs" + fileSeparator + ConstantsFor.PROGNAME_OSTPST + appVersion + ".jar");
        return retMassive;
    }
    
    FTPClient getFtpClient() {
        try {
            RegRuFTPLibsUploader.ftpClient.connect(getHost(), ConstantsFor.FTP_PORT);
            FTPClientConfig config = new FTPClientConfig();
            config.setServerTimeZoneId("Europe/Moscow");
            RegRuFTPLibsUploader.ftpClient.configure(config);
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), "getFtpClient", e.getMessage());
        }
        return RegRuFTPLibsUploader.ftpClient;
    }
    
    protected void setFtpPass(String ftpPass) {
        this.ftpPass = ftpPass;
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
    
    protected InetAddress getHost() {
        InetAddress ftpAddress = InetAddress.getLoopbackAddress();
        try {
            byte[] addressBytes = InetAddress.getByName(FTP_SERVER).getAddress();
            ftpAddress = InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            messageToUser.error(RegRuFTPLibsUploader.class.getSimpleName(), "getHost", e.getMessage());
        }
        return ftpAddress;
    }
    
    private String makeConnectionAndStoreLibs() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ftpClient.connect(getHost());
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), "CONNECT ERROR", e.getMessage());
        }
        try {
            ftpClient.login("u0466446_java", ftpPass);
            System.out.println(ftpClient.getReplyString());
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), "LOGIN ERROR", e.getMessage());
        }
        
        ftpClient.setAutodetectUTF8(true);
        System.out.println(ftpClient.getReplyString());
        
        try {
            ftpClient.changeWorkingDirectory("/lib");
            System.out.println(ftpClient.getReplyString());
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), "CWD ERROR", e.getMessage());
        }
        
        File[] libsToStore = getLibFiles();
        for (File file : libsToStore) {
            stringBuilder.append(uploadFile(file));
        }
        return stringBuilder.toString();
    }
    
    private static String uploadFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(file.getAbsolutePath()).append(" local file. ");
        String nameFTPFile = getName(file);
        
        try (InputStream inputStream = new FileInputStream(file)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println(ftpClient.getReplyString());
            
            stringBuilder.append(nameFTPFile).append(" remote name.\n");
            
            ftpClient.enterLocalPassiveMode();
            System.out.println(ftpClient.getReplyString());
            
            stringBuilder.append("Is file stored to server: ");
            boolean isStore = ftpClient.storeFile(nameFTPFile, inputStream);
            System.out.println(ftpClient.getReplyString());
            stringBuilder.append(isStore).append(", reply: ").append(ftpClient.getReplyString()).append("\n");
        }
        catch (IOException e) {
            messageToUser.error(RegRuFTPLibsUploader.class.getSimpleName(), "uploadFile", e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    private static String getName(File file) {
        String nameFTPFile = file.getName();
        if (nameFTPFile.contains("networker") & nameFTPFile.toLowerCase().contains(".jar")) {
            nameFTPFile = "n.jar";
        }
        else if (nameFTPFile.toLowerCase().contains("ostpst-")) {
            nameFTPFile = "ost.jar";
        }
        return nameFTPFile;
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
            messageToUser.error(RegRuFTPLibsUploader.class.getSimpleName(), "getDigest", e.getMessage());
        }
        return new String(dBytes);
    }
    
}