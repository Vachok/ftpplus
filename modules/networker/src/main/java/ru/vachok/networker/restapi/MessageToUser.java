// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;

import java.util.Collection;


public interface MessageToUser extends ru.vachok.messenger.MessageToUser {
    
    
    default void info(Collection<?> fromCollection) {
        Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
        logger.info(new TForms().fromArray(fromCollection));
    }
    
    @Override
    default String confirm(String s, String s1, String s2) {
        throw new InvokeIllegalException("06.08.2019 (11:39)");
    }
}