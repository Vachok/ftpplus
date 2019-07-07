package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 @see AdminFury
 @since 01.07.2019 (13:46) */
@SuppressWarnings("ALL") public class AdminFuryTest {
    
    
    @Test(enabled = false)
    public void run() {
        stepOne();
    }
    
    private void stepOne() {
        try {
            AdminFuryTest.DirSizeCounter dirSizeCounter = new AdminFuryTest.DirSizeCounter();
            Files.walkFileTree(Paths.get("\\\\srv-fs\\Common_new\\06_Маркетинг\\"), dirSizeCounter);
            List<Path> trashFilesPaths = dirSizeCounter.getTrashFilesPaths();
            for (Path filesPath : trashFilesPaths) {
                Path targetDir = Paths.get("\\\\srv-mail3\\c$\\ProgramData\\Microsoft\\Windows\\trash");
                if (!targetDir.toFile().exists()) {
                    Files.createDirectories(targetDir);
                }
                String replace = filesPath.toAbsolutePath().normalize().toString().replace("\\\\srv-fs\\Common_new\\06_Маркетинг\\", System.getProperty("file.separator"));
                Path target = Paths.get(targetDir + replace);
                Files.createDirectories(target.getParent());
                Files.copy(filesPath, target);
                Files.setAttribute(targetDir, "dos:hidden", true);
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
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