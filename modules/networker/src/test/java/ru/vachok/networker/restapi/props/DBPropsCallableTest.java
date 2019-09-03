// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

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
        long lastScan = Long.parseLong(call.getProperty(PropertiesNames.LASTSCAN));
        Assert.assertTrue(lastScan > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)), new Date(lastScan).toString());
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
    
    @Test
    public void testFileReadOnly() {
        File localProps = new File(ConstantsFor.class.getSimpleName() + ".properties");
        try {
            Files.setAttribute(localProps.toPath(), "dos:readonly", true);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        this.initProperties = new DBPropsCallable();
        Properties properties = initProperties.call();
        Assert.assertTrue(properties.size() >= 17);
        Assert.assertEquals(properties.getProperty("test"), "test");
        Assert.assertTrue(localProps.canWrite());
    }
}