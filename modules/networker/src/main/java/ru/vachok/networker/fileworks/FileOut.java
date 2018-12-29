package ru.vachok.networker.fileworks;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 Запись в файл

 @since 27.12.2018 (13:27) */
public class FileOut extends FileSystemWorker implements Runnable {

    private String fileToWrite;

    private boolean createNewFile;

    private byte[] bytesToWrite;

    public FileOut(String fileToWrite, byte[] bytesToWrite, boolean createNewFile) {
        this.fileToWrite = fileToWrite;
        this.createNewFile = createNewFile;
        this.bytesToWrite = bytesToWrite;
        try {
            Files.deleteIfExists(Paths.get(fileToWrite));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public FileOut(String fileToWrite, byte[] bytesToWrite) {
        this.fileToWrite = fileToWrite;
        this.createNewFile = false;
        this.bytesToWrite = ("\n" + new String(bytesToWrite)).getBytes();
    }


    @Override
    public void run() {
        StandardOpenOption standardOpenOption = StandardOpenOption.APPEND;
        if (createNewFile || !new File(fileToWrite).exists()) {
            standardOpenOption = StandardOpenOption.CREATE_NEW;
        }
        writeNew(standardOpenOption);
    }

    private void writeNew(StandardOpenOption standardOpenOption) {
        Path path = Paths.get(fileToWrite);
        try (OutputStream outputStream = path.getFileSystem().provider().newOutputStream(path, standardOpenOption);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
            bufferedOutputStream.write(bytesToWrite, 0, bytesToWrite.length);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
