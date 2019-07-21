// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest */
public class CommonRightsChecker extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final String STR_FILES_IS_NOT_EXISTS = " is not exists!";
    
    private static final String STR_KILOBYTES = " kilobytes";
    
    protected static final String STR_SIZE_IN_MEGABYTES = " size in megabytes = ";
    
    private final File fileLocalCommonPointOwn = new File(ConstantsFor.FILENAME_COMMONOWN);
    
    private final File fileLocalCommonPointRgh = new File(ConstantsFor.FILENAME_COMMONRGH);
    
    private Path startPath = ConstantsFor.COMMON_DIR;
    
    private final Path logsCopyStopPath;
    
    long filesScanned;
    
    long dirsScanned;
    
    private File fileRemoteCommonPointRgh = new File("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя\\" + fileLocalCommonPointRgh.getName());
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private Path currentPath = ConstantsFor.COMMON_DIR;
    
    public CommonRightsChecker(@NotNull Path logsCopyStopPath) {
        this.logsCopyStopPath = logsCopyStopPath;
    }
    
    /**
     TEST ONLY
     
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRealRun()
     @since 09.07.2019 (15:31)
     */
    protected CommonRightsChecker(Path startPath, Path logsCopyStopPath) {
        this.startPath = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\");
        this.logsCopyStopPath = Paths.get(".").toAbsolutePath().normalize();
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName("RightsCheck".toUpperCase());
        try {
            Files.walkFileTree(startPath, this);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("CommonRightsChecker.run {0} - {1}\nParameters: []\nReturn: void\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms()
                    .fromArray(e)));
        }
        String headerMsg = "Copy of common principal files: " + copyExistsFiles();
        String titleMsg = fileRemoteCommonPointRgh.getAbsolutePath() + STR_SIZE_IN_MEGABYTES + fileRemoteCommonPointRgh.length() / ConstantsFor.MBYTE;
            File fileOwn = new File(fileRemoteCommonPointRgh.getAbsolutePath().replace("rgh", "own"));
            String bodyMsg = fileOwn.getAbsolutePath() + STR_SIZE_IN_MEGABYTES + fileOwn.length() / ConstantsFor.MBYTE;
            messageToUser.info(headerMsg, titleMsg, bodyMsg);
            
            CommonRightsParsing commonRightsParsing = new CommonRightsParsing(startPath, fileRemoteCommonPointRgh);
        
        String appendToFileInfo = filesScanned + " files, " + dirsScanned + " dirs\nAt: " + new Date();
        FileSystemWorker.appendObjectToFile(new File(getClass().getSimpleName() + ".res"), appendToFileInfo);
        messageToUser.info(appendToFileInfo);
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
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        FileVisitResult fileVisitResult = FileVisitResult.CONTINUE;
        if (file.toFile().exists() && attrs.isRegularFile()) {
            this.currentPath = file;
            this.filesScanned++;
        }
        if (file.toFile().getName().equalsIgnoreCase(ConstantsFor.FILENAME_OWNER) || file.toFile().getName().equalsIgnoreCase("folder_acl")) {
            Files.delete(file);
        }
        return fileVisitResult;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        AclFileAttributeView users = Files.getFileAttributeView(currentPath, AclFileAttributeView.class);
        UserPrincipal owner;
        try {
            owner = Files.getOwner(currentPath);
        }
        catch (NoSuchFileException e) {
            owner = Files.getOwner(dir);
        }
        if (attrs.isDirectory()) {
            this.currentPath = dir;
            this.dirsScanned++;
            checkRights(attrs);
            writeACLs(owner, users);
        }
    
        FileSystemWorker.appendObjectToFile(fileLocalCommonPointOwn,
            currentPath.toAbsolutePath().normalize() + ConstantsFor.STR_OWNEDBY + Files.getOwner(currentPath));
        FileSystemWorker.appendObjectToFile(fileLocalCommonPointRgh,
            currentPath.toAbsolutePath().normalize() + " | ACL: " + Arrays
                .toString(Files.getFileAttributeView(currentPath, AclFileAttributeView.class).getAcl().toArray()));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("Dir visited = ")
            .append(dir).append("\n")
            .append(dirsScanned).append(" total directories scanned; total files scanned: ").append(filesScanned).append("\n");
    
        System.out.println(stringBuilder);
        return FileVisitResult.CONTINUE;
    }
    
    protected void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    void setFileRemoteCommonPointRgh(File fileRemoteCommonPointRgh) {
        this.fileRemoteCommonPointRgh = fileRemoteCommonPointRgh;
    }
    
    protected void writeACLs(@NotNull Principal owner, @NotNull AclFileAttributeView users) {
        String fileName = new StringBuilder().append(currentPath.getParent()).append(ConstantsFor.FILESYSTEM_SEPARATOR).append(ConstantsFor.FILENAME_OWNER)
            .toString();
        fileName = fileName.replace("null", "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая");
        String filePathStr = currentPath.toAbsolutePath().normalize().toString();
        try {
            filePathStr = FileSystemWorker.writeFile(fileName, MessageFormat.format("Checked at: {2}.\nOWNER: {0}\nUsers:\n{1}",
                owner.toString(), users.getAcl(), LocalDateTime.now()));
        }
        catch (IOException e) {
            new File(filePathStr).delete();
        }
        
        File fileOwnerFile = new File(filePathStr);
        try {
            Files.setAttribute(Paths.get(fileOwnerFile.getAbsolutePath()), ConstantsFor.ATTRIB_HIDDEN, true);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("CommonRightsChecker.writeACLs\n{0}: {1}\nParameters: [owner, users]\nReturn: void\nStack:\n{2}", e.getClass().getTypeName(), e
                    .getMessage(), new TForms().fromArray(e)));
        }
    }
    
    private void checkRights(BasicFileAttributes attrs) throws IOException {
        UserPrincipal owner = Files.getOwner(currentPath);
        if (!owner.toString().contains("BUILTIN\\Администраторы")) {
            if (attrs.isRegularFile()) {
                setParentOwner(owner);
            }
        }
    }
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private void setParentOwner(@NotNull UserPrincipal userPrincipal) {
        try {
            Path pathSetOwner = Files.setOwner(currentPath, Files.getOwner(currentPath.getRoot()));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.setParentOwner: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    
        String headerMsg = currentPath.toAbsolutePath().normalize() + " . Changing owner...\n\n";
        String titleMsg = "Was: " + userPrincipal;
        String bodyMsg = null;
        try {
            bodyMsg = "\nNow: " + Files.getOwner(currentPath);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.setParentOwner: {0}, ({1})", e.getMessage(), e.getClass().getName()));
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
