// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 @see ru.vachok.networker.ad.pc.PCOnTest
 @since 31.01.2019 (0:20) */
class PCOn extends PCInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOn.class.getSimpleName());
    
    private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
    
    private static final Pattern USERS = Pattern.compile("Users");
    
    private static DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private String lastUsersDirFileUsedName;
    
    private @NotNull String sql;
    
    private String pcName;
    
    public PCOn(@NotNull String pcName) {
        try {
            this.pcName = InetAddress.getByAddress(InetAddress.getByName(pcName).getAddress()).getHostName();
        }
        catch (UnknownHostException e) {
            this.pcName = pcName;
        }
        this.sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCOn.class.getSimpleName() + "[\n", "\n]")
                .add("sql = '" + sql + "'")
                .add("pcName = '" + pcName + "'")
                .toString();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "PC is not set " + this.toString();
        }
        else {
            return getInfoAbout(pcName);
        }
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        ThreadConfig.thrNameSet(pcName.substring(0, 5));
        StringBuilder stringBuilder = new StringBuilder();
    
        String dbInfoStr = new DBPCInfo(pcName).defaultInformation();
        System.out.println("dbInfoStr = " + dbInfoStr);
        String strHTMLLink = pcNameWithHTMLLink(pcName);
        
        stringBuilder.append(strHTMLLink);
        stringBuilder.append(lastUserResolved());
        return stringBuilder.toString();
    }
    
    @Override
    public String fillWebModel() {
        System.out.println();
        String namesToFile = new PCOn.WalkerToUserFolder().writeNamesToTMPFile();
        System.out.println(namesToFile);
        System.out.println();
        File file = new File("err");
        try {
            String fourSlash = "\\\\";
            file = new File(fourSlash + pcName + "\\c$\\users\\" + namesToFile.split(" ")[0]);
        }
        catch (IndexOutOfBoundsException ignore) {
            //
        }
        return file.getAbsolutePath();
    }
    
    private @NotNull String lastUserResolved() {
        StringBuilder stringBuilder = new StringBuilder();
        
        final String sqlLoc = "SELECT * FROM `pcuser` WHERE `pcName` LIKE ?";
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement p = connection.prepareStatement(sqlLoc)) {
            p.setString(1, new StringBuilder().append("%").append(pcName).append("%").toString());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    if (r.last()) {
                        stringBuilder.append(r.getString(ConstantsFor.DB_FIELD_USER));
                    }
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage());
        }
        return new PageGenerationHelper().setColor("white", stringBuilder.toString());
    }
    
    private @NotNull String pcNameWithHTMLLink(@NotNull String pcName) {
        String lastUser = lastUserResolved();
        
        StringBuilder builder = new StringBuilder();
        builder.append("<br><b>");
        builder.append(new PageGenerationHelper().getAsLink("/ad?" + pcName.split(".eatm")[0], pcName));
        builder.append(lastUser);
        builder.append("</b>    ");
        builder.append(new DBPCInfo(pcName).countOnOff());
        builder.append(". ");
        
        String printStr = builder.toString();
        String pcOnline = "online is true<br>";
        
        NetKeeper.getNetworkPCs().put(printStr, true);
    
        messageToUser.info(pcName, pcOnline, new DBPCInfo(pcName).userNameFromDBWhenPCIsOff());
        
        int onlinePC = Integer.parseInt((LOCAL_PROPS.getProperty(PropertiesNames.PR_ONLINEPC, "0")));
        onlinePC += 1;
        
        LOCAL_PROPS.setProperty(PropertiesNames.PR_ONLINEPC, String.valueOf(onlinePC));
        return builder.toString();
    }
    
    @Override
    public String fillAttribute(String attributeName) {
        this.pcName = attributeName;
        return getHTMLCurrentUserName();
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
        if (pcName.contains(ConstantsFor.ERROR_DOUBLE_DOMAIN)) {
            pcName = pcName.replace(ConstantsFor.ERROR_DOUBLE_DOMAIN, ConstantsFor.DOMAIN_EATMEATRU);
        }
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        long date = System.currentTimeMillis();
        try {
            date = Long.parseLong(timesUserLast.split(" ")[0]);
        }
        catch (NumberFormatException ignore) {
            //23.08.2019 (17:25)
        }
        String format = "Крайнее имя пользователя на ПК " + pcName + " - " + timesUserLast.split(" ")[1] + "<br>( " + new Date(date) + " )";
        return format + stringBuilder.toString();
        
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
        if (usersDirectory == null || usersDirectory.length < 1) {
            timeName.add(MessageFormat.format("No User for {0} resolved!", pcName));
            return timeName;
        }
        for (File file : usersDirectory) {
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
    
    /**
     Поиск файлов в папках {@code c-users}.
 
     @see #walkUsersFolderIfPCOnline(String)
     @since 22.11.2018 (14:46)
     */
    public class WalkerToUserFolder extends SimpleFileVisitor<Path> {
        
        
        /**
         new {@link ArrayList}, список файлов, с отметками {@link File#lastModified()}
         
         @see #visitFile(Path, BasicFileAttributes)
         */
        private final List<String> timePath = new ArrayList<>();
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            timePath.add(file.toFile().lastModified() + " " + file + " " + new Date(file.toFile().lastModified()) + " " + file.toFile().lastModified());
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
        
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
    
        private String writeNamesToTMPFile() {
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
                    lastUsersDirFileUsedName = USERS.split(walkUsersFolderIfPCOnline(pathAsStr))[1];
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
                PCInfo.saveAutoresolvedUserToDB(pcName, lastUsersDirFileUsedName);
                return lastUsersDirFileUsedName;
            }
            pcNameFile.deleteOnExit();
            return MessageFormat.format("{0} exists {1}", pcNameFile.toPath().toAbsolutePath().normalize(), pcNameFile.exists());
        }
    
        private String walkUsersFolderIfPCOnline(String pathAsStr) {
            
            try {
                if (NetScanService.isReach(pcName)) {
                    Files.walkFileTree(Paths.get(pathAsStr), Collections.singleton(FOLLOW_LINKS), 2, this);
                }
                List<String> timePath = this.getTimePath();
                Collections.sort(timePath);
                return timePath.get(timePath.size() - 1);
            }
            catch (IOException | IndexOutOfBoundsException e) {
                return e.getMessage() + " " + getClass().getSimpleName() + ".getLastTimeUse";
            }
        }
        
        @Contract(pure = true)
        private List<String> getTimePath() {
            return timePath;
        }
        
    }
}