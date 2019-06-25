package ru.vachok.networker.fileworks;


import ru.vachok.networker.componentsrepo.IllegalInvokeEx;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;


public class CommonCleaner extends FileSystemWorker implements Callable<String> {
    
    
    private Map<Path, String> pathAttrMap = new ConcurrentHashMap<>();
    
    public CommonCleaner(Map<Path, String> pathAttrMap) {
        this.pathAttrMap = pathAttrMap;
    }
    
    @Override public String call() {
        if (pathAttrMap.size() == 0) {
            throw new IllegalInvokeEx("25.06.2019 (9:20). Map<Path, String> pathAttrMap");
        }
        
        return "";
    }
    
    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
