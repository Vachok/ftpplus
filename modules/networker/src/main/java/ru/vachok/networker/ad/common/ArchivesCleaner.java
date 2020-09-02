package ru.vachok.networker.ad.common;


import com.google.gson.JsonObject;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


/**
 @see ArchivesCleanerTest */
public class ArchivesCleaner extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final String TAG = "ArchivesCleaner";
    
    private final long startTime = System.currentTimeMillis();
    
    private long filesSize = 0;
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() & file.toFile().lastModified() < (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(720))) {
            long fileSize = (file.toFile().length() / ConstantsFor.MBYTE);
            boolean isDel = file.toFile().delete();
            if (isDel) {
                this.filesSize = fileSize + filesSize;
            }
            else {
                file.toFile().deleteOnExit();
            }
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("dir is cleaned = " + dir);
        System.out.println(MessageFormat.format("Cleaned {0} megabytes.", filesSize));
        if (dir.toFile().isDirectory() && dir.toFile().listFiles().length <= 0) {
            dir.toFile().delete();
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public void run() {
        try {
            Files.walkFileTree(Paths.get("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives"), this);
        }
        catch (IOException e) {
            MessageToUser.getInstance(MessageToUser.EMAIL, TAG).error(e.getMessage());
        }
        finally {
            MessageToUser.getInstance(MessageToUser.EMAIL, TAG).info(toString());
        }
    }
    
    @Override
    public String toString() {
        final JsonObject jo = new JsonObject();
        jo.addProperty(ConstantsFor.JSONNAME_CLASS, TAG);
        jo.addProperty("filesSize", filesSize);
        jo.addProperty("time", System.currentTimeMillis() - startTime);
        return jo.toString();
    }
}
