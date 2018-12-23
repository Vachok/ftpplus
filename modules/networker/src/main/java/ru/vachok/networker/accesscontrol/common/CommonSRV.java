package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.Collections;

/**
 /common сервис
 <p>

 @since 05.12.2018 (9:07) */
@Service("common")
public class CommonSRV {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

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

    String searchByPat() {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            String[] toSearch = searchPat.split("\\Q:\\E");
            String searchInCommon = FileSystemWorker.searchInCommon(toSearch);
            stringBuilder.append(searchInCommon);
        } catch (Exception e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }

    void setNullToAllFields() {
        this.delFolderPath = "";
        this.perionDays = "";
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
        } catch (ArrayIndexOutOfBoundsException e) {
            followInt = 1;
        }
        stringBuilder
            .append(followInt)
            .append(" кол-во вложений папок для просмотра\n");
        try {
            String msg = followInt + " number of followed links" + "\n" + toString();
            LOGGER.warn(msg);
            Thread.sleep(1000);
            Files.walkFileTree(restoreFromArchives.getArchiveDir(), Collections.singleton(FileVisitOption.FOLLOW_LINKS), followInt + 1, restoreFromArchives);
        } catch (IOException e) {
            return e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stringBuilder.append(restoreFromArchives.toString());
        writeResult(stringBuilder.toString());
        return restoreFromArchives.toString();
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
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        String msg = file.getAbsolutePath() + " written";
        LOGGER.info(msg);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonSRV{");
        sb.append("delFolderPath='").append(delFolderPath).append('\'');
        sb.append(", perionDays='").append(perionDays).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
