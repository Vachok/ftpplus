package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 Очистка папки \\192.168.14.10\IT-Backup\SRV-FS\Archives

 @see ru.vachok.networker.SystemTrayHelper
 @since 15.11.2018 (14:09) */
public class ArchivesAutoCleaner implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Первоначальная папка
     */
    private static final String SRV_FS_ARCHIVES = "\\\\192.168.14.10\\IT-Backup\\SRV-FS\\Archives";

    /**
     Год
     */
    private int yearStop;

    private List<Path> dirList = new ArrayList<>();

    /**
     @param yearStop год, за который почистить
     */
    public ArchivesAutoCleaner(int yearStop) {
        this.yearStop = yearStop;
    }

    private ArchivesAutoCleaner(int yearStop, List<Path> filesLevel3) {
        this.dirList = filesLevel3;
        this.yearStop = yearStop;
    }

    /**
     Запуск 1. {@link #starterClean(List)} 1.1 {@link #checkOld(Object)} 1.2 {@link #checkOld(Object)} 1.3 {@link #deleteFilesOlder(List)} 1.3.1 {@link #checkOld(Object)} 1.3.2 {@link
    #starterClean(List)} 1.3.2.1 {@link #checkOld(Object)} 1.3.2.2 {@link #checkOld(Object)}
     */
    @Override
    public void run() {
        LOGGER.warn("ArchivesAutoCleaner.run");
        Path path = Paths.get(SRV_FS_ARCHIVES);
        dirList.add(path);
        String msg = "ArchivesAutoCleaner.run" + "\n" + "Year is " + yearStop;
        LOGGER.warn(msg);
        Thread.currentThread().setName(yearStop + " THREAD");
        starterClean(dirList);
    }

    /**
     Удаляет, если файл ( {@link #checkOld(Object)} ) и пишет новый лист папок.

     @param filesLevel2 следующий уровень {@link Path}
     */
    private void deleteFilesOlder(List<Path> filesLevel2) {
        LOGGER.warn("ArchivesAutoCleaner.deleteFilesOlder");
        List<Path> filesLevel3 = new ArrayList<>();
        filesLevel2.iterator().forEachRemaining(f -> {
            if (f.toFile().isFile()) {
                checkOld(f);
            } else {
                try (DirectoryStream<Path> p = Files.newDirectoryStream(f)) {
                    p.iterator().forEachRemaining(e -> {
                        File[] files = e.toFile().listFiles();
                        try {
                            if (files.length > 0) Files.delete(e);
                        } catch (NullPointerException | IOException e1) {
                            LOGGER.warn(e1.getMessage());
                        }
                        boolean add = filesLevel3.add(e);
                        String msg = "Adding: " + e.toAbsolutePath() + " " + add;
                        LOGGER.info(msg);
                    });
                } catch (IOException | NullPointerException e) {
                    restartThr(filesLevel3);
                }
            }
        });
        starterClean(filesLevel3);
    }

    /**
     Создаёт {@link DirectoryStream} <br> Для каждого элемента проводит проверку, {@code if (isFile())} - добавляет в новый {@link List} {@link Path} <br> если ложь или {@link IOException}, {@link
    NullPointerException} - выводит сообщение лога и {@link #checkOld(Object)}
     <p>
     В конце запускает {@link #deleteFilesOlder(List)} , с отобранным из {@link DirectoryStream} листом папок.

     @param dirPathList {@link List} элементов {@link Path}, для начала сканирования.
     */
    private void starterClean(List<Path> dirPathList) {
        dirPathList.iterator().forEachRemaining(x -> {
            File[] files = x.toFile().listFiles();
            try {
                for (File file : files) {
                    if (file.isDirectory()) dirParse(files);
                    else {
                        checkOld(file.getAbsolutePath());
                    }
                }
                dirPathList.remove(x);
            } catch (NullPointerException e) {
                restartThr(dirPathList);
            }
        });

    }

    private void delDir(Path x) {
        try {
            Files.delete(x);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    private void restartThr(List<Path> filesLevel3) {
        ThreadConfig threadConfig = new ThreadConfig();
        Thread.currentThread().interrupt();
        threadConfig.threadPoolTaskExecutor().execute(new ArchivesAutoCleaner(yearStop, filesLevel3));
    }

    private void dirParse(File[] files) {
        List<Path> filesLevel2 = new ArrayList<>();
        String msg1 = files.length + " size files ID 120";
        LOGGER.info(msg1);
        if (files.length > 0) {
            for (File f : files) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(f.toPath())) {
                    directoryStream.iterator().forEachRemaining(xD -> {
                        boolean add = filesLevel2.add(xD);
                        String msg = "Add stream: " + xD + "| " + add;
                        LOGGER.info(msg);
                    });
                } catch (IOException | NullPointerException e) {
                    checkOld(f);
                }
            }
        }
        deleteFilesOlder(filesLevel2);
    }

    /**
     Удаляет файл, если в имени есть паттерн: <br> " " + {@link #yearStop}+ "-"

     @param f {@link Path} or {@link File}
     */
    private void checkOld(Object f) {
        String filePath = f.toString();
        File file = new File(filePath);
        if (file.getName().contains(" " + yearStop + "-")) {
            try {
                String msg = file.getAbsolutePath() + " deleted" + " by " + "checkOld " + yearStop;
                Files.delete(Paths.get(file.getAbsolutePath()));
                LOGGER.warn(msg);
            } catch (IOException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
