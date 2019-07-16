// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.props.PropertiesAdapter;

import java.util.Properties;


/**
 @since 14.07.2019 (17:33) */
public interface InitProperties extends ru.vachok.mysqlandprops.props.InitProperties {
    
    
    @Override
    default MysqlDataSource getRegSourceForProperties() {
        return PropertiesAdapter.getDBRegProps(ConstantsFor.class.getSimpleName()).getRegSourceForProperties();
    }
    
    @Override
    Properties getProps();
    
    @Override
    boolean setProps(Properties properties);
    
    @Override
    boolean delProps();
}
