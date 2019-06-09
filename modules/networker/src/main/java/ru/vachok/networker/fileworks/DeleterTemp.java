// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 Удаление временных файлов.

 @since 19.12.2018 (11:05) */
@SuppressWarnings("ClassWithoutmessageToUser")
class DeleterTemp extends FileSystemWorker implements Runnable {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(DeleterTemp.class.getSimpleName());
    
    private PrintWriter printWriter;
    
    private List<WatchEvent<?>> eventList;
    
    public List<WatchEvent<?>> getEventList() {
        return eventList;
    }

    /**
     Счётчик файлов
     */
    private int filesCounter;
    
    private String patToDel;

    private List<String> fromFile = new ArrayList<>();
    
    {
        try {
            OutputStream outputStream = new FileOutputStream(DeleterTemp.class.getSimpleName() + ".txt");
            printWriter = new PrintWriter(outputStream, true);
        }
        catch (FileNotFoundException e) {
            messageToUser.error(e.getMessage());
        }
    }

    DeleterTemp(String patToDel) {
        this.patToDel = patToDel;
        this.fromFile.add(patToDel);
    }

    DeleterTemp() {
    }
    
    @Override
    public void run() {
        printWriter.println(new Date() + " " + getClass().getSimpleName() + " is start.");
        getList();
        Thread.currentThread().setName("deleter");
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        AppComponents.threadConfig().thrNameSet(file.toFile().getName().substring(0, 2));
        this.filesCounter += 1;
        String fileAbs = new StringBuilder().append(filesCounter).append(") ")
            .append(file.toFile().getName())
            .append(ConstantsFor.STR_DELETED).toString();
        if (moreInfo(attrs)) {
            printWriter.println(new StringBuilder()
                .append(file.toAbsolutePath())
                .append(",")
                .append(new Date(attrs.lastAccessTime().toMillis())));
        }
        if(tempFile(file.toAbsolutePath())){
            try{
                Files.deleteIfExists(file);
                printWriter.println(fileAbs);
            }
            catch(FileSystemException e){
                file.toFile().deleteOnExit();
                fileAbs = filesCounter + ") " + file.toFile().getName() + " must be deleted on exit";
                printWriter.println(fileAbs);
                return FileVisitResult.CONTINUE;
            }
            messageToUser.warn(fileAbs);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        WatchEvent.Kind<Path> createEvent = StandardWatchEventKinds.ENTRY_CREATE;
        try (WatchService watchService = dir.getFileSystem().newWatchService()) {
            WatchKey createEntKey = dir.register(watchService, createEvent);
            eventList = createEntKey.pollEvents();
        }
        return FileVisitResult.CONTINUE;
    }
    
    @SuppressWarnings("InjectedReferences") private void getList() {
        if (patToDel != null) {
            fromFile.add(patToDel);
        }
        else {
            try (InputStream inputStream = DeleterTemp.class.getResourceAsStream("/BOOT-INF/classes/static/config/temp_pat.cfg");
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)
            ) {
                while (reader.ready()) {
                    fromFile.add(bufferedReader.readLine());
                }
                FileStore fileStore = Files.getFileStore(Paths.get(""));
                messageToUser.info(getClass().getSimpleName() + ".getList", fileStore.name(), " = " + fileStore.type());
            }
            catch (IOException e) {
                messageToUser.warn(new TForms().fromArray(e, false));
            }
        }
        printWriter.println(new TForms().fromArray(fromFile, false));
    }

    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>

     @param attrs {@link BasicFileAttributes}
     @return <b>true</b> = lastAccessTime - ONE_YEAR and size bigger MBYTE*2
     */
    private boolean moreInfo(BasicFileAttributes attrs) {
        boolean retBool = false;
        if (attrs.isRegularFile() && attrs.size() <= 0) {
            retBool = true;
        }
        else if (attrs.isDirectory()) {
            retBool = false;
        }
        return retBool;
    }

    /**
     Проверка файлика на "временность".
     <p>
     ClassPath - /BOOT-INF/classes/static/config/temp_pat.cfg <br> .\resources\static\config\temp_pat.cfg

     @param filePath {@link Path} до файла
     @return удалять / не удалять
     */
    private boolean tempFile(Path filePath) {
        return fromFile.stream().anyMatch(sP -> filePath.toString().toLowerCase().contains(sP));
    }
}
