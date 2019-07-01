// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest */
public class CommonRightsChecker extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private final File commonOwn = new File(ConstantsFor.FILENAME_COMMONOWN);
    
    private final File commonRgh = new File(ConstantsFor.FILENAME_COMMONRGH);
    
    long countFiles;
    
    private Path toCheckPath;
    
    private Path logsCopyPath;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private static final String STR_FILES_IS_NOT_EXISTS = " is not exists!";
    
    private static final String STR_KILOBYTES = " kilobytes";
    
    public CommonRightsChecker(Path toCheckPath, @NotNull Path logsCopyPath) {
        this.toCheckPath = toCheckPath;
        this.logsCopyPath = logsCopyPath;
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonRightsChecker{");
        sb.append("commonOwn=").append(commonOwn);
        sb.append(", commonRgh=").append(commonRgh);
        sb.append(", countFiles=").append(countFiles);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRun()
     */
    @Override public void run() {
        try {
            if (toCheckPath != null) {
                Files.walkFileTree(toCheckPath, this);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("copyExistsFiles() = " + copyExistsFiles());
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
            List<AclEntry> acl = fileAttributeView.getAcl();
            FileSystemWorker.appendObjectToFile(commonRgh, acl);
            FileSystemWorker.appendObjectToFile(commonOwn, Files.getOwner(dir));
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(file, AclFileAttributeView.class);
            FileSystemWorker.appendObjectToFile(commonOwn, file.toAbsolutePath().normalize() + " owned by: " + Files.getOwner(file));
            FileSystemWorker.appendObjectToFile(commonRgh, file.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(fileAttributeView.getAcl().toArray()));
            this.countFiles++;
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        System.out.println("visited = " + dir + " files total scanned: " + countFiles);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    private String isDelete() throws IOException {
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
    
        if (copyDoesNotNeed(cOWNCopyPath, cRGHCopyPath)) {
            throw new IllegalInvokeEx(getClass().getTypeName() + " copy files with common rights does not need");
        }
        else {
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
    
    private boolean copyDoesNotNeed(Path cOWNCopyPath, Path cRGHCopyPath) {
        return (cOWNCopyPath.toFile().lastModified() > commonOwn.lastModified()) & (cRGHCopyPath.toFile().lastModified() > commonRgh.lastModified());
    }
}
