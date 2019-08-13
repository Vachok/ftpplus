// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.ad.user.FileADUsersParser;
import ru.vachok.networker.ad.user.UserInformation;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 
 @since 02.10.2018 (17:32) */
public class PCUserResolver extends ADSrv implements UserInformation {
    
    
    private static final String METHNAME_REC_AUTO_DB = "PCUserResolver.recAutoDB";
    
    private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
    
    private static final Pattern USERS = Pattern.compile("Users");
    
    private String lastUsersDirFileUsedName;
    
    private String pcName;
    
    public PCUserResolver() {
    }
    
    @Override
    public String getInfoAbout(String samAccountName) {
        this.pcName = samAccountName;
        return getInfoAbout();
    }
    
    @Override
    public void setInfo(Object info) {
        MessageToUser messageToUser = (MessageToUser) info;
    }
    
    @Override
    public List<ADUser> getADUsers() {
        UserInformation userInformation = new FileADUsersParser();
        return userInformation.getADUsers();
    }
    
    private @NotNull String getInfoAbout() {
        System.out.println();
        String namesToFile = new PCUserResolver.WalkerToUserFolder().namesToFile();
        System.out.println(namesToFile);
        System.out.println();
        File file = new File("err");
        try {
            file = new File("\\\\" + pcName + "\\c$\\users\\" + namesToFile.split(" ")[0]);
        }
        catch (IndexOutOfBoundsException ignore) {
            //
        }
        return file.getAbsolutePath() + " " + file.length() + ConstantsFor.STR_BYTES;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PCUserResolver{");
        sb.append("lastUsersDirFileUsedName='").append(lastUsersDirFileUsedName).append('\'');
        sb.append(", pcName='").append(pcName).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private void searchForUser() {
        ADUser adUser = new ADUser();
        UserInformation adUsersSRV = new FileADUsersParser(adUser);
        Queue<String> usersCsvQueue = FileSystemWorker.readFileEncodedToQueue(new File(getClass().getResource(FileNames.USERS_CSV).getFile()).toPath(), "UTF-16LE");
        List<ADUser> adUsers = getADUsers();
        System.out.println("adUsers = " + new TForms().fromArray(adUsers));
    }
    
    private static class DatabaseWriter {
        
        
        private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
        
        /**
         Записывает инфо о пльзователе в <b>pcuserauto</b>
         <p>
         Записи добавляются к уже имеющимся.
         <p>
         <b>{@link SQLException}, {@link ArrayIndexOutOfBoundsException}, {@link NullPointerException}: </b>
         1. {@link FileSystemWorker#error(String, Exception)} <br>
         
         @param pcName имя ПК
         @param lastFileUse строка - имя последнего измененного файла в папке пользователя.
         */
        private static void recAutoDB(String pcName, String lastFileUse) {
            
            final String sql = "insert into pcuser (pcName, userName, lastmod, stamp) values(?,?,?,?)";
            
            try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
                final String sqlReplaced = COMPILE.matcher(sql).replaceAll(ConstantsFor.DBFIELD_PCUSERAUTO);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlReplaced)) {
                    String[] split = lastFileUse.split(" ");
                    preparedStatement.setString(1, pcName);
                    preparedStatement.setString(2, split[0]);
                    preparedStatement.setString(3, UsefulUtilities.thisPC());
                    preparedStatement.setString(4, split[7]);
                    System.out.println(preparedStatement.executeUpdate() + " " + sql);
                }
                catch (SQLException e) {
                
                }
            }
            catch (SQLException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            
            }
        }
    }
    
    
    
    /**
     Поиск файлов в папках {@code c-users}.
     
     @see #getLastTimeUse(String)
     @since 22.11.2018 (14:46)
     */
    private class WalkerToUserFolder extends SimpleFileVisitor<Path> {
        
        
        /**
         new {@link ArrayList}, список файлов, с отметками {@link File#lastModified()}
         
         @see #visitFile(Path, BasicFileAttributes)
         */
        private final List<String> timePath = new ArrayList<>();
        
        /**
         Предпросмотр директории.
         <p>
         До листинга файлов.
         
         @param dir {@link Path}
         @param attrs {@link BasicFileAttributes}
         @return {@link FileVisitResult#CONTINUE}
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }
        
        /**
         Просмотр файла.
         <p>
         Добавляет в {@link #timePath}: <br>
         Время модификации файла {@link File#lastModified()} + файл {@link Path#toString()} + new {@link Date}(java.io.File#lastModified()) + {@link File#lastModified()}.
         
         @param file {@link Path}
         @param attrs {@link BasicFileAttributes}
         @return {@link FileVisitResult#CONTINUE}
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            timePath.add(file.toFile().lastModified() + " " + file + " " + new Date(file.toFile().lastModified()) + " " + file.toFile().lastModified());
            return FileVisitResult.CONTINUE;
        }
        
        /**
         Просмотр файла не удался.
         <p>
         
         @param file {@link Path}
         @param exc {@link IOException}
         @return {@link FileVisitResult#CONTINUE}
         */
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
        
        /**
         Постпросмотр директории.
         <p>
         После листинга файлов.
         
         @param dir {@link Path}
         @param exc {@link IOException}
         @return {@link FileVisitResult#CONTINUE}
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("WalkerToUserFolder{");
            sb.append("timePath=").append(timePath);
            sb.append('}');
            return sb.toString();
        }
        
        /**
         @return {@link #timePath}
         */
        @Contract(pure = true)
        private List<String> getTimePath() {
            return timePath;
        }
        
        private String namesToFile() {
            File[] files;
            File pcNameFile = new File("null");
            try {
                pcNameFile = Files.createTempFile(pcName, ".tmp").toFile();
                pcNameFile.deleteOnExit();
            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            }
            
            try (OutputStream outputStream = new FileOutputStream(pcNameFile)) {
                try (PrintWriter writer = new PrintWriter(outputStream, true)) {
                    String pathAsStr = new StringBuilder().append("\\\\").append(pcName).append("\\c$\\Users\\").toString();
                    lastUsersDirFileUsedName = USERS.split(getLastTimeUse(pathAsStr))[1];
                    files = new File(pathAsStr).listFiles();
                    writer
                        .append(PATTERN.matcher(Arrays.toString(files)).replaceAll(Matcher.quoteReplacement("\n")))
                        .append("\n\n\n")
                        .append(lastUsersDirFileUsedName);
                }
            }
            catch (IOException | ArrayIndexOutOfBoundsException ignored) {
                //
            }
            catch (NullPointerException n) {
                System.err.println(new TForms().fromArray(n, false));
            }
            if (lastUsersDirFileUsedName != null) {
                PCUserResolver.DatabaseWriter.recAutoDB(pcName, lastUsersDirFileUsedName);
                return lastUsersDirFileUsedName;
            }
            pcNameFile.deleteOnExit();
            return MessageFormat.format("{0} exists {1}", pcNameFile.toPath().toAbsolutePath().normalize(), pcNameFile.exists());
        }
        
        private String getLastTimeUse(String pathAsStr) {
            Thread.currentThread().setName(this.getClass().getSimpleName());
            
            PCUserResolver.WalkerToUserFolder walkerToUserFolder = new PCUserResolver.WalkerToUserFolder();
            try {
                if (InetAddress.getByName(pcName).isReachable(ConstantsFor.TIMEOUT_650)) {
                    Files.walkFileTree(Paths.get(pathAsStr), Collections.singleton(FOLLOW_LINKS), 2, walkerToUserFolder);
                }
                List<String> timePath = walkerToUserFolder.getTimePath();
                Collections.sort(timePath);
                return timePath.get(timePath.size() - 1);
            }
            catch (IOException | IndexOutOfBoundsException e) {
                return e.getMessage() + " " + getClass().getSimpleName() + ".getLastTimeUse";
            }
        }
        
        
    }
}
