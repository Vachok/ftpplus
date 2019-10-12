package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


/**
 Class ru.vachok.networker.componentsrepo.fileworks.BiggestFileInPathFinder
 <p>
 
 @since 29.09.2019 (15:31) */
public class BiggestFileInPathFinder extends FileSystemWorker {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, BiggestFileInPathFinder.class.getSimpleName());
    
    private Path inThePath;
    
    private Map<String, Long> filesWithSize = new ConcurrentHashMap<>();
    
    public BiggestFileInPathFinder(Path inThePath) {
        this.inThePath = inThePath;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            filesWithSize.put(file.toAbsolutePath().toString(), attrs.size() / ConstantsFor.KBYTE);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String packFiles(List<File> filesToZip, String zipName) {
        throw new TODOException("ru.vachok.networker.componentsrepo.fileworks.BiggestFileInPathFinder.packFiles( String ) at 29.09.2019 - (15:31)");
    }
    
    @Override
    public String findBiggestFile(Path inThePath) {
        this.inThePath = inThePath;
        return findBiggestFile();
    }
    
    public String findBiggestFile() {
        try {
            Files.walkFileTree(inThePath, this);
            Set<Map.Entry<String, Long>> entriesSet = filesWithSize.entrySet();
            AtomicLong sizeMax = new AtomicLong();
            AtomicReference<String> fileName = new AtomicReference<>();
            entriesSet.forEach((longEntry)->entryParse(sizeMax, longEntry, fileName));
            Long maxFile = filesWithSize.get(fileName.get());
            return MessageFormat.format("{0} file is {1} kbytes", fileName.get(), sizeMax);
        }
        catch (IOException e) {
            return e.getMessage();
        }
    }
    
    private void entryParse(@NotNull AtomicLong sizeMax, @NotNull Map.Entry<String, Long> longEntry, AtomicReference<String> fileName) {
        long maxLoc = Math.max(sizeMax.get(), longEntry.getValue());
        if (maxLoc > sizeMax.get()) {
            fileName.set(longEntry.getKey());
            sizeMax.set(maxLoc);
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", BiggestFileInPathFinder.class.getSimpleName() + "[\n", "\n]")
            .add("inThePath = " + inThePath)
            .toString();
    }
}