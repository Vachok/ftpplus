// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.OtherKnownDevices;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 @see ru.vachok.networker.net.libswork.RegRuFTPLibsUploaderTest
 @since 01.06.2019 (4:19) */
@SuppressWarnings("ClassUnconnectedToPackage")
public class RegRuFTPLibsUploader implements Runnable {
    
    
    private static final String FTP_SERVER = "31.31.196.85";
    
    @SuppressWarnings("SpellCheckingInspection") protected static final String PASSWORD_HASH = "*D0417422A75845E84F817B48874E12A21DCEB4F6";
    
    private static final Pattern PATTERN = Pattern.compile("\\Q\\\\E");
    
    private final FTPClient ftpClient = getFtpClient();
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.SWING, RegRuFTPLibsUploader.class.getSimpleName());
    
    private static File[] retMassive = new File[2];
    
    private String ftpPass = chkPass();
    
    @Override
    public void run() {
        
        if (chkPC()) {
            try {
                String connectTo = uploadLibs();
                messageToUser.infoTimer(Math.toIntExact(ConstantsFor.DELAY * 2), connectTo);
            }
            catch (AccessDeniedException | NullPointerException e) {
                messageToUser.error(e.getMessage() + " " + getClass().getSimpleName() + ".run");
            }
        }
        else {
            System.err.println(UsefulUtilities.thisPC() + " this PC is not develop PC!");
        }
    }
    
    public String uploadLibs() throws AccessDeniedException {
        String pc = UsefulUtilities.thisPC();
        if (ftpPass != null) {
            try {
                return makeConnectionAndStoreLibs();
            }
            catch (IOException | NullPointerException e) {
                return FileSystemWorker.error(getClass().getSimpleName() + ".uploadLibs", e);
            }
        }
        else {
            throw new AccessDeniedException("Wrong Password");
        }
    }
    
    File[] getLibFiles() {
    
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyw");
        String format = simpleDateFormat.format(new Date());
        String appVersion = "8.0." + format;
        Path pathRoot = Paths.get(".").toAbsolutePath().normalize();
        try {
            pathRoot = pathRoot.getRoot();
            for (Path path : Files.walkFileTree(pathRoot, new SearchForLibs())) {
                System.out.println(path.toAbsolutePath());
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return retMassive;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegRuFTPLibsUploader{");
        try {
            sb.append("ftpClient=").append(ftpClient.getStatus());
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("RegRuFTPLibsUploader.toString: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        sb.append('}');
        return sb.toString();
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
    
    private String chkPass() {
        Properties properties = new DBRegProperties(PropertiesNames.PRID_PASS).getProps();
        String passDB = properties.getProperty(PropertiesNames.DEFPASSFTPMD5HASH);
        if (Arrays.equals(passDB.getBytes(), PASSWORD_HASH.getBytes())) {
            return properties.getProperty("realftppass");
        }
        else {
            return ConstantsFor.WRONG_PASS;
        }
    }
    
    private @NotNull FTPClient getFtpClient() {
        FTPClient client = new FTPClient();
        
        try {
            client.connect(getHost(), ConstantsFor.FTP_PORT);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getFtpClient", e));
        }
        FTPClientConfig config = new FTPClientConfig();
        config.setServerTimeZoneId("Europe/Moscow");
        client.configure(config);
        return client;
    }
    
    private @NotNull String uploadToServer(@NotNull Queue<Path> pathQueue) {
        StringBuilder stringBuilder = new StringBuilder();
        
        while (!pathQueue.isEmpty()) {
            uploadFile(pathQueue.poll().toFile());
        }
        for (File file : getLibFiles()) {
            stringBuilder.append(uploadFile(file));
        }
        
        return stringBuilder.toString();
    }
    
    private @NotNull String makeConnectionAndStoreLibs() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ftpClient.connect(getHost());
            ftpClient.enterLocalPassiveMode();
            stringBuilder.append(ftpClient.getReplyString());
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".makeConnectionAndStoreLibs");
            ftpClient.connect(getHost());
            stringBuilder.append(ftpClient.getReplyString());
            ftpClient.enterLocalActiveMode();
            stringBuilder.append(ftpClient.getReplyString());
        }
        try {
            ftpClient.login("u0466446_java", ftpPass);
            stringBuilder.append(ftpClient.getReplyString());
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + "LOGIN ERROR", e));
        }
        
        ftpClient.setAutodetectUTF8(true);
        stringBuilder.append(ftpClient.getReplyString());
        
        try {
            ftpClient.changeWorkingDirectory("/lib");
            stringBuilder.append(ftpClient.getReplyString());
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + "CWD ERROR", e));
        }
        stringBuilder.append(uploadToServer(new LinkedList<>()));
        return stringBuilder.toString();
    }
    
    private @NotNull String checkDir(final String dirRelative) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean changeWorkingDirectory = ftpClient.changeWorkingDirectory("/cover");
        stringBuilder.append(ftpClient.getReplyString());
        if (changeWorkingDirectory) {
            boolean removeDirectory = ftpClient.removeDirectory(dirRelative);
            if (dirRelative != null && dirRelative.isEmpty() && !removeDirectory) {
                checkDirContent(dirRelative);
            }
            ftpClient.makeDirectory(dirRelative);
            stringBuilder.append(ftpClient.getReplyString());
        }
        return stringBuilder.toString();
    }
    
    private void checkDirContent(String dirRelative) throws IOException {
        ftpClient.changeWorkingDirectory(dirRelative);
        System.out.println(ftpClient.getReplyString());
        
        for (FTPFile ftpFile : ftpClient.listFiles()) {
            boolean deleteFile = ftpClient.deleteFile(ftpFile.getLink());
            System.out.println(ftpClient.getReplyString());
            String isDel = ftpFile.getName() + " is deleted: " + deleteFile;
            System.out.println("isDel = " + isDel);
        }
        for (FTPFile ftpDir : ftpClient.listDirectories()) {
            System.out.println(checkDir(ftpDir.getLink()));
        }
    }
    
    private @NotNull String uploadFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Objects.requireNonNull(file).getAbsolutePath()).append(" local file. ");
        String nameFTPFile = getName(file);
        ftpClient.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
        stringBuilder.append(ftpClient.getReplyString());
        
        try (InputStream inputStream = new FileInputStream(file)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println(ftpClient.getReplyString());
            stringBuilder.append(nameFTPFile).append(" remote name.\n");
    
            ftpClient.enterLocalPassiveMode();
            stringBuilder.append(ftpClient.getReplyString());
    
            stringBuilder.append("Is file stored to server: ");
            boolean isStore;
            try {
                isStore = ftpClient.storeFile(nameFTPFile, inputStream);
                String stringReply = ftpClient.getReplyString();
                stringBuilder.append(stringReply);
                System.out.println(stringReply + " file: " + nameFTPFile);
            }
            catch (RuntimeException e) {
                ftpClient.enterLocalActiveMode();
                stringBuilder.append(ftpClient.getReplyString());
                isStore = ftpClient.storeFile(nameFTPFile, inputStream);
                String replyStr = ftpClient.getReplyString();
                stringBuilder.append(replyStr).append(" Exception: ").append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
                System.out.println(replyStr);
            }
            stringBuilder.append(isStore).append(": ").append(nameFTPFile).append("\n");
        }
        catch (IOException e) {
            stringBuilder.append(RegRuFTPLibsUploader.class.getSimpleName()).append(" uploadFile : ").append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    private static @NotNull String getName(@NotNull File file) {
        String nameFTPFile = file.getName();
        if (nameFTPFile.contains(ConstantsFor.PREF_NODE_NAME) & nameFTPFile.toLowerCase().contains(".jar")) {
            nameFTPFile = "n.jar";
        }
        else if (nameFTPFile.toLowerCase().contains("ostpst-")) {
            nameFTPFile = "ost.jar";
        }
        else {
            nameFTPFile = file.getName();
        }
        
        return nameFTPFile;
    }
    
    private class SearchForLibs extends SimpleFileVisitor<Path> {
        
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyw");
            String format = simpleDateFormat.format(new Date());
            String appVersion = "8.0." + format;
            if (file.toFile().getName().contains("networker-" + appVersion + ".jar")) {
                retMassive[0] = file.toFile();
            }
            if (file.toFile().getName().contains(ConstantsFor.PROGNAME_OSTPST + appVersion + ".jar")) {
                retMassive[1] = file.toFile();
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
    
    private boolean chkPC() {
        return UsefulUtilities.thisPC().toLowerCase().contains("home") || UsefulUtilities.thisPC().toLowerCase().contains(OtherKnownDevices.DO0213_KUDR.split("\\Q.eat\\E")[0]);
    }
}