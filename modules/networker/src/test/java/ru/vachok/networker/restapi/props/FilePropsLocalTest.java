// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

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
        Assert.assertTrue(props.getProperty(ConstantsFor.PR_DBUSER).contains(ConstantsFor.DBPREFIX));
        boolean isSetToDB = new DBPropsCallable().setProps(props);
        System.out.println(MessageFormat.format("{0} is {1}", DBPropsCallable.class.getTypeName(), isSetToDB));
    }
    
    @Test
    public void testSetProps() {
    }
    
    @Test
    public void testDelProps() {
    }
}