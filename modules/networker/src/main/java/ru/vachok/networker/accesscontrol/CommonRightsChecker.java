package ru.vachok.networker.accesscontrol;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CommonRightsChecker extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static Connection connection = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");

    private PrintWriter printWriterFails;

    private PrintWriter printWriterGood;

    /*Itinial Block*/ {
        Thread.currentThread().setName(getClass().getSimpleName());
        try {
            OutputStream osGood = new FileOutputStream(ConstantsFor.IT_FOLDER + "\\file_own.txt");
            OutputStream osFails = new FileOutputStream(ConstantsFor.IT_FOLDER + "\\file_own_failed.txt");
            printWriterFails = new PrintWriter(osFails, true);
            printWriterGood = new PrintWriter(osGood, true);
        } catch (FileNotFoundException e) {
            LOGGER.warn(e.getMessage());
        }
    }


    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        setOwnerAdmGroup(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printWriterFails.println(file.toAbsolutePath() + " visit error - " + exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        setOwnerAdmGroup(dir);
        writeOwnerToDB(dir);
        return FileVisitResult.CONTINUE;
    }

    private void writeOwnerToDB(Path dir) throws IOException {
        String owner = Files.getOwner(dir).getName();
        String sql = "insert into common (dir, user) values (?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, dir.toString());
            preparedStatement.setString(2, owner);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            uspdDB(dir, owner);
        }
    }

    private void uspdDB(Path dir, String name) {
        String sql = "UPDATE  common SET  user =  ? WHERE  dir = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, dir.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private void setOwnerAdmGroup(Path dir) {
        try {
            FileSystem fileSystem = dir.getFileSystem();
            UserPrincipal owner = Files.getOwner(dir);
            UserPrincipalLookupService userPrincipalLookupService = fileSystem.getUserPrincipalLookupService();
            UserPrincipal builtinAdm = userPrincipalLookupService.lookupPrincipalByGroupName("EATMEAT\\Domain Admins");
            String name = owner.getName();
            if (name.toLowerCase().contains("S-1-5-21-")) {
                Files.setOwner(dir, builtinAdm);
                name = Files.getOwner(dir).getName();
                String msg = dir.toString() + " user changed to: " + name;
                LOGGER.info(msg);
            }
            printWriterGood.println(dir + " " + name);
        } catch (IOException | UnsupportedOperationException e) {
            LOGGER.info(e.getMessage());
        }
    }

    static class DirectoryRights extends SimpleFileVisitor<Path> implements Runnable {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileSystem fileSystem = dir.getFileSystem();
            FileSystemProvider provider = fileSystem.provider();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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

        @Override
        public void run() {

        }
    }
}
