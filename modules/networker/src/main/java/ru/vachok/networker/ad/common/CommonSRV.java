// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.usermanagement.UserACLManager;
import ru.vachok.networker.componentsrepo.fileworks.FileSearcher;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;

import static ru.vachok.networker.data.enums.FileNames.FILE_PREFIX_SEARCH_;


/**
 @see CommonSRVTest
 @since 05.12.2018 (9:07) */
@Service(ModelAttributeNames.COMMON)
public class CommonSRV {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSRV.class.getSimpleName());
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, CommonSRV.class.getSimpleName());
    
    /**
     Пользовательский ввод через форму на сайте
     
     @see CommonCTRL
     */
    @NonNull private String pathToRestoreAsStr;
    
    private String perionDays;
    
    private @NotNull String searchPat = ":";
    
    private UserACLManager aclParser = UserACLManager.getI(UserACLManager.ACL_PARSING, Paths.get("."));
    
    private int dirLevel;
    
    /**
     @return {@link #pathToRestoreAsStr}
     */
    @SuppressWarnings("WeakerAccess")
    public String getPathToRestoreAsStr() {
        return pathToRestoreAsStr;
    }
    
    /**
     common.html форма
     <p>
 
     @param pathToRestoreAsStr {@link #pathToRestoreAsStr}
     */
    public void setPathToRestoreAsStr(String pathToRestoreAsStr) {
        this.pathToRestoreAsStr = pathToRestoreAsStr;
    }
    
    @SuppressWarnings("WeakerAccess")
    public String getSearchPat() {
        return searchPat;
    }
    
    public void setSearchPat(String searchPat) {
        this.searchPat = searchPat;
    }
    
    /**
     <b>MUST BE PUBLIC</b>
     <p>
 
     @return кол-во дней, за которое выполнять поиск.
     */
    @SuppressWarnings("WeakerAccess")
    public String getPerionDays() {
        return perionDays;
    }
    
    public void setPerionDays(String perionDays) {
        this.perionDays = perionDays;
    }
    
    String searchByPat(String searchPatParam) {
        this.searchPat = searchPatParam;
        StringBuilder stringBuilder = new StringBuilder();
        if (searchPat.equals(":")) {
            stringBuilder.append(getLastSearchResultFromDB());
        }
        else if (searchPat.contains("acl:")) {
            this.searchPat = searchPat.replace("acl:", "").replaceFirst(" ", "").trim();
            stringBuilder.append(getACLs());
        }
        else {
            try {
                String[] toSearch = searchPat.split("\\Q:\\E");
                String searchInCommon = searchInCommon(toSearch);
                stringBuilder.append(searchInCommon);
            }
            catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
                stringBuilder.append(e.getMessage());
            }
        }
        return stringBuilder.toString();
    }
    
    private static @NotNull String getLastSearchResultFromFile() {
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : Objects.requireNonNull(new File(".").listFiles(), "No Files in root...")) {
            if (file.getName().toLowerCase().contains(FILE_PREFIX_SEARCH_)) {
                stringBuilder.append(FileSystemWorker.readFile(file.getAbsolutePath()));
            }
        }
        stringBuilder.trimToSize();
        if (stringBuilder.capacity() == 0) {
            stringBuilder.append("No previous searches found ...");
        }
        return stringBuilder.toString();
    }
    
    protected static @NotNull String getLastSearchResultFromDB() {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection(ConstantsFor.DB_SEARCH)) {
            DatabaseMetaData connectionMetaData = connection.getMetaData();
            try (ResultSet rs = connectionMetaData.getTables(ConstantsFor.DB_SEARCH, "", "%", null)) {
                while (rs.next()) {
                    tableNames.add(rs.getString(3));
                }
                Collections.sort(tableNames);
                Collections.reverse(tableNames);
            }
            for (String tblName : tableNames) {
                String sql = String.format("select * from %s", tblName);
                stringBuilder.append(new Date(Long.parseLong(tblName.replace("s", "")))).append(":\n");
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                     ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        stringBuilder.append(resultSet.getString(3)).append("\n");
                    }
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(MessageFormat.format("CommonSRV.getLastSearchResultFromDB: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return stringBuilder.toString();
    }
    
    private String getACLs() {
        
        List<String> searchPatterns = new ArrayList<>();
        if (searchPat.contains(" ")) {
            searchPatterns.addAll(Arrays.asList(searchPat.split(", ")));
        }
        else {
            searchPatterns.add(searchPat);
        }
        aclParser.setClassOption(searchPatterns);
        return aclParser.getResult();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", CommonSRV.class.getSimpleName() + "[\n", "\n]")
            .add("pathToRestoreAsStr = '" + pathToRestoreAsStr + "'")
            .add("perionDays = '" + perionDays + "'")
            .add("searchPat = '" + searchPat + "'")
            .add("aclParser = " + aclParser)
            .add("dirLevel = " + dirLevel)
            .toString();
    }
    
    String reStoreDir() {
        if (pathToRestoreAsStr == null) {
            pathToRestoreAsStr = "\\\\srv-fs.eatmeat.ru\\it$$\\";
        }
        if (perionDays == null) {
            this.perionDays = "1";
        }
        StringBuilder stringBuilder = new StringBuilder();
        FileRestorer restoreFromArchives = null;
        try {
            restoreFromArchives = new FileRestorer(pathToRestoreAsStr, perionDays);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, true));
        }
        stringBuilder
            .append("User inputs: ")
            .append(pathToRestoreAsStr)
            .append("\n");
        List<?> restoreCall = callToRestore(restoreFromArchives);
        Set<String> filesSet = new TreeSet<>();
        restoreCall.stream().forEach(listElement->parseElement(listElement, filesSet));
        return writeResult(stringBuilder.toString());
    }
    
    private List<?> callToRestore(FileRestorer restoreFromArchives) {
        Future<List<?>> submit = AppComponents.threadConfig().getTaskExecutor().submit(restoreFromArchives);
        List<?> retList = new ArrayList<>();
        try {
            retList = submit.get((long) ConstantsFor.ONE_HOUR_IN_MIN, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            messageToUser.error(e.getMessage() + " see line: 179 ***");
        }
        return retList;
    }
    
    void setNullToAllFields() {
        this.pathToRestoreAsStr = "";
        this.perionDays = "";
    }
    
    private void parseElement(Object listElement, Set<String> filesSet) {
        if (listElement instanceof String) {
            filesSet.add(listElement + "\n");
        }
        if (listElement instanceof Path) {
            filesSet.add("00 " + listElement + "\n");
            if (((Path) listElement).toFile().isDirectory()) {
                dirLevel++;
                showDir(((Path) listElement).toFile().listFiles(), filesSet);
            }
        }
    }
    
    private void showDir(@NotNull File[] listElement, Set<String> filesSet) {
        for (File file : listElement) {
            if (file.isDirectory()) {
                dirLevel++;
                showDir(Objects.requireNonNull(file.listFiles()), filesSet);
            }
            else {
                filesSet.add(dirLevelGetVisual() + " " + (file.getAbsolutePath()) + ("\n"));
            }
        }
        dirLevel--;
    }
    
    private @NotNull String dirLevelGetVisual() {
        StringBuilder stringBuilder = new StringBuilder();
        String format = String.format("%02d", dirLevel);
        stringBuilder.append(format);
        for (int i = 0; i < dirLevel; i++) {
            stringBuilder.append(">");
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String writeResult(@NotNull String resultToFile) {
        File file = new File(getClass().getSimpleName() + ".reStoreDir.results.txt");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(resultToFile.toLowerCase().getBytes());
        }
        catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        String msg = file.getAbsolutePath() + ConstantsFor.STR_WRITTEN;
        LOGGER.info(msg);
        return msg;
    }
    
    /**
     Поиск в \\srv-fs\common_new
     <p>
     
     @param patternAndFolder [0] - поисковый паттерн, [1] - папка, откуда начать искать
     @return список файлов или {@link Exception}
     
     @see FileSearcher
     */
    private static String searchInCommon(@NotNull String[] patternAndFolder) {
        FileSearcher fileSearcher = new FileSearcher(patternAndFolder[0]);
        String folderToSearch;
        try {
            folderToSearch = patternAndFolder[1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            folderToSearch = "";
        }
        folderToSearch = "\\\\srv-fs.eatmeat.ru\\common_new\\" + folderToSearch;
        try {
            Path startPath = Paths.get(folderToSearch);
            Files.walkFileTree(startPath, fileSearcher);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 276");
        }
        Set<String> fileSearcherRes = fileSearcher.getResSet();
        fileSearcherRes.add("Searched: " + new Date() + "\n");
        return new TForms().fromArray(fileSearcherRes, true);
    }
}
