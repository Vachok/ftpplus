// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import java.util.Properties;


/**
 @since 14.07.2019 (17:33) */
public interface InitProperties extends ru.vachok.mysqlandprops.props.InitProperties {
    
    
    @Override
    Properties getProps();
    
    @Override
    boolean setProps(Properties properties);
    
    @Override
    boolean delProps();
}
