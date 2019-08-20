// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.info.HTMLInfo;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.LocalPCInfo;
import ru.vachok.networker.info.PCInfo;
import ru.vachok.networker.net.NetScanService;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 @see ru.vachok.networker.ad.PCUserNameHTMLResolverTest
 @since 02.10.2018 (17:32) */
public class PCUserNameHTMLResolver extends PCInfo implements HTMLInfo {
    
    
    private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
    
    private static final Pattern USERS = Pattern.compile("Users");
    
    private String lastUsersDirFileUsedName;
    
    private String pcName;
    
    private InformationFactory informationFactory;
    
    @Contract(pure = true)
    public PCUserNameHTMLResolver(InformationFactory informationFactory) {
        this.informationFactory = informationFactory;
    }
    
    public PCUserNameHTMLResolver(String aboutWhat) {
        this.pcName = aboutWhat;
        this.informationFactory = InformationFactory.getInstance(InformationFactory.LOCAL);
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        this.informationFactory = getLocalInfo(aboutWhat);
        return getHTMLCurrentUserName() + "<br>";
    }
    
    private @NotNull String getHTMLCurrentUserName() {
        List<String> timeName = getLastUserFolderFile();
        String timesUserLast = timeName.get(timeName.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append("<p>  Список всех зарегистрированных пользователей ПК:<br>");
        
        for (String userFolderFile : timeName) {
            String[] strings = userFolderFile.split(" ");
            stringBuilder.append(strings[1])
                .append(" ");
            try {
                stringBuilder.append(new Date(Long.parseLong(strings[0])));
            }
            catch (NumberFormatException e) {
                stringBuilder.append("offline");
            }
            stringBuilder.append("<br>");
        }
        
        try {
            LocalPCInfo.recToDB(pcName + ConstantsFor.DOMAIN_EATMEATRU, timesUserLast.split(" ")[1]);
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        stringBuilder.append("\n\n<p><b>").append(informationFactory.getInfoAbout(pcName));
        long date = System.currentTimeMillis();
        try {
            date = Long.parseLong(timesUserLast.split(" ")[0]);
        }
        catch (NumberFormatException ignore) {
        
        }
        String format = "Крайнее имя пользователя на ПК " + pcName + " - " + timesUserLast.split(" ")[1] + "<br>( " + new Date(date) + " )";
        return format + stringBuilder.toString();
        
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }
    
    @Override
    public String getInfo() {
        this.informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
        return informationFactory.getInfoAbout(pcName);
    }
    
    @Override
    public String fillAttribute(String samAccountName) {
        this.pcName = samAccountName;
        this.informationFactory = getLocalInfo(samAccountName);
        return getHTMLCurrentUserName();
    }
    
    @Override
    public @NotNull String fillWebModel() {
        System.out.println();
        String namesToFile = new PCUserNameHTMLResolver.WalkerToUserFolder().namesToFile();
        System.out.println(namesToFile);
        System.out.println();
        File file = new File("err");
        try {
            file = new File("\\\\" + pcName + "\\c$\\users\\" + namesToFile.split(" ")[0]);
        }
        catch (IndexOutOfBoundsException ignore) {
            //
        }
        return file.getAbsolutePath();
    }
    
    private @NotNull List<String> getLastUserFolderFile() {
        if (!this.pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            this.pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        boolean isReachPC = NetScanService.isReach(pcName);
        String pathName = "\\\\" + pcName + "\\c$\\Users\\";
        List<String> timeName = new ArrayList<>();
        if (isReachPC) {
            timeName = getTimeName(pathName);
            Collections.sort(timeName);
        }
        else {
            timeName.add(MessageFormat.format("{0} is not available", pcName));
        }
        return timeName;
    }
    
    private @NotNull List<String> getTimeName(String pathName) {
        List<String> timeName = new ArrayList<>();
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
        return timeName;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCUserNameHTMLResolver.class.getSimpleName() + "[\n", "\n]")
            .add("lastUsersDirFileUsedName = '" + lastUsersDirFileUsedName + "'")
            .add("pcName = '" + pcName + "'")
            .add("informationFactory = " + informationFactory)
            .toString();
    }
    
    @Override
    public String getUserByPCNameFromDB(String pcName) {
        return null;
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
                LocalPCInfo.recAutoDB(pcName, lastUsersDirFileUsedName);
                return lastUsersDirFileUsedName;
            }
            pcNameFile.deleteOnExit();
            return MessageFormat.format("{0} exists {1}", pcNameFile.toPath().toAbsolutePath().normalize(), pcNameFile.exists());
        }
        
        private String getLastTimeUse(String pathAsStr) {
            Thread.currentThread().setName(this.getClass().getSimpleName());
    
            PCUserNameHTMLResolver.WalkerToUserFolder walkerToUserFolder = new PCUserNameHTMLResolver.WalkerToUserFolder();
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
