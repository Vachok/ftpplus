package ru.vachok.networker.componentsrepo.fileworks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see FileSearcher
 @since 02.07.2019 (14:08) */
public class FileSearcherTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private FileSearcher fileSearcher;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @BeforeMethod
    public void initSearcher() {
        this.fileSearcher = new FileSearcher(ConstantsFor.OWNER, Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\"));

    }

    @Test
    public void makeSearchByName() {
        try {
            FileSearcherTest.FileSearcherWalker searcherWalker = new FileSearcherTest.FileSearcherWalker("name", ".txt");
            Files.walkFileTree(Paths.get("."), searcherWalker);
            List<Path> foundedPaths = searcherWalker.getFoundedPaths();
            Assert.assertTrue(foundedPaths.size() > 0);
            System.out.println(searcherWalker.getFileCounter() + " searched. Found (" + foundedPaths.size() + " files):\n" + AbstractForms.fromArray(foundedPaths));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void makeSearchByTime() {
        FileSearcherTest.FileSearcherWalker fileSearcherWalker = new FileSearcherTest.FileSearcherWalker("time", "02062019");
        try {
            Files.walkFileTree(Paths.get("."), fileSearcherWalker);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        System.out.println(AbstractForms.fromArray(fileSearcherWalker.getFoundedPaths()));
    }

    @Test
    public void testCall() {
        Future<Set<String>> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(fileSearcher);
        try {
            Set<String> resSet = submit.get(45, TimeUnit.SECONDS);
            String setRes = AbstractForms.fromArray(resSet);
            Assert.assertTrue(setRes.contains("Searching for: owner"), setRes);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }

    @Test
    public void testToString() {
        String s = fileSearcher.toString();
        Assert.assertEquals(s, "0 nothing...");
    }

    /**
     @see FileSearcher#getSearchResultsFromDB()
     */
    @Test
    public void testGetSearchResultsFromDB() {
        String fromDB = new FileSearcher().getSearchResultsFromDB();
        Assert.assertNotNull(fromDB);
        Assert.assertFalse(fromDB.isEmpty());
    }

    @Test
    public void testDropSearchTables() {
        List<String> tablesToDrop = FileSearcher.getSearchTablesToDrop();
        Assert.assertTrue(tablesToDrop.size() > 0);
        for (String tableName : tablesToDrop) {
            Assert.assertTrue(tableName.matches("^(s)+\\d{13}"));
        }
    }

    @Test
    public void testDropTables() {
        String dropTables = fileSearcher.dropTables();
        System.out.println("dropTables = " + dropTables);
    }

    private static class FileSearcherWalker extends SimpleFileVisitor<Path> {


        static final String CONDITION_TIME = "time";

        static final String CONDITION_NAME = "name";

        private final List<Path> foundedPaths = new ArrayList<>();

        private String searchCondition;

        private String searchPattern;

        private int fileCounter;

        FileSearcherWalker(String searchCondition, String searchPattern) {
            this.searchCondition = searchCondition;
            this.searchPattern = searchPattern;
        }

        int getFileCounter() {
            return fileCounter;
        }

        List<Path> getFoundedPaths() {
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
                Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
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