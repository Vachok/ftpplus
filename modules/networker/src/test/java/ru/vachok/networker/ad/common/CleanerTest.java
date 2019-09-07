// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;


/**
 @see Cleaner
 @since 25.06.2019 (10:28) */
public class CleanerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final File infoAboutOldCommon = new File(FileNames.FILENAME_OLDCOMMON);
    
    private final long epochSecondOfStart = LocalDateTime.of(2019, 6, 25, 11, 45, 00).toEpochSecond(ZoneOffset.ofHours(3));
    
    private Cleaner cleaner = new Cleaner();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    /**
     @see Cleaner#call()
     */
    @Test(enabled = false)
    public void testCall() {
        try {
            System.out.println("cleaner.call() = " + cleaner.call());
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetPathAttrMap() {
        Map<Path, String> map = cleaner.getPathAttrMap();
        String strMap = new TForms().fromArray(map);
        Assert.assertTrue(strMap.isEmpty(), strMap);
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(cleaner.toString().contains("Cleaner{"), cleaner.toString());
    }
    
    private @NotNull Map<Path, String> fillMapFromFile() {
    
        Map<Path, String> filesToDeleteWithAttrs = new HashMap<>();
        int limitOfDeleteFiles = countLimitOfDeleteFiles(infoAboutOldCommon);
        List<String> fileAsList = FileSystemWorker.readFileToList(infoAboutOldCommon.toPath().toAbsolutePath().normalize().toString());
        Random random = new Random();
        
        for (int i = 0; i < limitOfDeleteFiles; i++) {
            String deleteFileAsString = fileAsList.get(random.nextInt(fileAsList.size()));
            try {
                String[] pathAndAttrs = deleteFileAsString.split(", ,");
                filesToDeleteWithAttrs.putIfAbsent(Paths.get(pathAndAttrs[0]), pathAndAttrs[1]);
            }
            catch (IndexOutOfBoundsException | NullPointerException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
        }
        return filesToDeleteWithAttrs;
    }
    
    private int countLimitOfDeleteFiles(@NotNull File fileWithInfoAboutOldCommon) {
        int stringsInLogFile = FileSystemWorker.countStringsInFile(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize());
        long lastModified = fileWithInfoAboutOldCommon.lastModified();
        
        if (System.currentTimeMillis() < lastModified + TimeUnit.DAYS.toMillis(1)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 10);
        }
        else if (System.currentTimeMillis() < lastModified + TimeUnit.DAYS.toMillis(2)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 25);
        }
        else if (System.currentTimeMillis() < lastModified + TimeUnit.DAYS.toMillis(3)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 75);
        }
        else {
            System.out.println(stringsInLogFile);
        }
        
        return stringsInLogFile;
    }
    
    @Test
    @Ignore
    public void testDeletedRS(){
        Set<String> retSet = new ConcurrentSkipListSet<>();
        
        try(Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection(ConstantsFor.STR_VELKOM);
            PreparedStatement preparedStatement = connection.prepareStatement("select * from oldfiles");
            ResultSet resultSet= preparedStatement.executeQuery();){
            
            ResultSet preparedStatementResultSet = preparedStatement.getResultSet();
            while (preparedStatementResultSet.next()){
                int concurrency = preparedStatement.getResultSetConcurrency();
                System.out.println("resultSet = " + preparedStatementResultSet.getString(2));
                preparedStatementResultSet.deleteRow();
            }
        }catch (SQLException e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
}