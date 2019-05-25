// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Collections;


/**
 /common сервис
 <p>

 @since 05.12.2018 (9:07) */
@Service(ConstantsFor.ATT_COMMON)
public class CommonSRV {
    
    
    /**
     {@link AppComponents#getLogger(String)}
     */
    private static final Logger LOGGER = AppComponents.getLogger(CommonSRV.class.getSimpleName());
    
    /**
     Пользовательский ввод через форму на сайте
     
     @see CommonCTRL
     */
    private String delFolderPath;
    
    private String perionDays;
    
    private String searchPat;
    
    public String getPerionDays() {
        return perionDays;
    }
    
    public void setPerionDays(String perionDays) {
        this.perionDays = perionDays;
    }
    
    /**
     @return {@link #delFolderPath}
     */
    @SuppressWarnings("WeakerAccess")
    public String getDelFolderPath() {
        return delFolderPath;
    }
    
    /**
     common.html форма
     <p>
     
     @param delFolderPath {@link #delFolderPath}
     */
    public void setDelFolderPath(String delFolderPath) {
        this.delFolderPath = delFolderPath;
    }
    
    @SuppressWarnings("WeakerAccess")
    public String getSearchPat() {
        return searchPat;
    }
    
    public void setSearchPat(String searchPat) {
        this.searchPat = searchPat;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonSRV{");
        sb.append("delFolderPath='").append(delFolderPath).append('\'');
        sb.append(", perionDays='").append(perionDays).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    String searchByPat() {
        StringBuilder stringBuilder = new StringBuilder();
        if (searchPat.equals(":")) {
            stringBuilder.append(getFromFile());
        }
        String[] toSearch = new String[2];
        try {
            toSearch = searchPat.split("\\Q:\\E");
            String searchInCommon = FileSystemWorker.searchInCommon(toSearch);
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
        RestoreFromArchives restoreFromArchives = new RestoreFromArchives(delFolderPath, perionDays);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("User inputs: ")
            .append(delFolderPath)
            .append("\n");
        int followInt;
        try {
            String[] foldersInPath = delFolderPath.split("\\Q\\\\E");
            followInt = foldersInPath.length;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            followInt = 1;
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
        this.delFolderPath = "";
        this.perionDays = "";
    }
    
    private String getFromFile() {
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : new File(".").listFiles()) {
            if (file.getName().toLowerCase().contains(ConstantsOst.FILE_PREFIX_SEARCH_)) {
                stringBuilder.append(FileSystemWorker.readFile(file.getAbsolutePath()));
            }
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
            outputStream.write(resultToFile.getBytes());
        }
        catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        @SuppressWarnings("DuplicateStringLiteralInspection") String msg = file.getAbsolutePath() + ConstantsFor.STR_WRITTEN;
        LOGGER.info(msg);
    }
    
    /**
     Сборщик прав \\srv-fs.eatmeat.ru\common_new
     <p>
     {@link Files#walkFileTree(java.nio.file.Path, java.nio.file.FileVisitor)}, где {@link Path} = \\srv-fs.eatmeat.ru\common_new и {@link FileVisitor}
     = new {@link CommonRightsChecker}.
     <p>
     <b>{@link IOException}:</b><br>
     {@link MessageToUser#errorAlert(String, String, String)},
     {@link FileSystemWorker#error(String, Exception)}
     */
    public static void runCommonScan() {
        final long stMeth = System.currentTimeMillis();
        try {
            FileVisitor<Path> commonRightsChecker = new CommonRightsChecker();
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
        }
        catch (IOException e) {
            MessageToUser messageToUser = new MessageLocal(CommonSRV.class.getSimpleName());
            messageToUser.error(FileSystemWorker.error(CommonSRV.class.getSimpleName() + ".runCommonScan", e));
        }
    }
}
