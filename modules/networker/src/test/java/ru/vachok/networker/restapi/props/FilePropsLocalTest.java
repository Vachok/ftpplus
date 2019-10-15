// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.text.MessageFormat;
import java.util.Properties;


/**
 @see FilePropsLocal
 @since 17.07.2019 (1:25) */
public class FilePropsLocalTest {
    
    
    @Test
    public void testGetProps() {
        Properties props = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
        Assert.assertTrue(props.size() > 9);
        String propertyDBUser = props.getProperty(PropertiesNames.DBUSER);
        Assert.assertTrue(propertyDBUser.contains(ConstantsFor.DBPREFIX), "dbuser: u0466446_ is not found! Found: " + propertyDBUser);
        boolean isSetToDB = new DBPropsCallable().setProps(props);
        System.out.println(MessageFormat.format("{0} is {1}", DBPropsCallable.class.getTypeName(), isSetToDB));
    }
    
    @Test
    public void testSetProps() {
    }
    
    @Test
    public void testDelProps() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        String showString = initProperties.toString();
        System.out.println("showString = " + showString);
    }
}