// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringJoiner;


/**
 Class ru.vachok.networker.restapi.props.InitPropertiesAdapter
 <p>
 
 @see ru.vachok.networker.restapi.props.InitPropertiesAdapterTest
 @since 17.07.2019 (0:45) */
class InitPropertiesAdapter implements ru.vachok.networker.restapi.props.InitProperties {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, InitPropertiesAdapter.class.getSimpleName());
    
    private ru.vachok.mysqlandprops.props.InitProperties initProperties;
    
    @Contract(pure = true)
    public InitPropertiesAdapter(@NotNull String where) {
        if (where.equalsIgnoreCase(ru.vachok.networker.restapi.props.InitProperties.DB)) {
            this.initProperties = new DBRegProperties("ru.vachok.networker-" + ConstantsFor.class.getSimpleName());
        }
        else {
            this.initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        }
    }
    
    @Override
    public Properties getProps() {
        return initProperties.getProps();
    }
    
    @Override
    public boolean setProps(Properties props) {
        if (initProperties instanceof DBRegProperties) {
            return checkIdDB();
        }
        return initProperties.setProps(props);
    }
    
    @Override
    public boolean delProps() {
        return initProperties.delProps();
    }
    
    private static boolean checkIdDB() {
        final String sql = "SELECT * FROM `ru_vachok_networker` ORDER BY `ru_vachok_networker`.`timeset` DESC";
        boolean retBool = false;
        try (Connection c = DataConnectTo.getInstance(DataConnectTo.LOCAL_REGRU).getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
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
            messageToUser.error(e.getMessage() + " see line: 81 ***");
        }
        return retBool;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", InitPropertiesAdapter.class.getSimpleName() + "[\n", "\n]")
            .add("initProperties = " + initProperties)
            .toString();
    }
}