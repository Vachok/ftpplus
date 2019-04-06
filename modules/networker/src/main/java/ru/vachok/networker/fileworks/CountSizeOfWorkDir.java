package ru.vachok.networker.fileworks;



import ru.vachok.networker.ConstantsFor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.fileworks.CountSizeOfWorkDir
 <p>

 @since 06.04.2019 (13:15) */
public class CountSizeOfWorkDir extends FileSystemWorker implements Callable<String> {

    private long sizeBytes = 0L;

    private Map<Long, String> longPathMap = new TreeMap<>();


    @Override public String call() throws Exception {
        return getSizeOfDir();
    }


    @Override public FileVisitResult preVisitDirectory(Path dir , BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }


    @Override public FileVisitResult visitFile(Path file , BasicFileAttributes attrs) throws IOException {
        if(file.toFile().length() <= 0) {
            try{
                Files.deleteIfExists(file);
            }catch(Exception e){
                file.toFile().deleteOnExit();
            }
        }
        if(attrs.isRegularFile()) {
            this.sizeBytes = sizeBytes + file.toFile().length();
            if(attrs.lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)) {
                longPathMap.putIfAbsent(file.toFile().length() ,
                    file.toAbsolutePath().toString() + "<b> 3 days old...</b>");
            }
            else { longPathMap.putIfAbsent(file.toFile().length() , file.toAbsolutePath().toString()); }
        }
        return FileVisitResult.CONTINUE;
    }


    @Override public FileVisitResult visitFileFailed(Path file , IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }


    @Override public FileVisitResult postVisitDirectory(Path dir , IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }


    private String getSizeOfDir() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Files.walkFileTree(Paths.get(".") , this);
        stringBuilder.append("Total size = ").append(sizeBytes / ConstantsFor.KBYTE / ConstantsFor.KBYTE).append(" MB<br>\n");
        longPathMap.forEach((x , y) -> stringBuilder.append(String.format("%.02f" , (float) x / ConstantsFor.KBYTE)).append(" kb in: ").append(y).append("<br>\n"));
        return stringBuilder.toString();
    }
}