package ru.vachok.networker.accesscontrol.common;


import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 @since 31.07.2019 (9:59) */
public class CheckReplaceNeed implements Runnable {
    
    
    private static final Path PATH_TO_MONITOR = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger");
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private List<WatchEvent<?>> watchEvents = new ArrayList<>();
    
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        startMon();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CheckReplaceNeed{");
        sb.append('}');
        return sb.toString();
    }
    
    private void startMon() {
        try (FileSystem fileSystem = PATH_TO_MONITOR.getFileSystem()) {
            try (WatchService watchService = fileSystem.newWatchService()) {
                WatchKey watchKey = PATH_TO_MONITOR.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                watchEvents.addAll(watchKey.pollEvents());
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CheckReplaceNeed.startMon: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
}
