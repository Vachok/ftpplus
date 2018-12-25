package ru.vachok.networker.fileworks;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 Операции копирования
 <p>

 @since 19.12.2018 (10:27) */
public class SysConsoleToFile extends FileSystemWorker implements Runnable {

    @Override
    public void run() {
        LOGGER.info("SysConsoleToFile.run");
        writeSysOut();
    }

    private boolean writeSysOut() {
        try (OutputStream outputStream = new FileOutputStream("con_" + System.currentTimeMillis() + ".log");
             PrintStream printStream = new PrintStream(outputStream, true)) {
            System.setOut(printStream);
            printStream.println();
            return true;
        } catch (IOException e) {
            LOGGER.info(e.getMessage(), e);
            return false;
        }
    }
}
