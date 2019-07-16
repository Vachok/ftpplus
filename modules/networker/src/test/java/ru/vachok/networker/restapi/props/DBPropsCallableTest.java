// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 @see DBPropsCallable
 @since 16.07.2019 (1:06) */
@SuppressWarnings("ALL")
public class DBPropsCallableTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final InitProperties dbPropsCallable = new DBPropsCallable();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    @AfterMethod
    public void checkRealDB() {
        DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
        try (Connection c = dataConnectTo.getDataSource().getConnection();
             PreparedStatement p = c.prepareStatement("select * from ru_vachok_networker");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                if (r.isLast()) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
                    Date timeset = dateFormat.parse(r.getString("timeset"));
                    Assert.assertTrue(timeset.getTime() > (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)));
                }
            }
        }
        catch (SQLException | ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    @Test
    public void realGet() {
        Properties props = dbPropsCallable.getProps();
        Assert.assertTrue(props.size() > 9);
    }
    
    @Test
    public void realGetWhenFileReadOnly() {
        dbPropsCallable.getProps();
    }
    
    @Test
    public void testGetRegSourceForProperties() {
    }
    
    @Test
    public void testGetProps() {
    }
    
    @Test
    public void testSetProps() {
    }
    
    @Test
    public void testCall() {
    
    }
    
    @Test
    public void testDelProps() {
    }
    
    @Test
    public void testToString1() {
        System.out.println(dbPropsCallable.toString());
    }
}