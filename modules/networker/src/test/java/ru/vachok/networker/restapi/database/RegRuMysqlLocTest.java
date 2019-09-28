// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;


/**
 @see RegRuMysqlLoc
 @since 14.07.2019 (12:34) */
public class RegRuMysqlLocTest {
    
    
    private RegRuMysqlLoc regRuLocal;
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void initDcT() {
        this.regRuLocal = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection connection = regRuLocal.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_TESTING);
             PreparedStatement p = connection.prepareStatement("INSERT INTO `u0466446_testing`.`fake` (`Rec`) VALUES (?)")) {
            p.setString(1, LocalDateTime.now().toString());
            Assert.assertTrue(p.executeUpdate() > 0);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }catch (InvokeEmptyMethodException e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString() {
        
        try{
            System.out.println(regRuLocal.toString());
        }catch (ExceptionInInitializerError e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource source = regRuLocal.getDataSource();
        Assert.assertEquals("jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_testing", source.getURL());
    }
    
    @Test
    public void testDropTable() {
        try {
            boolean isDropped = regRuLocal.dropTable("test.test");
            Assert.assertTrue(isDropped);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testUploadCollection() {
        try {
            regRuLocal.uploadCollection(FileSystemWorker.readFileToList("build.gradle"), "test.test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}