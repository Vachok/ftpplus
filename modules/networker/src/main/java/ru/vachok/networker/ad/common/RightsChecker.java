// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.ad.common.RightsCheckerTest */
public class RightsChecker extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private final File fileLocalCommonPointOwn = new File(FileNames.FILENAME_COMMONOWN);
    
    private final File fileLocalCommonPointRgh = new File(FileNames.FILENAME_COMMONRGH);
    
    private final Path logsCopyStopPath;
    
    private static final Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection(ConstantsFor.STR_VELKOM);
    
    private long filesScanned;
    
    private long dirsScanned;
    
    private Path startPath = ConstantsFor.COMMON_DIR;
    
    private long lastModDir;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
    
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
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        new RightsWriter(file.toAbsolutePath().normalize().toString(), ConstantsFor.STR_ERROR, exc.getMessage()).writeDBCommonTable();
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
            
            new RightsWriter(dir.toAbsolutePath().normalize().toString(), owner.toString(), Arrays.toString(users.getAcl().toArray())).writeDBCommonTable();
        }
        return FileVisitResult.CONTINUE;
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
    
    private void copyExistsFiles(final long timeStart) {
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
    


    /**
     @since 11.09.2019 (16:16)
     */
    private class RightsWriter {
        
        
        private String dir;
        
        private String owner;
        
        private String acl;
        
        @Contract(pure = true)
        RightsWriter(String dir, String owner, String acl) {
            this.dir = dir;
            this.owner = owner;
            this.acl = acl;
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("RightsWriter{");
            sb.append(", owner='").append(owner).append('\'');
            sb.append(", dir='").append(dir).append('\'');
            sb.append(", acl='").append(acl).append('\'');
            sb.append('}');
            return sb.toString();
        }
        
        void writeDBCommonTable() {
            final String sql = "INSERT INTO common (`dir`, `user`, `users`) VALUES (?,?,?)";
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, dir);
                preparedStatement.setString(2, owner);
                preparedStatement.setString(3, acl);
                preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                if (e.getMessage().contains(ConstantsFor.STR_DUPLICATE)) {
                    updateRecord();
                }
                else {
                    messageToUser.error("RightsWriter", "writeDBCommonTable", e.getMessage() + " see line: 222");
                }
                
            }
        }
        
        private void updateRecord() {
            final String sql = "UPDATE common SET dir = ?, user = ?, users = ? WHERE dir = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, dir);
                preparedStatement.setString(2, owner);
                preparedStatement.setString(3, acl);
                preparedStatement.setString(4, dir);
                
                preparedStatement.executeUpdate();
            }
            catch (SQLException ignore) {
                //11.09.2019 (17:43)
            }
        }
    }
}
