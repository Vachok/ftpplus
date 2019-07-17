// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;

import java.util.Properties;


/**
 Class ru.vachok.networker.restapi.props.InitPropertiesAdapter
 <p>
 
 @see ru.vachok.networker.restapi.props.InitPropertiesAdapterTest
 @since 17.07.2019 (0:45) */
public abstract class InitPropertiesAdapter {
    
    
    public static boolean setProps(Properties props) {
        InitProperties libInit = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        return libInit.setProps(props);
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