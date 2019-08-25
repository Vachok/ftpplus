// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.usermanagement.UserACLManager;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSearcher;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;


/**
 /common сервис
 <p>
 
 @since 05.12.2018 (9:07) */
@Service(ModelAttributeNames.COMMON)
public class CommonSRV {
    
    
    private static final String FILE_PREFIX_SEARCH_ = "search_";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSRV.class.getSimpleName());
    
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonSRV{");
        sb.append("pathToRestoreAsStr='").append(pathToRestoreAsStr).append('\'');
        sb.append(", perionDays='").append(perionDays).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    String searchByPat(String searchPatParam) {
        this.searchPat = searchPatParam;
        StringBuilder stringBuilder = new StringBuilder();
        if (searchPat.equals(":")) {
            stringBuilder.append(getLastSearchResultFromFile());
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
        int followInt;
        try {
            Path pathToRestore = Paths.get(pathToRestoreAsStr).toAbsolutePath().normalize();
            followInt = pathToRestore.toAbsolutePath().normalize().getNameCount();
        }
        catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            followInt = 1;
            stringBuilder.append(e.getMessage()).append("\n");
        }
        List<?> restoreCall = restoreFromArchives.call();
        Set<String> filesSet = new TreeSet<>();
        restoreCall.stream().forEach(listElement->parseElement(listElement, filesSet));
        writeResult(stringBuilder.toString());
        return new TForms().fromArray(filesSet, false);
    }
    
    void setNullToAllFields() {
        this.pathToRestoreAsStr = "";
        this.perionDays = "";
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
            Files.walkFileTree(Paths.get(folderToSearch), fileSearcher);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        List<String> fileSearcherResList = fileSearcher.getResList();
        fileSearcherResList.add("Searched: " + new Date() + "\n");
        String resTo = new TForms().fromArray(fileSearcherResList, true);
        if (fileSearcherResList.size() > 0) {
            FileSystemWorker.writeFile(FILE_PREFIX_SEARCH_ + LocalTime.now().toSecondOfDay() + ".res", fileSearcherResList.stream());
        }
        return resTo;
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
    
    private void writeResult(@NotNull String resultToFile) {
        File file = new File(getClass().getSimpleName() + ".reStoreDir.results.txt");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(resultToFile.toLowerCase().getBytes());
        }
        catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        String msg = file.getAbsolutePath() + ConstantsFor.STR_WRITTEN;
        LOGGER.info(msg);
    }
    
}