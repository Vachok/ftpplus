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
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;


/**
 @see CommonSRVTest
 @since 05.12.2018 (9:07) */
@Service(ModelAttributeNames.COMMON)
public class CommonSRV {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSRV.class.getSimpleName());
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, CommonSRV.class.getSimpleName());
    
    private FileSearcher fileSearcher = new FileSearcher();
    
    /**
     Пользовательский ввод через форму на сайте
     
     @see CommonCTRL
     */
    @NonNull private String pathToRestoreAsStr = "\\\\srv-fs.eatmeat.ru\\common_new\\14_ИТ_служба\\Общая\\";
    
    private String perionDays = "365";
    
    private @NotNull String searchPat = ":";
    
    private int dirLevel;
    
    /**
     @return {@link #pathToRestoreAsStr}
     */
    @SuppressWarnings("WeakerAccess")
    public String getPathToRestoreAsStr() {
        return pathToRestoreAsStr;
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
    
    @SuppressWarnings("WeakerAccess")
    public String getSearchPat() {
        return searchPat;
    }
    
    public void setSearchPat(@NotNull String searchPat) {
        this.searchPat = searchPat;
    }
    
    /**
     common.html форма
     <p>
     
     @param pathToRestoreAsStr {@link #pathToRestoreAsStr}
     */
    public void setPathToRestoreAsStr(String pathToRestoreAsStr) {
        this.pathToRestoreAsStr = pathToRestoreAsStr;
    }
    
    public void setPerionDays(String perionDays) {
        this.perionDays = perionDays;
    }
    
    String searchByPat(@NotNull String searchPatParam) {
        this.searchPat = searchPatParam.toLowerCase();
        StringBuilder stringBuilder = new StringBuilder();
        if (searchPat.equals(":")) {
            stringBuilder.append(fileSearcher.getSearchResultsFromDB());
        }
        else if (searchPat.equalsIgnoreCase("::")) {
            stringBuilder.append(FileSearcher.dropTables());
        }
        else if (searchPat.contains("acl:")) {
            this.searchPat = searchPat.replace("acl:".toLowerCase(), "").replaceFirst(" ", "").trim();
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
    
    private String getACLs() {
        UserACLManager aclParser = UserACLManager.getInstance(UserACLManager.ACL_PARSING, Paths.get("."));
        List<String> searchPatterns = new ArrayList<>();
        if (searchPat.contains(", ")) {
            searchPatterns.addAll(Arrays.asList(searchPat.split(", ")));
        }
        else {
            searchPatterns.add(searchPat);
        }
        aclParser.setClassOption(searchPatterns);
        return aclParser.getResult();
    }
    
    /**
     Поиск в \\srv-fs\common_new
     <p>
     
     @param patternAndFolder [0] - поисковый паттерн, [1] - папка, откуда начать искать
     @return список файлов или {@link Exception}
     
     @see FileSearcher
     */
    private @NotNull String searchInCommon(@NotNull String[] patternAndFolder) {
        String folderToSearch;
        try {
            folderToSearch = patternAndFolder[1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            folderToSearch = "\\\\srv-fs.eatmeat.ru\\common_new\\";
        }
        if (!folderToSearch.contains("\\\\srv-fs.eatmeat.ru\\common_new\\")) {
            folderToSearch = "\\\\srv-fs.eatmeat.ru\\common_new\\" + folderToSearch;
        }
        this.fileSearcher = new FileSearcher(patternAndFolder[0], Paths.get(folderToSearch));
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> fileSearcherRes = fileSearcher.call();
        boolean isWrite = FileSystemWorker.writeFile(FileNames.SEARCH_LAST, fileSearcherRes.stream());
        if (isWrite) {
            stringBuilder.append(new File(FileNames.SEARCH_LAST).getAbsolutePath()).append(" written: ").append(true);
        }
        stringBuilder.append(getLastSearchResultFromFile());
        return stringBuilder.toString();
    }
    
    private static @NotNull String getLastSearchResultFromFile() {
        StringBuilder stringBuilder = new StringBuilder();
        File lastSearchFile = new File(FileNames.SEARCH_LAST);
        if (lastSearchFile.exists()) {
            stringBuilder.append(FileSystemWorker.readFile(lastSearchFile.getAbsolutePath()));
        }
        stringBuilder.trimToSize();
        if (stringBuilder.capacity() == 0) {
            stringBuilder.append("No previous searches found ...");
        }
        return stringBuilder.toString();
    }
    
    void setNullToAllFields() {
        this.pathToRestoreAsStr = "";
        this.perionDays = "";
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
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", CommonSRV.class.getSimpleName() + "[\n", "\n]")
            .add("pathToRestoreAsStr = '" + pathToRestoreAsStr + "'")
            .add("perionDays = '" + perionDays + "'")
            .add("searchPat = '" + searchPat + "'")
            .add("dirLevel = " + dirLevel)
            .toString();
    }
}
