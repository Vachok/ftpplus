// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.restapi.DataConnectTo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see RegRuMysqlLoc
 @since 14.07.2019 (12:34) */
public class RegRuMysqlLocTest {
    
    
    private DataConnectTo dataConTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
    
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
    public void testToString1() {
        
        try{
            System.out.println(dataConTo.toString());
        }catch (ExceptionInInitializerError e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private void setPassPref() {
        Preferences pref = AppComponents.getUserPref();
        pref.put(PropertiesNames.PR_DBUSER, DataConnectTo.DBUSER_KUDR);
        pref.put(PropertiesNames.PR_DBPASS, "36e42yoak8");
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}