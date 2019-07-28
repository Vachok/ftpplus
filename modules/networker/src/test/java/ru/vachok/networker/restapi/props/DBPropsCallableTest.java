package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.restapi.InitProperties;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class DBPropsCallableTest {
    
    
    private InitProperties initProperties = new DBPropsCallable();
    
    
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
        long lastscan = Long.parseLong(call.getProperty(ConstantsNet.PR_LASTSCAN));
        Assert.assertTrue(lastscan > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)), new Date(lastscan).toString());
    }
    
    @Test
    public void testDelProps() {
        this.initProperties = new DBPropsCallable(ConstantsFor.APPNAME_WITHMINUS, this.getClass().getSimpleName());
        Assert.assertTrue(initProperties.delProps());
    }
    
    @Test
    public void testToString1() {
        String toString = initProperties.toString();
        Assert.assertTrue(toString.contains("RegRuMysqlLoc["), toString);
    }
}