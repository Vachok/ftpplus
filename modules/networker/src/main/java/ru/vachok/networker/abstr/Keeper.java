// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import java.net.InetAddress;
import java.util.Deque;


public interface Keeper {
    
    
    Deque<InetAddress> getOnlineDevicesInetAddress();
    
}
