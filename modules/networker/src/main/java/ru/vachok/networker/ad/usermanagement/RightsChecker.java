// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
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
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.common.usermanagement.RightsCheckerTest */
public class RightsChecker extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final String STR_FILES_IS_NOT_EXISTS = " is not exists!";
    
    private static final String STR_KILOBYTES = " kilobytes";
    
    protected static final String STR_SIZE_IN_MEGABYTES = " size in megabytes = ";
    
    private final File fileLocalCommonPointOwn = new File(FileNames.FILENAME_COMMONOWN);
    
    private final File fileLocalCommonPointRgh = new File(FileNames.FILENAME_COMMONRGH);
    
    private Path startPath = ConstantsFor.COMMON_DIR;
    
    private final Path logsCopyStopPath;
    
    private long lastModDir;
    
    long filesScanned;
    
    long dirsScanned;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    public RightsChecker(@NotNull Path logsCopyStopPath) {
        this.logsCopyStopPath = logsCopyStopPath;
        if (fileLocalCommonPointOwn.exists()) {
            fileLocalCommonPointOwn.delete();
        }
        if (fileLocalCommonPointRgh.exists()) {
            fileLocalCommonPointRgh.delete();
        }
    }
    
    public RightsChecker(Path start, Path logs) {
        this.startPath = start;
        this.logsCopyStopPath = logs;
    }
    
    @Override
    public void run() {
        final long timeStart = System.currentTimeMillis();
        try {
            Files.walkFileTree(startPath, this);
            copyExistsFiles(timeStart);
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
            if (file.toFile().getName().equals(FileNames.FILENAME_OWNER)) {
                file.toFile().delete();
            }
            else if (file.toFile().getName().equals(FileNames.FILENAME_FOLDERACLTXT)) {
                file.toFile().delete();
            }
            else if (file.toFile().getName().equals(FileNames.FILENAME_OWNER + ".replacer")) {
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
        this.lastModDir = attrs.lastModifiedTime().toMillis();
        AclFileAttributeView users = Files.getFileAttributeView(dir, AclFileAttributeView.class);
        UserPrincipal owner = Files.getOwner(dir);
        if (attrs.isDirectory()) {
            this.dirsScanned++;
            Files.setAttribute(dir, ConstantsFor.ATTRIB_HIDDEN, false);
            FileSystemWorker.appendObjectToFile(fileLocalCommonPointOwn, dir.toAbsolutePath().normalize() + ConstantsFor.STR_OWNEDBY + owner);
            //Изменение формата ломает: CommonRightsParsing.rightsWriterToFolderACL
            String objectToFile = FileSystemWorker
                .appendObjectToFile(fileLocalCommonPointRgh, dir.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(users.getAcl().toArray()));
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
            new ConcreteFolderACLWriter(dir).run();
            dir.toFile().setLastModified(lastModDir);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    protected void copyExistsFiles(final long timeStart) {
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
        
        File forAppend = new File(this.getClass().getSimpleName() + ".res");
        
        FileSystemWorker.appendObjectToFile(forAppend, MessageFormat.format("{2}) {0} dirs scanned, {1} files scanned. Elapsed: {3}\n",
            this.dirsScanned, this.filesScanned, new Date(), TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timeStart)));
    }
}
