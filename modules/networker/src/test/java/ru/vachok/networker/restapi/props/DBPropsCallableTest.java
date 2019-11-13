// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class DBPropsCallableTest {
    
    
    private final Properties retProps = InitProperties.getInstance(InitProperties.FILE).getProps();
    
    private DBPropsCallable initProperties = new DBPropsCallable();
    
    private static final MessageToUser messageToUser = new MessageLocal(DBPropsCallableTest.class.getSimpleName());
    
    @Test
    public void testGetRegSourceForProperties() {
        MysqlDataSource sourceForProperties = initProperties.getRegSourceForProperties();
        String propertiesURL = sourceForProperties.getURL();
        Assert.assertEquals(propertiesURL, "jdbc:mysql://srv-inetstat.eatmeat.ru:3306/u0466446_properties");
    }
    
    @Test
    public void testGetProps() {
        Properties propertiesProps = initProperties.getProps();
        Assert.assertFalse(propertiesProps.isEmpty());
    }
    
    @Test
    public void testSetProps() {
        this.initProperties = new DBPropsCallable(this.getClass().getSimpleName());
        Properties properties = initProperties.getProps();
        properties.setProperty("test", "test");
        initProperties.setProps(properties);
        Properties initPropertiesProps = initProperties.getProps();
        Assert.assertEquals(initPropertiesProps.getProperty("test"), "test");
    }
    
    @Test
    public void testCall() {
        Properties call = new DBPropsCallable().call();
        long lastScan = Long.parseLong(call.getProperty(PropertiesNames.LASTSCAN));
        Assert.assertTrue(lastScan > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)), new Date(lastScan).toString());
    }
    
    @Test
    @Ignore
    public void testDelProps() {
        this.initProperties = new DBPropsCallable(this.getClass().getSimpleName());
        Assert.assertTrue(initProperties.delProps());
    }
    
    @Test
    public void testToString() {
        String toString = initProperties.toString();
        Assert.assertTrue(toString.contains("jdbc:mysql://srv-inetstat.eatmeat.ru:3306"), toString);
        Assert.assertTrue(toString.contains("DBPropsCallable{"), toString);
    }
    
    @Test
    public void testFileReadOnly() {
        File localProps = new File(ConstantsFor.class.getSimpleName() + FileNames.EXT_PROPERTIES);
        try {
            Files.setAttribute(localProps.toPath(), OtherConstants.READONLY, true);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        this.initProperties = new DBPropsCallable();
        Properties properties = initProperties.getProps();
        properties.setProperty("test", "test");
        Assert.assertTrue(properties.size() >= 17);
        Assert.assertEquals(properties.getProperty("test"), "test");
        Assert.assertTrue(localProps.canWrite());
        properties.remove("test");
        Assert.assertFalse(properties.contains("test"));
    }
}