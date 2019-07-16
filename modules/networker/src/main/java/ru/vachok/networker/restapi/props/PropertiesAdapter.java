// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.InitProperties;

import java.util.Properties;


public abstract class PropertiesAdapter implements InitProperties {
    
    
    private static String propsDatabaseID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    @Contract("_ -> new")
    public static DBRegProperties getDBRegProps(String propsID) {
        PropertiesAdapter.propsDatabaseID = propsID;
        return new DBRegProperties(propsID);
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        return new DBRegProperties(propsDatabaseID).getRegSourceForProperties();
    }
    
    @Override
    public abstract Properties getProps();
    
    @Override
    public abstract boolean setProps(Properties properties);
    
    @Override
    public abstract boolean delProps();
}
