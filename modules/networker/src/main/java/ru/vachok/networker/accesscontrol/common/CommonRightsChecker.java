// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.DBMessenger;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest */
public class CommonRightsChecker extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final String STR_FILES_IS_NOT_EXISTS = " is not exists!";
    
    private static final String STR_KILOBYTES = " kilobytes";
    
    protected static final String STR_SIZE_IN_MEGABYTES = " size in megabytes = ";
    
    private final File commonOwn = new File(ConstantsFor.FILENAME_COMMONOWN);
    
    private final File commonRgh = new File(ConstantsFor.FILENAME_COMMONRGH);
    
    long countFiles;
    
    long countDirs;
    
    private File commonRghFile = new File("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя\\" + commonRgh.getName());
    
    private Path toCheckPath;
    
    private Path logsCopyPath;
    
    private MessageToUser messageToUser = new DBMessenger(getClass().getSimpleName());
    
    public CommonRightsChecker(Path toCheckPath, @NotNull Path logsCopyPath) {
        this.toCheckPath = toCheckPath;
        this.logsCopyPath = logsCopyPath;
    }
    
    /**
     TEST ONLY
     
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRealRun()
     @since 09.07.2019 (15:31)
     */
    protected CommonRightsChecker() {
        this.toCheckPath = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\");
        this.logsCopyPath = Paths.get(".");
    }
    
    /**
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRun()
     */
    @Override public void run() {
        try {
            if (toCheckPath != null) {
                Files.walkFileTree(toCheckPath, this);
                String headerMsg = "Copy of common principal files: " + copyExistsFiles();
                String titleMsg = commonRghFile.getAbsolutePath() + STR_SIZE_IN_MEGABYTES + commonRghFile.length() / ConstantsFor.MBYTE;
                File fileOwn = new File(commonRghFile.getAbsolutePath().replace("rgh", "own"));
                String bodyMsg = fileOwn.getAbsolutePath() + STR_SIZE_IN_MEGABYTES + fileOwn.length() / ConstantsFor.MBYTE;
    
                messageToUser.info(headerMsg, titleMsg, bodyMsg);
    
                CommonRightsParsing commonRightsParsing = new CommonRightsParsing(toCheckPath, commonRghFile);
                commonRightsParsing.rightsWriterToFolderACL();
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    
        String appendToFileInfo = countFiles + " files, " + countDirs + " dirs\nAt: " + new Date();
        FileSystemWorker.appendObjectToFile(new File(getClass().getSimpleName() + ".res"), appendToFileInfo);
        messageToUser.info(appendToFileInfo);
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonRightsChecker{");
        sb.append("commonOwn=").append(commonOwn);
        sb.append(", commonRgh=").append(commonRgh);
        sb.append(", countFiles=").append(countFiles);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        FileVisitResult fileVisitResult = FileVisitResult.CONTINUE;
        for (String pattern : delPatterngList()) {
            fileVisitResult = delTrash(file, pattern.toLowerCase());
        }
    
        if (file.toFile().exists() && attrs.isRegularFile()) {
            checkRights(file);
            this.countFiles++;
        }
        return fileVisitResult;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            checkRights(dir);
            this.countDirs++;
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        StringBuilder stringBuilder = new StringBuilder()
            .append("Dir visited = ")
            .append(dir).append("\n")
            .append(countDirs).append(" total directories scanned; total files scanned: ").append(countFiles).append("\n");
        if (exc != null) {
            stringBuilder.append(new TForms().fromArray(exc, false));
        }
        System.out.println(stringBuilder);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    void setCommonRghFile(File commonRghFile) {
        this.commonRghFile = commonRghFile;
    }
    
    private FileVisitResult delTrash(@NotNull Path file, String pattern) {
        if (file.toAbsolutePath().toString().toLowerCase().contains(pattern)) {
            try {
                Files.delete(file);
                System.out.println("DELETE = " + file + " " + file.toFile().exists());
            }
            catch (IOException e) {
                file.toFile().deleteOnExit();
            }
        }
        return FileVisitResult.CONTINUE;
    }
    
    private List<String> delPatterngList() {
        List<String> retList = new ArrayList<>();
        try (InputStream inputStream = getClass().getResourceAsStream("/static/delfromcommon.txt");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(retList::add);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return retList;
    }
    
    private void checkRights(Path pathToCheck) throws IOException {
        UserPrincipal userPrincipal = Files.getOwner(pathToCheck);
        AclFileAttributeView fileAttributeView = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        
        if (userPrincipal.toString().contains(ConstantsFor.STR_UNKNOWN)) {
            ifOwnerUnknown(pathToCheck, userPrincipal);
            userPrincipal = Files.getOwner(pathToCheck.getRoot());
            fileAttributeView = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        }
    
        FileSystemWorker.appendObjectToFile(commonOwn, pathToCheck.toAbsolutePath().normalize() + ConstantsFor.STR_OWNEDBY + userPrincipal);
        FileSystemWorker.appendObjectToFile(commonRgh, pathToCheck.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(fileAttributeView.getAcl().toArray()));
    }
    
    private void ifOwnerUnknown(Path pathToCheck, @NotNull UserPrincipal userPrincipal) throws IOException {
        Path pathSetOwner = Files.setOwner(pathToCheck, Files.getOwner(pathToCheck.getRoot()));
        
        String headerMsg = pathToCheck.toAbsolutePath().normalize() + " . Changing owner...";
        String titleMsg = "Was: " + userPrincipal;
        String bodyMsg = "Now: " + Files.getOwner(pathToCheck);
        messageToUser.info(headerMsg, titleMsg, bodyMsg);
        
        ifACLUnknown(pathToCheck);
    }
    
    private void ifACLUnknown(@NotNull Path pathToCheck) throws IOException {
        String rootPlusOne = pathToCheck.getRoot().toAbsolutePath().normalize().toString();
        rootPlusOne += pathToCheck.getName(0);
        
        AclFileAttributeView rootPlusOneACL = Files.getFileAttributeView(Paths.get(rootPlusOne), AclFileAttributeView.class);
        AclFileAttributeView currentFileACL = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        
        currentFileACL.getAcl().forEach(acl->messageToUser.info(acl.toString()));
        rootPlusOneACL.getAcl().forEach(acl->messageToUser.info(acl.toString()));
    }
    
    private @NotNull String isDelete() throws IOException {
        boolean isOWNFileDeleted = Files.deleteIfExists(commonOwn.toPath().toAbsolutePath().normalize());
        boolean isRGHFileDeleted = Files.deleteIfExists(new File(ConstantsFor.FILENAME_COMMONRGH).toPath().toAbsolutePath().normalize());
        return new StringBuilder()
            .append("Starting a new instance of ")
            .append(getClass().getSimpleName())
            .append(" at ").append(new Date())
            .append("\ncommon.rgh and common.own deleted : ")
            .append(isRGHFileDeleted)
            .append(" ")
            .append(isOWNFileDeleted).toString();
    }
    
    private boolean copyExistsFiles() {
        Path cRGHCopyPath = Paths.get(logsCopyPath.toAbsolutePath().normalize() + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + commonRgh.getName());
        Path cOWNCopyPath = Paths.get(logsCopyPath.toAbsolutePath().normalize() + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + commonOwn.getName());
    
        boolean isOWNCopied = true;
        boolean isRGHCopied = true;
    
        if (commonOwn.exists()) {
                isOWNCopied = FileSystemWorker.copyOrDelFile(commonOwn, cOWNCopyPath, true);
            }
        if (commonRgh.exists()) {
                isRGHCopied = FileSystemWorker.copyOrDelFile(commonRgh, cRGHCopyPath, true);
            }
    
        return isOWNCopied & isRGHCopied;
    
    }
}
