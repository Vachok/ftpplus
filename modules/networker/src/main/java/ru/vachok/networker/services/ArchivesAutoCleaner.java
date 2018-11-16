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

    /**
     * Строка лога
     */
    private String clnStr = yearStop + " year ." + "Cleaning: ";

    /**
     @param yearStop год, за который почистить
     */
    public ArchivesAutoCleaner(int yearStop) {
        this.yearStop = yearStop;
    }

    /**Запуск
     1. {@link #starterClean(List)}
     1.1 {@link #checkOld(Object)}
     1.2 {@link #checkOld(Object)}
     1.3 {@link #deleteFilesOlder(List)}
     1.3.1 {@link #checkOld(Object)}
     1.3.2 {@link #starterClean(List)}
     1.3.2.1 {@link #checkOld(Object)}
     1.3.2.2 {@link #checkOld(Object)}
     */
    @Override
    public void run() {
        LOGGER.warn("ArchivesAutoCleaner.run");
        List<Path> dirList = new ArrayList<>();
        Path path = Paths.get(SRV_FS_ARCHIVES);
        dirList.add(path);
        String msg = "ArchivesAutoCleaner.run" + "\n" + "Year is " + yearStop;
        LOGGER.warn(msg);
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
                boolean isOlder = f.toFile().getName().toLowerCase().contains(" " + yearStop + "-");
                if (isOlder) {
                    try {
                        String toDel = f.toString() + " DELETED ; " + getClass().getMethod("deleteFilesOlder", List.class);
                        Files.delete(f);
                        LOGGER.warn(toDel);
                    } catch (IOException | NullPointerException | NoSuchMethodException e) {
                        checkOld(f);
                    }
                }
            } else if (f.toFile().isDirectory()) {
                try (DirectoryStream<Path> p = Files.newDirectoryStream(f);
                     OutputStream outputStream = new FileOutputStream("savepoint.ini")) {
                    p.iterator().forEachRemaining(filesLevel3::add);
                    outputStream.write(f.toAbsolutePath().toString().getBytes());
                } catch (IOException | NullPointerException ignore) {
                    //
                }

            } else {
                String msg = f.toString() + " ********************************";
                LOGGER.warn(msg);
            }
        });
        starterClean(filesLevel3);
    }

    /**Создаёт {@link DirectoryStream} <br>
     Для каждого элемента проводит проверку, {@code if (isFile())} - добавляет в новый {@link List} {@link Path} <br>
     если ложь или {@link IOException}, {@link NullPointerException} - выводит сообщение лога и {@link #checkOld(Object)}
     <p>
     В конце запускает {@link #deleteFilesOlder(List)} , с отобранным из {@link DirectoryStream} листом папок.
     @param dirPathList {@link List} элементов {@link Path}, для начала сканирования.
     */
    private void starterClean(List<Path> dirPathList) {
        LOGGER.warn("ArchivesAutoCleaner.starterClean");

        List<Path> filesLevel2 = new ArrayList<>();
        dirPathList.iterator().forEachRemaining(x -> {
            if (x.toFile().isDirectory()) {
                File[] files = x.toFile().listFiles();
                if (files != null) {
                    for (File f : files) {
                        try (DirectoryStream directoryStream = Files.newDirectoryStream(f.toPath())) {
                            directoryStream.iterator().forEachRemaining(xD -> filesLevel2.add((Path) xD));
                        } catch (IOException | NullPointerException e) {
                            checkOld(x);
                        }
                    }
                }
            } else {
                checkOld(x);
            }
        });
        deleteFilesOlder(filesLevel2);
    }

    /**
     Удаляет файл, если в имени есть паттерн: <br>
     " " + {@link #yearStop}+ "-"
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
