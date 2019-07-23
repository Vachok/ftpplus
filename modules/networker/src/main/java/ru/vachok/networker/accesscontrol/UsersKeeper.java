package ru.vachok.networker.accesscontrol;


import ru.vachok.networker.abstr.monitors.UserKeeper;

import java.util.Map;


/**
 @since 23.07.2019 (11:35) */
public class UsersKeeper implements UserKeeper {
    
    
    private Map<String, String> uniqUserInetAccess;
    
    @Override
    public Map<String, String> getUniqUserInetAccess() {
        return uniqUserInetAccess;
    }
    
    public void setUniqUserInetAccess(Map<String, String> uniqUserInetAccess) {
        this.uniqUserInetAccess = uniqUserInetAccess;
    }
}
