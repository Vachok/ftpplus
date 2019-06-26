// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


@SuppressWarnings("ALL") public class DBPropsCallableTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    @Test(enabled = false)
    public void tryingDel() {
        DataBaseRegSQL dbPropsCallable = new DBPropsCallable();
        int rowsDel = dbPropsCallable.deleteFrom();
        Assert.assertTrue(rowsDel > 0);
        boolean isPRFileReadOnly = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES).setReadOnly();
        Assert.assertTrue(isPRFileReadOnly);
        System.out.println(true + " file readonly!");
        System.out.println("rowsDel = " + rowsDel);
    }
    
    @Test(enabled = false)
    public void tryUpd() {
        DataBaseRegSQL dbPropsCallable = new DBPropsCallable();
        int orNot = dbPropsCallable.updateTable();
        Assert.assertTrue(orNot != 0);
    }
    
    @Test(enabled = false)
    public void testCall() {
        DBPropsCallable dbPropsCallable = new DBPropsCallable();
        Properties calledProps = dbPropsCallable.call();
        String prAsStr = new TForms().fromArray(calledProps);
        Assert.assertNotNull(prAsStr);
    }
    
    @Test
    public void testSelectFrom() {
        try {
            int selectedFrom = new DBPropsCallable().selectFrom();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testInsertTo() {
        try {
            int insertTo = new DBPropsCallable().insertTo();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test(enabled = false)
    public void testDeleteFrom() {
        int deleteFromRows = new DBPropsCallable().deleteFrom();
        Assert.assertTrue(deleteFromRows > 5);
    }
    
    @Test(enabled = false)
    public void testUpdateTable() {
        int updateTable = new DBPropsCallable(new FileProps(ConstantsFor.class.getSimpleName()).getProps()).updateTable();
        Assert.assertTrue(updateTable > 5, String.valueOf(updateTable));
    }
    
    @Test(enabled = false)
    public void fileIsWritableOrNotExistsTest() {
        Properties retProps = new Properties();
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        retProps.putAll(initProperties.getProps());
        retProps.setProperty("loadedFromFile", "false");
        try {
            boolean isUp = new AppComponents().updateProps(retProps);
            Assert.assertTrue(isUp);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test(enabled = false)
    public void readOnlyFileReturnFileTest() {
        Properties retProps = new Properties();
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        retProps.putAll(initProperties.getProps());
        try {
            boolean isUp = new AppComponents().updateProps(retProps);
            Assert.assertTrue(isUp);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}