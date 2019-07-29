package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.accesscontrol.common.OwnerFixerTest
 @since 29.07.2019 (13:23) */
public class OwnerFixer extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private List<String> resultsList = new ArrayList<>();
    
    private Path startPath;
    
    public OwnerFixer(Path startPath) {
        this.startPath = startPath;
    }
    
    @Override
    public void run() {
        resultsList.add("Starting: " + new Date() + " ; " + toString());
        try {
            Files.walkFileTree(startPath, this);
            FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".res", resultsList);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("OwnerFixer.run: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            checkRights(dir);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OwnerFixer{");
        sb.append(", resultsList=").append(resultsList.size());
        sb.append(", startPath=").append(startPath);
        sb.append('}');
        return sb.toString();
    }
    
    private void checkRights(Path dir) throws IOException {
        UserPrincipal owner = Files.getOwner(dir);
        if (!owner.toString().contains("BUILTIN\\Администраторы")) {
            setParentOwner(dir, owner);
            
        }
    }
    
    private void setParentOwner(Path dir, @NotNull UserPrincipal userPrincipal) {
        try {
            Path pathSetOwner = Files.setOwner(dir, Files.getOwner(dir.getRoot()));
            resultsList.add(pathSetOwner + " owner: " + Files.getOwner(dir));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.setParentOwner: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
}
