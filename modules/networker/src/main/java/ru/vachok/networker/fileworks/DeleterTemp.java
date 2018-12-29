package ru.vachok.networker.fileworks;


import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 Удаление временных файлов.

 @since 19.12.2018 (11:05) */
class DeleterTemp extends FileSystemWorker implements Runnable {

    /**
     Запись лога в файл {@code DeleterTemp.class.getSimpleName() + "_log.txt"}.
     */
    private static PrintWriter printWriter;

    /**
     Счётчик файлов
     */
    private int filesCounter = 0;

    private static final List<String> FROM_FILE = new ArrayList<>();

    static {
        try (OutputStream outputStream = new FileOutputStream(DeleterTemp.class.getSimpleName() + "_log.txt")) {
            printWriter = new PrintWriter(outputStream, true);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        getList();
    }

    @Override
    public void run() {
        LOGGER.info("DeleterTemp.run");
    }

    private static void getList() {
        try (InputStream inputStream = DeleterTemp.class.getResourceAsStream("/BOOT-INF/classes/static/config/temp_pat.cfg");
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                FROM_FILE.add(bufferedReader.readLine());
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>

     @param attrs {@link BasicFileAttributes}
     @return <b>true</b> = lastAccessTime - ONE_YEAR and size bigger MBYTE*2
     */
    private boolean more2MBOld(BasicFileAttributes attrs) {
        return attrs
            .lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR) &&
            attrs
                .size() > ConstantsFor.MBYTE * 2;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Thread.currentThread().setName("DeleterTemp.visitFile");
        this.filesCounter = filesCounter + 1;
        String fileAbs = new StringBuilder()
            .append(file.toAbsolutePath().toString())
            .append(ConstantsFor.DELETED).toString();
        if (more2MBOld(attrs)) {
            Files.setAttribute(file, ConstantsFor.DOS_ARCHIVE, true);
            printWriter.println(new StringBuilder()
                .append(file.toAbsolutePath())
                .append(",")
                .append(( float ) file.toFile().length() / ConstantsFor.MBYTE)
                .append(",")
                .append(new Date(attrs.lastAccessTime().toMillis()))
                .append(",")
                .append(Files.readAttributes(file, "dos:*")).toString());
        }
        if (tempFile(file.toAbsolutePath())) {
            try {
                Files.deleteIfExists(file);
            } catch (FileSystemException e) {
                file.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }
            LOGGER.warn(fileAbs);
        }

        return FileVisitResult.CONTINUE;
    }

    /**
     Проверка файлика на "временность".
     <p>
     ClassPath - /BOOT-INF/classes/static/config/temp_pat.cfg <br> .\resources\static\config\temp_pat.cfg

     @param filePath {@link Path} до файла
     @return удалять / не удалять
     */
    private boolean tempFile(Path filePath) {
        return FROM_FILE.stream().anyMatch(sP -> filePath.toString().toLowerCase().contains(sP));
    }
}
