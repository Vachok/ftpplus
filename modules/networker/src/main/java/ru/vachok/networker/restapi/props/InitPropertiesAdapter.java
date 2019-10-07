// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.util.Properties;


/**
 Class ru.vachok.networker.restapi.props.InitPropertiesAdapter
 <p>
 
 @see ru.vachok.networker.restapi.props.InitPropertiesAdapterTest
 @since 17.07.2019 (0:45) */
abstract class InitPropertiesAdapter {
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, InitPropertiesAdapter.class.getSimpleName());
    
    private static final String javaID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    public static Properties getProps() {
        return new DBRegProperties(javaID).getProps();
    }
    
    public static boolean setProps(Properties props) {
        return new DBRegProperties(javaID).setProps(props);
    }
    
    
}