// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.restapi.props.InitPropertiesAdapter
 <p>
 
 @see ru.vachok.networker.restapi.props.InitPropertiesAdapterTest
 @since 17.07.2019 (0:45) */
public abstract class InitPropertiesAdapter {
    
    
    private static MessageToUser messageToUser = new MessageLocal(InitPropertiesAdapter.class.getSimpleName());
    
    public static boolean setProps(Properties props) {
        InitProperties libInit = new DBPropsCallable();
        libInit.setProps(props);
        libInit = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        libInit.setProps(props);
        return checkRealDB();
    }
    
    private static boolean checkRealDB() {
        final String sql = "SELECT * FROM `ru_vachok_networker` ORDER BY `timeset` DESC LIMIT 1";
        boolean retBool = false;
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try (Connection c = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES).getDataSource().getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Date parsedDate = dateFormat.parse(r.getString(ConstantsFor.DBFIELD_TIMESET));
                retBool = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1) < parsedDate.getTime();
                messageToUser.info(MessageFormat
                    .format("In database {0} last date is {1} (retBool {2})", ConstantsFor.DBBASENAME_U0466446_PROPERTIES, parsedDate, retBool));
            }
            return retBool;
        }
        catch (SQLException | ParseException e) {
            messageToUser.error(MessageFormat
                .format("InitPropertiesAdapter.checkRealDB\n{0}: {1}\nParameters: []\nReturn: boolean\nStack:\n{2}", e.getClass().getTypeName(), e
                    .getMessage(), new TForms().fromArray(e)));
            return false;
        }
    }
    
    public static Properties getProps() {
        ru.vachok.mysqlandprops.props.InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        Properties props = initProperties.getProps();
        if (props == null || props.isEmpty()) {
            return new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
        }
        else {
            return props;
        }
    }
}