package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 Очистка папки \\192.168.14.10\IT-Backup\SRV-FS\Archives

 @since 15.11.2018 (14:09) */
public class ArchivesAutoCleaner implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    private final String pathToClean = "\\\\192.168.14.10\\IT-Backup\\SRV-FS\\Archives";

    private int yearStop;

    public ArchivesAutoCleaner(int yearStop) {
        this.yearStop = yearStop;
    }

    public static void main(String[] args) {
        Runnable r = new ArchivesAutoCleaner(2015);
        r.run();
    }

    @Override
    public void run() {
        List<Path> dirList = new ArrayList<>();
        Path path = Paths.get(pathToClean);
        dirList.add(path);
        starterClean(dirList);
    }

    private void starterClean(List<Path> dirPathList) {
        List<Path> filesLevel2 = new ArrayList<>();
        dirPathList.forEach(x -> {
            if (x.toFile().isDirectory()) {
                File[] files = x.toFile().listFiles();
                for (File f : Objects.requireNonNull(files)) {
                    if (f.isDirectory()) {
                        try (DirectoryStream directoryStream = Files.newDirectoryStream(f.toPath())) {
                            directoryStream.iterator().forEachRemaining(xD -> {
                                    filesLevel2.add((Path) xD);
                                    LOGGER.info(f.getAbsolutePath() + " is " + filesLevel2.size() + " files or dirs");
                                }
                            );
                        } catch (IOException | NullPointerException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });
        deleteFilesOldier(filesLevel2);
    }

    private void deleteFilesOldier(List<Path> filesLevel2) {
        List<Path> filesLevel3 = new ArrayList<>();
        filesLevel2.forEach(x -> {
            if (x.toFile().isFile()) {
                if (x.toFile().getName().toLowerCase().contains(" " + yearStop + "-")) {
                    try {
                        String toDel = x.toString();
                        Files.delete(x);
                        LOGGER.warn(toDel);
                    } catch (IOException ignore) {
                        //
                    }
                }
            } else if (x.toFile().isDirectory()) {
                try (DirectoryStream<Path> p = Files.newDirectoryStream(x);
                     OutputStream outputStream = new FileOutputStream("savepoint.ini")) {
                    p.iterator().forEachRemaining(filesLevel3::add);
                    outputStream.write(x.toAbsolutePath().toString().getBytes());
                } catch (IOException | NullPointerException ignore) {
                    //
                }

            } else LOGGER.warn(x.toString() + " ********************************");
        });
        starterClean(filesLevel3);
    }
}
