// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import com.google.firebase.database.FirebaseDatabase;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see RegRuFTPLibsUploaderTest
 @since 01.06.2019 (4:19) */
@SuppressWarnings("ClassUnconnectedToPackage")
public class RegRuFTPLibsUploader implements Runnable {


    private static final String FTP_SERVER = "31.31.196.85";

    private final FTPClient ftpClient = getFtpClient();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RegRuFTPLibsUploader.class.getSimpleName());

    private static final File[] FILES_TO_UPLOAD = new File[2];

    @SuppressWarnings("SpellCheckingInspection") protected static final String PASSWORD_HASH = "*D0417422A75845E84F817B48874E12A21DCEB4F6";

    private String ftpPass;

    @NotNull
    private FTPClient getFtpClient() {
        FTPClient client = new FTPClient();

        try {
            client.connect(getHost(), ConstantsFor.FTP_PORT);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getFtpClient", e));
        }
        FTPClientConfig config = new FTPClientConfig();
        config.setServerTimeZoneId(ConstantsFor.TZ_MOSCOW);
        client.configure(config);
        return client;
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

    public RegRuFTPLibsUploader() {
        try {
            ftpPass = chkPass();
        }
        catch (RuntimeException e) {
            messageToUser.error("RegRuFTPLibsUploader.RegRuFTPLibsUploader", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegRuFTPLibsUploader{");
        try {
            sb.append("ftpClient=").append(ftpClient.getStatus());
        }
        catch (IOException e) {
            MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RegRuFTPLibsUploader.class.getSimpleName()).error(e.getMessage() + " see line: 110 ***");
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        if (chkPC()) {
            try {
                FirebaseDatabase.getInstance().getReference("test").setValue("Upload start!", (error, ref)->messageToUser
                    .warn(RegRuFTPLibsUploader.class.getSimpleName(), error.toException().getMessage(), " see line: 97 ***"));
                String connectTo = uploadLibs();
                MessageToUser.getInstance(MessageToUser.SWING, RegRuFTPLibsUploader.class.getSimpleName())
                    .infoTimer(Math.toIntExact(ConstantsFor.DELAY * 2), connectTo);
            }
            catch (AccessDeniedException | NullPointerException e) {
                messageToUser.error(e.getMessage() + " see line: 57");
            }
            FirebaseDatabase.getInstance().getReference("test").setValue("Upload complete!", (error, ref)->messageToUser
                .warn(RegRuFTPLibsUploader.class.getSimpleName(), error.toException().getMessage(), " see line: 106 ***"));
        }
        else {
            System.err.println(UsefulUtilities.thisPC() + " this PC is not develop PC!");
        }
    }

    private String chkPass() {
        Properties properties = InitProperties.getInstance(PropertiesNames.PROPERTIESID_GENERAL_PASS).getProps();
        String passDB = properties.getProperty(PropertiesNames.DEFPASSFTPMD5HASH);
        if (Arrays.equals(passDB.getBytes(), PASSWORD_HASH.getBytes())) {
            return properties.getProperty(PropertiesNames.REALFTPPASS);
        }
        else {
            return ConstantsFor.WRONG_PASS;
        }
    }

    @NotNull
    private String uploadLibs() throws AccessDeniedException {
        String pc;
        if (ftpPass != null) {
            try {
                return makeConnectionAndStoreLibs();
            }
            catch (IOException | RuntimeException e) {
                pc = e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace());
            }
        }
        else {
            throw new AccessDeniedException("Wrong Password");
        }
        return pc;
    }

    private boolean chkPC() {
        return UsefulUtilities.thisPC().toLowerCase().contains("home") || UsefulUtilities.thisPC().toLowerCase()
            .contains(OtherKnownDevices.DO0213_KUDR.split("\\Q.eat\\E")[0]);
    }

    @NotNull
    private String uploadToServer(@NotNull Queue<Path> pathQueue) {
        StringBuilder stringBuilder = new StringBuilder();
        while (!pathQueue.isEmpty()) {
            uploadFile(pathQueue.poll().toFile());
        }
        for (File file : getLibFiles()) {
            stringBuilder.append(uploadFile(file));
        }
        return stringBuilder.toString();
    }

    @NotNull
    private String makeConnectionAndStoreLibs() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ftpClient.connect(getHost());
            ftpClient.enterLocalPassiveMode();
            messageToUser.info(ftpClient.getReplyString());
        }
        catch (IOException e) {
            messageToUser.warn(RegRuFTPLibsUploader.class.getSimpleName(), e.getMessage(), " see line: 139 ***");
            ftpClient.connect(getHost());
            messageToUser.info(ftpClient.getReplyString());
            ftpClient.enterLocalActiveMode();
            messageToUser.info(ftpClient.getReplyString());
        }
        try {
            ftpClient.login("u0466446_java", ftpPass);
            messageToUser.info(ftpClient.getReplyString());
        }
        catch (IOException e) {
            messageToUser.warn(RegRuFTPLibsUploader.class.getSimpleName(), e.getMessage(), " see line: 150 ***");
        }

        ftpClient.setAutodetectUTF8(true);
        messageToUser.info(ftpClient.getReplyString());

        try {
            ftpClient.changeWorkingDirectory("networker.vachok.ru/lib");
            messageToUser.info(ftpClient.getReplyString());
        }
        catch (IOException e) {
            messageToUser.warn(RegRuFTPLibsUploader.class.getSimpleName(), e.getMessage(), " see line: 161 ***");
        }
        stringBuilder.append(uploadToServer(new LinkedList<>()));
        return stringBuilder.toString();
    }

    private File[] getLibFiles() {
        Path pathRoot = Paths.get(".").toAbsolutePath().normalize();
        try {
            pathRoot = pathRoot.getRoot();
            Files.walkFileTree(pathRoot, new SearchForLibs());
        }
        catch (IOException e) {
            messageToUser.warn(RegRuFTPLibsUploader.class.getSimpleName(), e.getMessage(), " see line: 237 ***");
        }
        finally {
            InitProperties instance = InitProperties.getInstance(InitProperties.DB_LOCAL);
            Properties props = instance.getProps();
            props.setProperty("app", FILES_TO_UPLOAD[0].getName());
            instance.setProps(props);
        }
        return FILES_TO_UPLOAD;
    }

    @NotNull
    private static String getName(@NotNull File file) {
        String nameFTPFile = file.getName();
        if (nameFTPFile.contains(ConstantsFor.PREF_NODE_NAME) & nameFTPFile.toLowerCase().contains(".jar")) {
            nameFTPFile = "n.jar";
        }
        else if (nameFTPFile.toLowerCase().contains(ConstantsFor.PROGNAME_OSTPST)) {
            nameFTPFile = "ost.jar";
        }
        else {
            nameFTPFile = file.getName();
        }

        return nameFTPFile;
    }

    @NotNull
    private String uploadFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Objects.requireNonNull(file).getName()).append(" local file. ");
        String nameFTPFile = getName(file);
        ftpClient.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));

        try (InputStream inputStream = new FileInputStream(file)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            messageToUser.info(ftpClient.getReplyString());
            ftpClient.enterLocalPassiveMode();
            messageToUser.info(ftpClient.getReplyString());
            boolean isStore = false;
            try {
                isStore = ftpClient.storeFile(nameFTPFile, inputStream);
                String stringReply = ftpClient.getReplyString();
                stringReply = stringReply + " file: " + nameFTPFile;
                messageToUser.info(stringReply);
            }
            catch (RuntimeException e) {
                messageToUser.warn(RegRuFTPLibsUploader.class.getSimpleName(), e.getMessage(), " see line: 222 ***");
                ftpClient.enterLocalActiveMode();
                isStore = ftpClient.storeFile(nameFTPFile, inputStream);
                String replyStr = ftpClient.getReplyString();
                messageToUser.info(replyStr);
            }
            finally {
                stringBuilder.append(setPropertyID(isStore, nameFTPFile));
            }
        }
        catch (IOException e) {
            messageToUser.warn(RegRuFTPLibsUploader.class.getSimpleName(), e.getMessage(), " see line: 233 ***");
        }
        return stringBuilder.toString();
    }

    private String setPropertyID(boolean isStore, String nameFTPFile) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(isStore).append(": ").append(nameFTPFile).append("\n");
        Properties props = InitProperties.getTheProps();
        String appIdNew;
        if (isStore) {
            appIdNew = MessageFormat.format("{0}.{1}-{2}", MyCalen.getWeekNumber(), LocalDate.now().getDayOfWeek().getValue(), (int) (LocalTime
                .now()
                .toSecondOfDay() / ConstantsFor.ONE_HOUR_IN_MIN));
        }
        else {
            appIdNew = false + " " + nameFTPFile;
        }
        props.setProperty(PropertiesNames.ID, appIdNew);
        InitProperties.getInstance(InitProperties.DB_LOCAL).setProps(props);
        InitProperties.getInstance(InitProperties.FILE).setProps(props);
        new File(FileNames.CONSTANTSFOR_PROPERTIES).setWritable(false);
        return appIdNew + "\n\n";
    }

    @NotNull
    private String checkDir(final String dirRelative) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean changeWorkingDirectory = ftpClient.changeWorkingDirectory("/cover");
        messageToUser.info(ftpClient.getReplyString());
        ;
        if (changeWorkingDirectory) {
            boolean removeDirectory = ftpClient.removeDirectory(dirRelative);
            if (dirRelative != null && dirRelative.isEmpty() && !removeDirectory) {
                checkDirContent(dirRelative);
            }
            ftpClient.makeDirectory(dirRelative);
            messageToUser.info(ftpClient.getReplyString());
            ;
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

    private class SearchForLibs extends SimpleFileVisitor<Path> {


        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyww");
            String format = simpleDateFormat.format(new Date());
            String appVersion = "8.0." + format;

            InitProperties.setPreference("version", appVersion);
            if (file.getFileName().toString().contains("networker-" + appVersion + ".jar")) {
                FILES_TO_UPLOAD[0] = file.toFile();
            }
            if (file.getFileName().toString().contains(ConstantsFor.PROGNAME_OSTPST + appVersion + ".jar")) {
                FILES_TO_UPLOAD[1] = file.toFile();
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
}