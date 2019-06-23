package ru.vachok.ostpst.api;


import java.util.Properties;


public interface InitProperties {
    
    
    Properties getProps();
    boolean delProps();
    boolean setProps(Properties properties);
}
