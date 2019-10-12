// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
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
        Properties properties = InitProperties.getInstance(InitProperties.FILE).getProps();
        Assert.assertTrue(properties.size() > 10);
        boolean isSetProps = InitPropertiesAdapter.setProps(properties);
        Assert.assertTrue(checkRealDatabase());
    }
    
    private boolean checkRealDatabase() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        final String sql = "SELECT * FROM `ru_vachok_networker` ORDER BY `ru_vachok_networker`.`timeset` DESC";
        boolean retBool = false;
    
        try (Connection c = dataConnectTo.getDefaultConnection("u0466446_properties");
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