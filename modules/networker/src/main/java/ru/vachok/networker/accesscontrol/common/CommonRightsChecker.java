// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
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
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    public CommonRightsChecker(@NotNull Path logsCopyStopPath) {
        this.logsCopyStopPath = logsCopyStopPath;
        if (fileLocalCommonPointOwn.exists()) {
            fileLocalCommonPointOwn.delete();
        }
        if (fileLocalCommonPointRgh.exists()) {
            fileLocalCommonPointRgh.delete();
        }
    }
    
    public CommonRightsChecker(Path start, Path logs) {
        this.startPath = start;
        this.logsCopyStopPath = logs;
    }
    
    @Override
    public void run() {
        try {
            Files.walkFileTree(startPath, this);
            copyExistsFiles();
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.run: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
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
        if (file.toFile().exists() && attrs.isRegularFile()) {
            this.filesScanned++;
            if (file.toFile().getName().equals(ConstantsFor.FILENAME_OWNER)) {
                file.toFile().delete();
            }
            else if (file.toFile().getName().equals(ConstantsFor.FILENAME_FOLDERACLTXT)) {
                file.toFile().delete();
            }
            else if (file.toFile().getName().equals("owner")) {
                file.toFile().delete();
            }
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        AclFileAttributeView users = Files.getFileAttributeView(dir, AclFileAttributeView.class);
        UserPrincipal owner = Files.getOwner(dir);
        if (attrs.isDirectory()) {
            this.dirsScanned++;
            Files.setAttribute(dir, ConstantsFor.ATTRIB_HIDDEN, false);
            FileSystemWorker.appendObjectToFile(fileLocalCommonPointOwn, dir.toAbsolutePath().normalize() + ConstantsFor.STR_OWNEDBY + owner);
            //Изменение формата ломает: CommonRightsParsing.rightsWriterToFolderACL
            FileSystemWorker.appendObjectToFile(fileLocalCommonPointRgh, dir.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(users.getAcl().toArray()));
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("Dir visited = ")
            .append(dir).append("\n")
            .append(dirsScanned).append(" total directories scanned; total files scanned: ").append(filesScanned).append("\n");
        System.out.println(stringBuilder);
        if (dir.toFile().isDirectory()) {
            new CommonConcreteFolderACLWriter(dir).run();
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    protected void copyExistsFiles() {
        if (!logsCopyStopPath.toAbsolutePath().toFile().exists()) {
            try {
                Files.createDirectories(logsCopyStopPath);
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat.format("CommonRightsChecker.copyExistsFiles: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        
        Path cRGHCopyPath = Paths.get(logsCopyStopPath.toAbsolutePath().normalize() + ConstantsFor.FILESYSTEM_SEPARATOR + fileLocalCommonPointRgh.getName());
        Path cOWNCopyPath = Paths.get(logsCopyStopPath.toAbsolutePath().normalize() + ConstantsFor.FILESYSTEM_SEPARATOR + fileLocalCommonPointOwn.getName());
        
        boolean isOWNCopied = true;
        boolean isRGHCopied = true;
        
        if (fileLocalCommonPointOwn.exists()) {
            FileSystemWorker.copyOrDelFile(fileLocalCommonPointOwn, cOWNCopyPath, true);
        }
        if (fileLocalCommonPointRgh.exists()) {
            FileSystemWorker.copyOrDelFile(fileLocalCommonPointRgh, cRGHCopyPath, true);
        }
        
        FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName() + ".res"), MessageFormat
            .format("{0} dirs scanned, {1} files scanned\n{2}\n\n", this.dirsScanned, this.filesScanned, new Date()));
    }
    
    
}
