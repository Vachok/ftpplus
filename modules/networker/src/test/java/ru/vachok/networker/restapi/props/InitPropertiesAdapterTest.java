// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

import static org.testng.Assert.assertNull;


/**
 @see InitPropertiesAdapter
 @since 17.07.2019 (0:48) */
public class InitPropertiesAdapterTest {
    
    
    private static final MessageToUser MESSAGE = new MessageLocal(InitPropertiesAdapterTest.class.getSimpleName());
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final String dbName = ConstantsFor.DBPREFIX + ConstantsFor.STR_PROPERTIES;
    
    private InitPropertiesAdapter initPropertiesAdapter = new InitPropertiesAdapter(InitProperties.DB);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testSetProps() {
        Properties props = InitProperties.getInstance(InitProperties.DB_LOCAL).getProps();
        Assert.assertTrue(props.size() > 9);
        props.setProperty("test", "test");
        boolean setProps = initPropertiesAdapter.setProps(props);
        Assert.assertTrue(checkRealDatabase());
        Assert.assertTrue(setProps, initPropertiesAdapter.toString());
    }
    
    private boolean checkRealDatabase() {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        final String sql = "SELECT * FROM `ru_vachok_networker` ORDER BY `ru_vachok_networker`.`timeset` DESC";
        boolean retBool = false;
        MysqlDataSource source = dataConnectTo.getDataSource();
        source.setPassword(AppComponents.getProps().getProperty(PropertiesNames.DBPASS));
        source.setUser(AppComponents.getProps().getProperty(PropertiesNames.DBUSER));
        source.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        try (Connection c = source.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                String property = r.getString(ConstantsFor.DBCOL_PROPERTY);
                String valueOfProperty = r.getString(ConstantsFor.DBCOL_VALUEOFPROPERTY);
                System.out.println(MessageFormat.format("{2}) {0}:{1}.\nStack {3}",
                    property, valueOfProperty, r.getString(ConstantsFor.DBFIELD_TIMESET)));
                if (property.equalsIgnoreCase("test") && valueOfProperty.equalsIgnoreCase("test")) {
                    retBool = true;
                }
            }
        }
        catch (SQLException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        return retBool;
    }
}