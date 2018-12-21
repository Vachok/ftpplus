package ru.vachok.networker.fileworks;


import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 Удаление временных файлов.

 @since 19.12.2018 (11:05) */
class DeleterTemp extends FileSystemWorker implements Runnable {

    /**
     Запись лога в файл {@code DeleterTemp.class.getSimpleName() + "_log.txt"}.
     */
    private static PrintWriter printWriter;

    static {
        try (OutputStream outputStream = new FileOutputStream(DeleterTemp.class.getSimpleName() + "_log.txt")) {
            printWriter = new PrintWriter(outputStream, true);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     Счётчик файлов
     */
    private int filesCounter = 0;

    @Override
    public void run() {
        LOGGER.info("DeleterTemp.run");
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Thread.currentThread().setName("DeleterTemp.visitFile");
        this.filesCounter = filesCounter + 1;
        String fileAbs = file.toAbsolutePath().toString() + " DELETED";
        if (more2MBOld(attrs)) {
            Files.setAttribute(file, "dos:archive", true);
            printWriter.println(file.toAbsolutePath()
                + ","
                + (float) file.toFile().length() / ConstantsFor.MBYTE + ""
                + ","
                + new Date(attrs.lastAccessTime().toMillis()) +
                "," +
                Files.readAttributes(file, "dos:*"));
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

    /**
     Проверка файлика на "временность".

     @param filePath {@link Path} до файла
     @return удалять / не удалять
     */
    private boolean tempFile(Path filePath) {
        return filePath.toString().toLowerCase().contains(".eatmeat.ru") ||
            filePath.toString().toLowerCase().contains(".log") ||
            filePath.toString().toLowerCase().contains(".obj") ||
            filePath.toString().toLowerCase().contains(".after") ||
            filePath.toString().toLowerCase().contains(".before") ||
            filePath.toString().toLowerCase().contains(".test") ||
            filePath.toString().toLowerCase().contains("putty.exe") ||
            filePath.toString().toLowerCase().contains(".me") ||
            filePath.toString().toLowerCase().contains(".csv") ||
            filePath.toString().toLowerCase().contains(".prn");
    }

}
