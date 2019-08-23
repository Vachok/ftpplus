package ru.vachok.networker.componentsrepo.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.*;
import java.util.ArrayList;
import java.util.List;


/**
 @see FileSearcher
 @since 02.07.2019 (14:08) */
public class FileSearcherTest {
    
    
    @Test
    public void makeSearchByName() {
        try {
            FileSearcherTest.FileSearcherWalker searcherWalker = new FileSearcherTest.FileSearcherWalker("name", ".txt");
            Files.walkFileTree(Paths.get("."), searcherWalker);
            List<Path> foundedPaths = searcherWalker.getFoundedPaths();
            Assert.assertTrue(foundedPaths.size() > 0);
            System.out.println(searcherWalker.getFileCounter() + " searched. Found (" + foundedPaths.size() + " files):\n" + new TForms().fromArray(foundedPaths, false));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void makeSearchByTime() {
        FileSearcherTest.FileSearcherWalker fileSearcherWalker = new FileSearcherTest.FileSearcherWalker("time", "02062019");
        try {
            Files.walkFileTree(Paths.get("."), fileSearcherWalker);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        System.out.println(new TForms().fromArray(fileSearcherWalker.getFoundedPaths(), false));
    }
    
    private static class FileSearcherWalker extends SimpleFileVisitor<Path> {
        
        
        public static final String CONDITION_TIME = "time";
        
        public static final String CONDITION_NAME = "name";
        
        private final List<Path> foundedPaths = new ArrayList<>();
        
        private String searchCondition;
        
        private String searchPattern;
        
        private int fileCounter;
        
        public FileSearcherWalker(String searchCondition, String searchPattern) {
            this.searchCondition = searchCondition;
            this.searchPattern = searchPattern;
        }
        
        public int getFileCounter() {
            return fileCounter;
        }
        
        public List<Path> getFoundedPaths() {
            return foundedPaths;
        }
        
        @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            
            if (searchCondition.equalsIgnoreCase(CONDITION_NAME)) {
                searchByName(file);
            }
            if (searchCondition.equalsIgnoreCase(CONDITION_TIME)) {
                searchByTime(file, attrs);
            }
            
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        private void searchByTime(Path file, BasicFileAttributes attrs) {
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
            try {
                long stampFromPattern = dateFormat.parse(searchPattern).getTime();
                if (attrs.lastModifiedTime().toMillis() < stampFromPattern) {
                    foundedPaths.add(file);
                }
            }
            catch (ParseException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
            this.fileCounter += 1;
        }
        
        private void searchByName(Path file) {
            if (file.getFileName().toString().contains(searchPattern)) {
                foundedPaths.add(file);
            }
            this.fileCounter += 1;
        }
    }
}