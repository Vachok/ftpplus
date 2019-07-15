package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.FilePropsLocal;
import ru.vachok.networker.restapi.InitProperties;

import java.util.Properties;


public abstract class PropertiesAdapter implements InitProperties {
    
    
    public static DBRegProperties getDBRegProps(String propsID) {
        return new DBRegProperties(propsID);
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        return initProperties.getRegSourceForProperties();
    }
    
    @Override
    public abstract Properties getProps();
    
    @Override
    public abstract boolean setProps(Properties properties);
    
    @Override
    public abstract boolean delProps();
}
