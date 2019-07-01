package ru.vachok.networker.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 @see ru.vachok.networker.fileworks.AdminFuryTest
 @since 01.07.2019 (13:46) */
public class AdminFury implements Runnable {
    
    
    private final String walkFileTreeStartDir = "\\\\srv-fs\\Common_new\\06_Маркетинг\\";
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    
    @Override public void run() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    private void stepOne() {
        try {
            AdminFury.DirSizeCounter dirSizeCounter = new AdminFury.DirSizeCounter();
            Files.walkFileTree(Paths.get(walkFileTreeStartDir), dirSizeCounter);
            List<Path> trashFilesPaths = dirSizeCounter.getTrashFilesPaths();
            for (Path filesPath : trashFilesPaths) {
                checkAndCopy(filesPath);
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    private void checkAndCopy(Path filesPath) throws IOException {
        Path targetDir = Paths.get("\\\\srv-mail3\\c$\\ProgramData\\Microsoft\\Windows\\trash");
        if (!targetDir.toFile().exists()) {
            createTrashDir(targetDir);
        }
        
        String replace = filesPath.toAbsolutePath().normalize().toString().replace(walkFileTreeStartDir, System.getProperty("file.separator"));
        Path target = Paths.get(targetDir + replace);
        
        Files.createDirectories(target.getParent());
        Files.copy(filesPath, target);
        
    }
    
    private void createTrashDir(Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        Files.setAttribute(targetDir, "dos:hidden", true);
    }
    
    private class DirSizeCounter extends SimpleFileVisitor<Path> {
        
        
        long dirSize;
        
        private List<Path> trashFilesPaths = new ArrayList<>();
        
        public List<Path> getTrashFilesPaths() {
            return trashFilesPaths;
        }
        
        public long getDirSize() {
            return dirSize;
        }
        
        @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (dirSize < ((45 * ConstantsFor.GBYTE) + (490 * ConstantsFor.MBYTE)) && attrs.isRegularFile()) {
                this.dirSize += file.toFile().length();
                trashFilesPaths.add(file);
                return FileVisitResult.CONTINUE;
            }
            else {
                return FileVisitResult.TERMINATE;
            }
        }
        
        @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            System.out.println("file = " + dir + " (" + dirSize / ConstantsFor.GBYTE + ")");
            return FileVisitResult.CONTINUE;
        }
    }
    
}
