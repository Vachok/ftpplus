// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PCInformation;
import ru.vachok.networker.restapi.message.MessageLocal;

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
public class PCUserNameResolver extends PCInformation {
    
    
    private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
    
    private static final Pattern USERS = Pattern.compile("Users");
    
    private boolean isFullInfo = true;
    
    private String lastUsersDirFileUsedName;
    
    private String pcName = PCInformation.getPcName();
    
    public PCUserNameResolver(InformationFactory informationFactory) {
        this.informationFactory = informationFactory;
    }
    
    private InformationFactory informationFactory;
    
    public PCUserNameResolver(String aboutWhat) {
        this.pcName = aboutWhat;
    }
    
    @Override
    public String getInfo() {
        return informationFactory.getInfoAbout(pcName);
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.isFullInfo = (boolean) classOption;
    }
    
    @Override
    public String getInfoAbout(String samAccountName) {
        this.pcName = samAccountName;
        return getHTMLCurrentUserName();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PCUserResolver{");
        sb.append("lastUsersDirFileUsedName='").append(lastUsersDirFileUsedName).append('\'');
        sb.append(", pcName='").append(pcName).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull String getHTMLCurrentUserName() {
        List<String> timeName = getLastUserFolderFile();
        String timesUserLast = timeName.get(timeName.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append("<p>  Список всех зарегистрированных пользователей ПК:<br>");
        
        for (String userFolderFile : timeName) {
            String[] strings = userFolderFile.split(" ");
            stringBuilder.append(strings[1])
                .append(" ")
                .append(new Date(Long.parseLong(strings[0])))
                .append("<br>");
        }
        
        try {
            new PCUserNameResolver.DatabaseWriter().recToDB(pcName + ConstantsFor.DOMAIN_EATMEATRU, timesUserLast.split(" ")[1]);
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        stringBuilder.append("\n\n<p><b>").append(informationFactory.getInfoAbout(pcName));
        
        if (isFullInfo) {
            return stringBuilder.toString();
        }
        else {
            return MessageFormat
                .format("Крайнее имя пользователя на ПК {1} - {2}<br>( {0} )", new Date(Long.parseLong(timesUserLast.split(" ")[0])), pcName, timesUserLast
                    .split(" ")[1]);
        }
    }
    
    private @NotNull List<String> getLastUserFolderFile() {
        List<String> timeName = new ArrayList<>();
        if (!this.pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            this.pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        String pathName = "\\\\" + pcName + "\\c$\\Users\\";
        File filesAsFile = new File(pathName);
        File[] usersDirectory = filesAsFile.listFiles();
        for (File file : Objects.requireNonNull(usersDirectory, MessageFormat.format("No files found! Pc Name: {0}, folder: {1}", pcName, pathName))) {
            if (!file.getName().toLowerCase().contains("temp") &&
                !file.getName().toLowerCase().contains("default") &&
                !file.getName().toLowerCase().contains("public") &&
                !file.getName().toLowerCase().contains("all") &&
                !file.getName().toLowerCase().contains("все") &&
                !file.getName().toLowerCase().contains("desktop")) {
                timeName.add(file.lastModified() + " " + file.getName());
            }
        }
        Collections.sort(timeName);
        return timeName;
    }
    
    private static class DatabaseWriter {
        
        
        private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", PCUserNameResolver.DatabaseWriter.class.getSimpleName() + "[\n", "\n]")
                .toString();
        }
        
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
        
        private void recToDB(String userName, String pcName) {
            MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
            String sql = "insert into pcuser (pcName, userName) values(?,?)";
            String msg = userName + " on pc " + pcName + " is set.";
            try (Connection connection = new AppComponents().connection(ConstantsNet.DB_NAME);
                 PreparedStatement p = connection.prepareStatement(sql)
            ) {
                p.setString(1, userName);
                p.setString(2, pcName);
                int executeUpdate = p.executeUpdate();
                messageToUser.info(msg + " executeUpdate=" + executeUpdate);
                ConstantsNet.getPcUMap().put(pcName, msg);
            }
            catch (SQLException ignore) {
                //nah
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
                PCUserNameResolver.DatabaseWriter.recAutoDB(pcName, lastUsersDirFileUsedName);
                return lastUsersDirFileUsedName;
            }
            pcNameFile.deleteOnExit();
            return MessageFormat.format("{0} exists {1}", pcNameFile.toPath().toAbsolutePath().normalize(), pcNameFile.exists());
        }
        
        private String getLastTimeUse(String pathAsStr) {
            Thread.currentThread().setName(this.getClass().getSimpleName());
    
            PCUserNameResolver.WalkerToUserFolder walkerToUserFolder = new PCUserNameResolver.WalkerToUserFolder();
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
         @return {@link #timePath}
         */
        @Contract(pure = true)
        private List<String> getTimePath() {
            return timePath;
        }
        
    }
}
