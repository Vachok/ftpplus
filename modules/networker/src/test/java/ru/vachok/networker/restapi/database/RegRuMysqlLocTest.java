// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.*;
import java.time.LocalDateTime;


/**
 @see RegRuMysqlLoc
 @since 14.07.2019 (12:34) */
public class RegRuMysqlLocTest {
    
    
    private DataConnectTo dataConTo;
    
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
        this.dataConTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection connection = dataConTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_TESTING);
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
            System.out.println(dataConTo.toString());
        }catch (ExceptionInInitializerError e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource source = dataConTo.getDataSource();
        Assert.assertEquals("jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_testing", source.getURL());
    }
}