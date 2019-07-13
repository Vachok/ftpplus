// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.ad.user.DataBaseADUsersSRV;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.ADSrv;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 <b>Ищет имя пользователя</b>
 
 @since 02.10.2018 (17:32) */
public class PCUserResolver extends ADSrv implements InfoWorker {
    
    
    private static final String METHNAME_REC_AUTO_DB = "PCUserResolver.recAutoDB";
    
    private static final MessageToUser messageToUser = new MessageLocal(PCUserResolver.class.getSimpleName());
    
    private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
    
    private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
    
    private static final Pattern USERS = Pattern.compile("Users");
    
    /**
     Последний измененный файл.
     
     @see #getLastTimeUse(String)
     */
    private String lastUsersDirFileUsedName;
    
    private String pcName;
    
    public PCUserResolver(String pcName) {
        this.pcName = pcName;
    }
    
    @Override
    public String getInfoAbout() {
        System.out.println();
        System.out.println(namesToFile());
        System.out.println();
        File file = new File(pcName);
        return file.getAbsolutePath() + " " + file.length() + ConstantsFor.STR_BYTES;
    }
    
    @Override
    public void setInfo() {
        getInfoAbout();
    }
    
    private void searchForUser() {
        ADUser adUser = new ADUser();
        DataBaseADUsersSRV adUsersSRV = new DataBaseADUsersSRV(adUser);
        Map<String, String> fileParser = adUsersSRV
            .fileParser(FileSystemWorker
                .readFileToQueue(Paths.get("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\src\\main\\resources\\static\\texts\\users.txt")));
        Set<String> stringSet = fileParser.keySet();
        stringSet.forEach(x->{
            String s = fileParser.get(x);
            if (s.contains(pcName)) {
                messageToUser.infoNoTitles(s + " " + s.contains(pcName));
            }
        });
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
            recAutoDB(pcName, lastUsersDirFileUsedName);
            return lastUsersDirFileUsedName;
        }
        pcNameFile.deleteOnExit();
        return pcNameFile.toPath().toAbsolutePath().normalize() + " exists " + pcNameFile.exists();
    }
    
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
    private void recAutoDB(String pcName, String lastFileUse) {
        this.pcName = pcName;
        this.lastUsersDirFileUsedName = lastFileUse;
        final String sql = "insert into pcuser (pcName, userName, lastmod, stamp) values(?,?,?,?)";
        
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            final String sqlReplaced = COMPILE.matcher(sql).replaceAll(ConstantsFor.DBFIELD_PCUSERAUTO);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlReplaced)) {
                String[] split = lastFileUse.split(" ");
                preparedStatement.setString(1, pcName);
                preparedStatement.setString(2, split[0]);
                preparedStatement.setString(3, ConstantsFor.thisPC());
                preparedStatement.setString(4, split[7]);
                System.out.println(preparedStatement.executeUpdate() + " " + sql);
            }
            catch (SQLException e) {
                System.err.println(e.getMessage() + " " + getClass().getSimpleName());
            }
        }
        catch (SQLException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName());
        }
    }
    
    /**
     Ищет в подпапках папки Users, файлы.
     <p>
     Работа в new {@link PCUserResolver.WalkerToUserFolder}. <br> В {@link Files#walkFileTree(Path, Set, int, FileVisitor)}, отправляем параметры: <br> 1. Путь<br>
     2. {@link FileVisitOption#FOLLOW_LINKS} <br> 3. Макс. глубина 2 <br> 4. {@link PCUserResolver.WalkerToUserFolder}
     <p>
     Сортируем {@link PCUserResolver.WalkerToUserFolder#getTimePath()} по Timestamp. От меньшего к большему.
     
     @param pathAsStr путь, который нужно пролистать.
     @return {@link PCUserResolver.WalkerToUserFolder#getTimePath()} последняя запись из списка.
     */
    private String getLastTimeUse(String pathAsStr) {
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
    
    /**
     Поиск файлов в папках {@code c-users}.
     
     @see #getLastTimeUse(String)
     @since 22.11.2018 (14:46)
     */
    private static class WalkerToUserFolder extends SimpleFileVisitor<Path> {
        
        
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
        
        /**
         @return {@link #timePath}
         */
        private List<String> getTimePath() {
            return timePath;
        }
    }
}
