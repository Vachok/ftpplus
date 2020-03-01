// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.ad.common.RightsCheckerTest */
public class RightsChecker extends SimpleFileVisitor<Path> implements Runnable {


    public static final String FOLDERACL_TXT = "folder_acl.txt";

    private final File fileLocalCommonPointOwn = new File(FileNames.COMMON_OWN);

    private final File fileLocalCommonPointRgh = new File(FileNames.COMMON_RGH);

    private final Path logsCopyStopPath;

    private final long startClass;

    private static final String TABLE_FULL_NAME = ModelAttributeNames.COMMON + ConstantsFor.SQLTABLE_POINTCOMMON;

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RightsChecker.class.getSimpleName());

    private long filesScanned;

    private long dirsScanned;

    private Path startPath = ConstantsFor.COMMON_DIR;

    private long lastModDir;

    public RightsChecker(@NotNull Path logsCopyStopPath) {
        this.logsCopyStopPath = logsCopyStopPath;
        if (fileLocalCommonPointOwn.exists()) {
            fileLocalCommonPointOwn.delete();
        }
        if (fileLocalCommonPointRgh.exists()) {
            fileLocalCommonPointRgh.delete();
        }
        startClass = System.currentTimeMillis();
    }

    public RightsChecker(Path start, Path logs) {
        this.startPath = start;
        this.logsCopyStopPath = logs;
        startClass = System.currentTimeMillis();
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
        finally {
            FirebaseDatabase.getInstance().getReference(MessageFormat.format("{0}:{1}", UsefulUtilities.thisPC(), getClass().getSimpleName()))
                .setValue(new Date(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        messageToUser
                            .error("RightsChecker.onComplete", error.toException().getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace()));
                    }
                });
        }
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
            FileSystemWorker.appendObjectToFile(fileLocalCommonPointRgh, dir.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(users.getAcl().toArray()));

            new RightsWriter(dir.toAbsolutePath().normalize().toString(), owner.toString(), Arrays.toString(users.getAcl().toArray())).writeDBCommonTable();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        new RightsWriter(file.toAbsolutePath().normalize().toString(), ConstantsFor.STR_ERROR, exc.getMessage()).writeDBCommonTable();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toFile().exists() && attrs.isRegularFile()) {
            this.filesScanned++;
            if (file.toFile().getName().equals(FileNames.FILENAME_OWNER)) {
                file.toFile().delete();
            }
            else if (file.toFile().getName().equals(FOLDERACL_TXT)) {
                file.toFile().delete();
            }
            else if (file.toFile().getName().equals(FileNames.FILENAME_OWNER + ".replacer")) {
                file.toFile().delete();
            }
            else if (file.toFile().getName().equals(ConstantsFor.OWNER)) {
                file.toFile().delete();
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("Dir visited = ")
                .append(dir).append("\n")
                .append(dirsScanned).append(" total directories scanned; total files scanned: ").append(filesScanned).append("\n");
        long secondsScan = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClass);
        if (secondsScan == 0) {
            secondsScan = 1;
        }
        stringBuilder.append(dirsScanned / secondsScan).append(" dirs/sec, ").append(filesScanned / secondsScan).append(" files/sec.\n");
        if (dir.toFile().isDirectory()) {
            new ConcreteFolderACLWriter(dir).run();
            dir.toFile().setLastModified(lastModDir);
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

            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(TABLE_FULL_NAME);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(TABLE_FULL_NAME);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
