// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.networker.componentsrepo.FilePropsLocal;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.InitProperties;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;


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
    
    @Test(enabled = false)
    public void tryingDel() {
    
        boolean rowsDel = dbPropsCallable.delProps();
        Assert.assertTrue(rowsDel);
        boolean isPRFileReadOnly = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES).setReadOnly();
        Assert.assertTrue(isPRFileReadOnly);
        System.out.println(true + " file readonly!");
        System.out.println("rowsDel = " + rowsDel);
    }
    
    @Test(enabled = false)
    public void tryUpd() {
        Properties toSet = new Properties();
        toSet.setProperty("test", LocalDateTime.now().toString());
        boolean orNot = dbPropsCallable.setProps(toSet);
        Assert.assertTrue(orNot);
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
        retProps.putAll(dbPropsCallable.getProps());
        retProps.setProperty("loadedFromFile", ConstantsFor.STR_FALSE);
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
        InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
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