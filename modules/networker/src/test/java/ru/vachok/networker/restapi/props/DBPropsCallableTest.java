// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class DBPropsCallableTest {
    
    
    private final Properties retProps = InitProperties.getInstance(InitProperties.FILE).getProps();
    
    private DBPropsCallable initProperties = new DBPropsCallable();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @Test
    public void testGetRegSourceForProperties() {
        MysqlDataSource sourceForProperties = initProperties.getRegSourceForProperties();
        String propertiesURL = sourceForProperties.getURL();
        Assert.assertEquals(propertiesURL, "jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_properties");
    }
    
    @Test
    public void testGetProps() {
        Properties propertiesProps = initProperties.getProps();
        Assert.assertFalse(propertiesProps.isEmpty());
    }
    
    @Test
    public void testSetProps() {
        this.initProperties = new DBPropsCallable(ConstantsFor.APPNAME_WITHMINUS, this.getClass().getSimpleName());
        Properties properties = new Properties();
        properties.setProperty("test", "test");
        initProperties.setProps(properties);
        Properties initPropertiesProps = initProperties.getProps();
        Assert.assertEquals(initPropertiesProps.getProperty("test"), "test");
    }
    
    @Test
    public void testCall() {
        Properties call = new DBPropsCallable().call();
        long lastscan = Long.parseLong(call.getProperty(PropertiesNames.LASTSCAN));
        Assert.assertTrue(lastscan > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)), new Date(lastscan).toString());
    }
    
    @Test
    public void testDelProps() {
        this.initProperties = new DBPropsCallable(ConstantsFor.APPNAME_WITHMINUS, this.getClass().getSimpleName());
        Assert.assertTrue(initProperties.delProps());
    }
    
    @Test
    public void testToString() {
        String toString = initProperties.toString();
        Assert.assertTrue(toString.contains("RegRuMysqlLoc["), toString);
    }
}