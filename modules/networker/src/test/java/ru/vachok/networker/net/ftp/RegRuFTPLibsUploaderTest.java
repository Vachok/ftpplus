// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.ConstantsFor;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.AccessDeniedException;


public class RegRuFTPLibsUploaderTest extends RegRuFTPLibsUploader {
    
    
    @Test()
    public void ftpTest() {
        LibsHelp libsHelp = new RegRuFTPLibsUploader();
        try {
            libsHelp.uploadLibs();
        }
        catch (AccessDeniedException | ConnectException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".ftpTest");
        }
    }
    
    @Test
    public void chkPassTest() {
        String ftpPass = null;
        try {
            ftpPass = new RegRuFTPLibsUploader().chkPass();
        }
        catch (AuthenticationException e) {
            Assert.assertNull(e, e.getMessage());
        }
        Assert.assertNotNull(ftpPass);
        System.out.println("ftpPass = " + ftpPass);
    }
    
    @Test
    public void continueConnect() {
        FTPClient ftpClient = new FTPClient();
        String dbPass = new DBRegProperties("general-pass").getProps().getProperty("realftppass");
        setFtpPass(dbPass);
        try {
            ftpClient.connect(super.getHost(), ConstantsFor.FTP_PORT);
            FTPClientConfig config = new FTPClientConfig();
            config.setServerTimeZoneId("Europe/Moscow");
            ftpClient.configure(config);
            makeConnectionAndStoreLibs(ftpClient);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
/*
    private String makeConnectionAndStoreLibs(FTPClient ftpClient, String ftpPass) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
    
        ftpClient.login("u0466446_java", ftpPass);
        System.out.println(ftpClient.getReplyString());
    
        ftpClient.setAutodetectUTF8(true);
        System.out.println(ftpClient.getReplyString());
    
        ftpClient.changeWorkingDirectory("/lib");
        System.out.println(ftpClient.getReplyString());
    
        File[] libsToStore = new File[2];
        libsToStore[0]= new File("C:\\Users\\ikudryashov\\IdeaProjects\\ftpplus\\modules\\networker\\build\\libs\\networker-8.0.1923.jar");
        libsToStore[1] = new File("C:\\Users\\ikudryashov\\IdeaProjects\\ftpplus\\modules\\networker\\ostpst\\build\\libs\\ostpst-8.0.1923.jar");
        for (File file : libsToStore) {
            stringBuilder.append(uploadFile(file, ftpClient));
        }
        return stringBuilder.toString();
    }
    

    private String uploadFile(File file, FTPClient ftpClient) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(file.getAbsolutePath()).append(" local file. ");
        
        try (InputStream inputStream = new FileInputStream(file)) {
            String nameFTPFile = getName(file);
            stringBuilder.append(nameFTPFile).append(" remote name.\n");
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            stringBuilder.append(ftpClient.getReplyString()).append(" file transfer mode, ");
            ftpClient.enterLocalPassiveMode();
            stringBuilder.append(ftpClient.getReplyString()).append("PassiveMode".toLowerCase()).append(".\n");
        
            stringBuilder.append("Is file stored to server: ");
            boolean isStrore = ftpClient.storeFile(nameFTPFile, inputStream);
            stringBuilder.append(isStrore).append(", reply: ").append(ftpClient.getReplyString()).append("\n");
        }
        catch (IOException e) {
            return e.getMessage();
        }
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
    
    private InetAddress getHost() {
        InetAddress ftpAddr = InetAddress.getLoopbackAddress();
        try {
            byte[] addressBytes = InetAddress.getByName("vachok.ru").getAddress();
            ftpAddr = InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        return ftpAddr;
    }
*/

}