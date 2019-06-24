// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest */
@SuppressWarnings ("DuplicateStringLiteralInspection")
public class CommonRightsChecker extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private final File commonOwn = new File(ConstantsFor.FILENAME_COMMONOWN);
    
    private final File commonRgh = new File(ConstantsFor.FILENAME_COMMONRGH);
    
    long countFiles = 0;
    
    private Path toCheckPath;
    
    public CommonRightsChecker() {
    }
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    public CommonRightsChecker(Path toCheckPath) {
        this.toCheckPath = toCheckPath;
    }
    
    /**
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRun()
     */
    @Override public void run() {
        try {
            messageToUser.info(getClass().getSimpleName() + ".run", "true", " = " + isDelete());
            if (toCheckPath != null) {
                Files.walkFileTree(toCheckPath, this);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
            List<AclEntry> acl = fileAttributeView.getAcl();
            FileSystemWorker.appendObjToFile(commonRgh, acl);
            FileSystemWorker.appendObjToFile(commonOwn, Files.getOwner(dir));
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(attrs.isRegularFile()){
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(file, AclFileAttributeView.class);
            FileSystemWorker.appendObjToFile(commonOwn, file.toAbsolutePath().normalize() + " owned by: " + Files.getOwner(file));
            FileSystemWorker.appendObjToFile(commonRgh, file.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(fileAttributeView.getAcl().toArray()));
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
        boolean b1 = Files.deleteIfExists(commonOwn.toPath());
        boolean b = Files.deleteIfExists(new File(ConstantsFor.FILENAME_COMMONRGH).toPath());
        String msg = new StringBuilder()
            .append("Starting a new instance of ")
            .append(getClass().getSimpleName())
            .append(" at ").append(new Date())
            .append("\ncommon.rgh and common.own deleted : ")
            .append(b)
            .append(" ")
            .append(b1).toString();
        return msg;
    }
}
