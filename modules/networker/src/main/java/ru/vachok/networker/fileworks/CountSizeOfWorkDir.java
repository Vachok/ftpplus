// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.fileworks.CountSizeOfWorkDirTest
 @since 06.04.2019 (13:15) */
public class CountSizeOfWorkDir extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private long sizeBytes;
    
    private Map<Long, String> longStrPathMap = new TreeMap<>();
    
    private String fileName;
    
    private PrintStream printStream;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    public CountSizeOfWorkDir(String fileName) {
        this.fileName = fileName;
    
        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    protected CountSizeOfWorkDir() {
    }
    
    @Override
    public String call() throws Exception {
        return getSizesOfFilesStores() + "\n\n<p>" + getSizeOfDir();
    }
    
    @Override
    public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toFile().length() <= 0 && !file.toFile().getName().equalsIgnoreCase("scan.tmp")) {
            try {
                Files.deleteIfExists(file);
            }
            catch (Exception e) {
                file.toFile().deleteOnExit();
            }
        }
        if (attrs.isRegularFile()) {
            this.sizeBytes += file.toFile().length();
            long lastAccessLong = attrs.lastAccessTime().toMillis();
            if (lastAccessLong < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)) {
                longStrPathMap.putIfAbsent(file.toFile().length(),
                    file.toAbsolutePath() + "<b> " + TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastAccessLong) + " days old...</b>");
            }
            else {
                longStrPathMap.putIfAbsent(file.toFile().length(), file.toAbsolutePath().toString());
            }
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printStream.println(file);
        printStream.println(new TForms().fromArray(exc, false));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CountSizeOfWorkDir{");
        sb.append(ConstantsFor.TOSTRING_NAME).append(fileName).append('\'');
        sb.append(", longStrPathMap=").append(longStrPathMap);
        sb.append(", sizeBytes=").append(sizeBytes);
        sb.append('}');
        return sb.toString();
    }
    
    private String getSizeOfDir() throws IOException {
        List<String> dirsSizes = new ArrayList<>();
        Files.walkFileTree(Paths.get(".").normalize(), this);
    
        dirsSizes.add("Total size = " + sizeBytes / ConstantsFor.KBYTE / ConstantsFor.KBYTE + " MB<br>\n");
    
        longStrPathMap.forEach((sizeBytes, fileName)->dirsSizes.add(fileName + ": " + String.format("%.02f", (float) sizeBytes / ConstantsFor.KBYTE) + "kb <br>\n"));
    
        Collections.sort(dirsSizes);
        return new TForms().fromArray(dirsSizes);
    }
    
    private @NotNull String getSizesOfFilesStores() {
        Path rootProgramPath = Paths.get(".").toAbsolutePath().normalize();
        StringBuilder stringBuilder = new StringBuilder();
        try (FileSystem fileSystem = rootProgramPath.getFileSystem()) {
            for (FileStore fileStore : fileSystem.getFileStores()) {
                this.messageToUser = DBMessenger.getInstance(this.getClass().getSimpleName());
                String spaces = MessageFormat.format("Store {0}. Usable = {1} Mb, unallocated = {2} Mb, total = {3} Mb",
                    fileStore.name(), fileStore.getUsableSpace() / ConstantsFor.MBYTE, fileStore.getUnallocatedSpace() / ConstantsFor.MBYTE, fileStore
                        .getTotalSpace() / ConstantsFor.MBYTE);
                stringBuilder.append(spaces);
                messageToUser.info(spaces);
                this.messageToUser = new MessageLocal(this.getClass().getSimpleName());
            }
        }
        catch (IOException | UnsupportedOperationException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return stringBuilder.toString();
    }
}