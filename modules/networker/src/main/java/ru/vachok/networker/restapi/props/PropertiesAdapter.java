package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.restapi.InitProperties;

import java.util.Properties;


public abstract class PropertiesAdapter implements InitProperties {
    
    
    public static DBRegProperties getDBRegProps(String propsID) {
        return new DBRegProperties(propsID);
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        return null;
    }
    
    @Override
    public Properties getProps() {
        return null;
    }
    
    @Override
    public boolean setProps(Properties properties) {
        return false;
    }
    
    @Override
    public boolean delProps() {
        return false;
    }
}
