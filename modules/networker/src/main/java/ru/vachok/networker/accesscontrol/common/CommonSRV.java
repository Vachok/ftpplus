// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSearcher;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 /common сервис
 <p>
 
 @since 05.12.2018 (9:07) */
@Service(ConstantsFor.ATT_COMMON)
public class CommonSRV {
    
    
    /**
     {@link AppComponents#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSRV.class.getSimpleName());
    
    /**
     Пользовательский ввод через форму на сайте
     
     @see CommonCTRL
     */
    @NonNull private String pathToRestoreAsStr;
    
    private String perionDays;
    
    @Nullable
    private String searchPat;
    
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
    @SuppressWarnings("WeakerAccess") public String getPerionDays() {
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
        if (searchPat == null) {
            this.searchPat = ":";
        }
        if (searchPat.equals(":")) {
            stringBuilder.append(getLastSearchResultFromFile());
        }
        String[] toSearch = new String[2];
        try {
            toSearch = searchPat.split("\\Q:\\E");
            String searchInCommon = searchInCommon(toSearch);
            stringBuilder.append(searchInCommon);
        }
        catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    /**
     @return {@link RestoreFromArchives#toString()}
     */
    String reStoreDir() {
        StringBuilder stringBuilder = new StringBuilder();
        RestoreFromArchives restoreFromArchives = null;
        try {
            restoreFromArchives = new RestoreFromArchives(pathToRestoreAsStr, perionDays);
        }
        catch (InvocationTargetException | ArrayIndexOutOfBoundsException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, true));
        }
        stringBuilder
            .append("User inputs: ")
            .append(pathToRestoreAsStr)
            .append("\n");
        int followInt;
        try {
            String[] foldersInPath = pathToRestoreAsStr.split("\\Q\\\\E");
            followInt = foldersInPath.length;
        }
        catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            followInt = 1;
            stringBuilder.append(e.getMessage()).append("\n");
        }
        stringBuilder
            .append(followInt)
            .append(" кол-во вложений папок для просмотра\n");
        try {
            String msg = followInt + " number of followed links" + "\n" + this;
            LOGGER.warn(msg);
            Thread.sleep(1000);
            Files.walkFileTree(restoreFromArchives.getArchiveDir(), Collections.singleton(FileVisitOption.FOLLOW_LINKS), followInt + 1, restoreFromArchives);
        }
        catch (IOException e) {
            return e.getMessage();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stringBuilder.append(restoreFromArchives);
        writeResult(stringBuilder.toString());
        return restoreFromArchives.toString();
    }
    
    void setNullToAllFields() {
        this.pathToRestoreAsStr = "";
        this.perionDays = "";
    }
    
    /**
     Поиск в \\srv-fs\common_new
     <p>
     
     @param patternAndFolder [0] - поисковый паттерн, [1] - папка, откуда начать искать
     @return список файлов или {@link Exception}
     
     @see FileSearcher
     */
    private static String searchInCommon(String[] patternAndFolder) {
        FileSearcher fileSearcher = new FileSearcher(patternAndFolder[0]);
        String folderToSearch = "";
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
            FileSystemWorker.writeFile(ConstantsFor.FILE_PREFIX_SEARCH_ + LocalTime.now().toSecondOfDay() + ".res", fileSearcherResList.stream());
        }
        return resTo;
    }
    
    private static String getLastSearchResultFromFile() {
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : Objects.requireNonNull(new File(".").listFiles(), "No Files in root...")) {
            if (file.getName().toLowerCase().contains(ConstantsFor.FILE_PREFIX_SEARCH_)) {
                stringBuilder.append(FileSystemWorker.readFile(file.getAbsolutePath()));
            }
        }
        stringBuilder.trimToSize();
        if (stringBuilder.capacity() == 0) {
            stringBuilder.append("No previous searches found ...");
        }
        return stringBuilder.toString();
    }
    
    /**
     Записывает файл с именем {@code CommonSRV.reStoreDir.results.txt}
     <p>
     
     @param resultToFile результат работы {@link RestoreFromArchives}
     */
    private void writeResult(String resultToFile) {
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
