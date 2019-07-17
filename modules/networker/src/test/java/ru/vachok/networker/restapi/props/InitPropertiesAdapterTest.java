// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

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
    
    protected final String dbName = ConstantsFor.DBPREFIX + ConstantsFor.STR_PROPERTIES;
    
    @Test
    public void testSetProps() {
        InitProperties initProperties = new DBPropsCallable();
        Properties props = initProperties.getProps();
        Assert.assertTrue(props.size() > 9);
        InitPropertiesAdapter.setProps(props);
    }
    
    private void checkRealDatabase() {
        DataConnectTo dataConnectTo = new RegRuMysqlLoc(dbName);
        final String sql = "SELECT * FROM `ru_vachok_networker` ORDER BY `ru_vachok_networker`.`timeset` DESC";
        
        try (Connection c = dataConnectTo.getDataSource().getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                System.out.println(MessageFormat.format("{2}) {0}:{1}.\nStack {3}",
                    r.getString("property"), r.getString("valueofproperty"), r.getString(ConstantsFor.DBFIELD_TIMESET), r.getString("stack")));
            }
        }
        catch (SQLException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}