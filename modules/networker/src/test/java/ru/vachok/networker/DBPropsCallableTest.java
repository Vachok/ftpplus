// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.abstr.DataBaseRegSQL;

import java.io.File;
import java.util.Properties;


public class DBPropsCallableTest {
    
    
    @Test(/*enabled = false*/)
    public void call4Props() {
        DBPropsCallable dbPropsCallable = new DBPropsCallable();
        Properties calledProps = dbPropsCallable.call();
        String prAsStr = new TForms().fromArray(calledProps);
        Assert.assertNotNull(prAsStr);
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
    
    @Test
    public void tryUpd() {
        DataBaseRegSQL dbPropsCallable = new DBPropsCallable();
        int orNot = dbPropsCallable.updateTable();
        Assert.assertTrue(orNot != 0);
    }
}