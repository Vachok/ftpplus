package ru.vachok.networker.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.services.MessageLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 Операции копирования
 <p>

 @since 19.12.2018 (10:27) */
public class SysConsoleToFile extends FileSystemWorker implements Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(SysConsoleToFile.class.getSimpleName());

    private boolean writeSysOut() {
        try (OutputStream outputStream = new FileOutputStream("con_" + System.currentTimeMillis() + ".log");
             PrintStream printStream = new PrintStream(outputStream, true)) {
            System.setOut(printStream);
            printStream.println();
            return true;
        } catch (IOException e) {
            messageToUser.error(SysConsoleToFile.class.getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
            return false;
        }
    }
    
    @Override
    public void run() {
        messageToUser.info("SysConsoleToFile.run");
        writeSysOut();
    }
}
