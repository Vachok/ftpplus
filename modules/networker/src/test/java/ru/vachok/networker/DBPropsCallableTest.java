// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.abstr.DataBaseRegSQL;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


@SuppressWarnings("ALL") public class DBPropsCallableTest {
    
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
    
    @Test
    public void testCall() {
        DBPropsCallable dbPropsCallable = new DBPropsCallable();
        Properties calledProps = dbPropsCallable.call();
        String prAsStr = new TForms().fromArray(calledProps);
        Assert.assertNotNull(prAsStr);
    }
    
    @Test
    public void testSelectFrom() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testInsertTo() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testDeleteFrom() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testUpdateTable() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
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