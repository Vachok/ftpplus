// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
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
    
    private final File fileLocalCommonPointOwn = new File(ConstantsFor.FILENAME_COMMONOWN);
    
    private final File fileLocalCommonPointRgh = new File(ConstantsFor.FILENAME_COMMONRGH);
    
    private final Path startPath;
    
    private final Path logsCopyStopPath;
    
    long filesScanned;
    
    long dirsScanned;
    
    private File fileRemoteCommonPointRgh = new File("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя\\" + fileLocalCommonPointRgh.getName());
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private Path pathToCheck;
    
    public CommonRightsChecker(Path startPath, @NotNull Path logsCopyStopPath) {
        this.startPath = startPath;
        this.logsCopyStopPath = logsCopyStopPath;
    }
    
    /**
     TEST ONLY
     
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRealRun()
     @since 09.07.2019 (15:31)
     */
    protected CommonRightsChecker() {
        this.startPath = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\");
        this.logsCopyStopPath = Paths.get(".");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonRightsChecker{");
        sb.append("fileLocalCommonPointOwn=").append(fileLocalCommonPointOwn);
        sb.append(", fileLocalCommonPointRgh=").append(fileLocalCommonPointRgh);
        
        sb.append(", filesScanned=").append(filesScanned);
        sb.append(", dirsScanned=").append(dirsScanned);
        
        sb.append(", startPath=").append(startPath);
        sb.append(", logsCopyStopPath=").append(logsCopyStopPath);
        
        sb.append('}');
        return sb.toString();
    }
    
    /**
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRun()
     */
    @Override public void run() {
        try {
            if (startPath != null) {
                Files.walkFileTree(startPath, this);
                String headerMsg = "Copy of common principal files: " + copyExistsFiles();
                String titleMsg = fileRemoteCommonPointRgh.getAbsolutePath() + STR_SIZE_IN_MEGABYTES + fileRemoteCommonPointRgh.length() / ConstantsFor.MBYTE;
                File fileOwn = new File(fileRemoteCommonPointRgh.getAbsolutePath().replace("rgh", "own"));
                String bodyMsg = fileOwn.getAbsolutePath() + STR_SIZE_IN_MEGABYTES + fileOwn.length() / ConstantsFor.MBYTE;
    
                messageToUser.info(headerMsg, titleMsg, bodyMsg);
        
                CommonRightsParsing commonRightsParsing = new CommonRightsParsing(startPath, fileRemoteCommonPointRgh);
                
                commonRightsParsing.rightsWriterToFolderACL();
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    
        String appendToFileInfo = filesScanned + " files, " + dirsScanned + " dirs\nAt: " + new Date();
        FileSystemWorker.appendObjectToFile(new File(getClass().getSimpleName() + ".res"), appendToFileInfo);
        messageToUser.info(appendToFileInfo);
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        FileVisitResult fileVisitResult = FileVisitResult.CONTINUE;
        for (String pattern : delPatterngList()) {
            fileVisitResult = delTrash(file, pattern.toLowerCase());
        }
    
        if (file.toFile().exists() && attrs.isRegularFile()) {
            this.pathToCheck = file;
            checkRights();
            this.filesScanned++;
        }
        return fileVisitResult;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            this.pathToCheck = dir;
            checkRights();
            this.dirsScanned++;
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        StringBuilder stringBuilder = new StringBuilder()
            .append("Dir visited = ")
            .append(dir).append("\n")
            .append(dirsScanned).append(" total directories scanned; total files scanned: ").append(filesScanned).append("\n");
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
    
    void setFileRemoteCommonPointRgh(File fileRemoteCommonPointRgh) {
        this.fileRemoteCommonPointRgh = fileRemoteCommonPointRgh;
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
    
    private void checkRights() throws IOException {
        UserPrincipal owner = Files.getOwner(pathToCheck);
        AclFileAttributeView users = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        
        if (!owner.toString().contains("BUILTIN\\Администраторы")) {
    
            if (pathToCheck.toFile().isFile()) {
                setParentOwner(owner);
            }
            owner = Files.getOwner(pathToCheck.getRoot());
            users = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
            if (pathToCheck.toFile().isDirectory()) {
                FileSystemWorker.writeFile(pathToCheck + ConstantsFor.FILESYSTEM_SEPARATOR + "owner", MessageFormat
                    .format("OWNER: {0}\n\nUsers:\n{1}", owner.toString(), users.getAcl()));
            }
        }
        
        FileSystemWorker.appendObjectToFile(fileLocalCommonPointOwn, pathToCheck.toAbsolutePath().normalize() + ConstantsFor.STR_OWNEDBY + owner);
        FileSystemWorker
            .appendObjectToFile(fileLocalCommonPointRgh, pathToCheck.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(users.getAcl().toArray()));
    }
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private void setParentOwner(@NotNull UserPrincipal userPrincipal) {
        
        try {
            Path pathSetOwner = Files.setOwner(pathToCheck, Files.getOwner(pathToCheck.getRoot()));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.setParentOwner: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        
        String headerMsg = pathToCheck.toAbsolutePath().normalize() + " . Changing owner...\n\n";
        String titleMsg = "Was: " + userPrincipal;
        String bodyMsg = null;
        try {
            bodyMsg = "\nNow: " + Files.getOwner(pathToCheck);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.setParentOwner: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    
        messageToUser.info(headerMsg, titleMsg, bodyMsg);
        
        setParentACL();
    }
    
    private void setParentACL() {
        String rootPlusOne = pathToCheck.getRoot().toAbsolutePath().normalize().toString();
        rootPlusOne += pathToCheck.getName(0);
        Path rootPath = Paths.get(rootPlusOne);
        
        AclFileAttributeView rootPlusOneACL = Files.getFileAttributeView(rootPath, AclFileAttributeView.class);
        AclFileAttributeView currentFileACL = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        
        try {
            
            currentFileACL.getAcl().forEach(acl->messageToUser.info(rootPath.toString(), String.valueOf(acl.type()), acl.toString()));
            rootPlusOneACL.getAcl().forEach(acl->messageToUser.info(pathToCheck.toString(), String.valueOf(acl.type()), acl.toString()));
            
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.setParentACL threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private @NotNull String isDelete() throws IOException {
        boolean isOWNFileDeleted = Files.deleteIfExists(fileLocalCommonPointOwn.toPath().toAbsolutePath().normalize());
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
        Path cRGHCopyPath = Paths
            .get(logsCopyStopPath.toAbsolutePath().normalize() + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + fileLocalCommonPointRgh.getName());
        Path cOWNCopyPath = Paths
            .get(logsCopyStopPath.toAbsolutePath().normalize() + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + fileLocalCommonPointOwn.getName());
    
        boolean isOWNCopied = true;
        boolean isRGHCopied = true;
    
        if (fileLocalCommonPointOwn.exists()) {
            isOWNCopied = FileSystemWorker.copyOrDelFile(fileLocalCommonPointOwn, cOWNCopyPath, true);
            }
        if (fileLocalCommonPointRgh.exists()) {
            isRGHCopied = FileSystemWorker.copyOrDelFile(fileLocalCommonPointRgh, cRGHCopyPath, true);
            }
    
        return isOWNCopied & isRGHCopied;
    
    }
}
