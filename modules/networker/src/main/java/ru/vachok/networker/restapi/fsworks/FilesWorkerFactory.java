// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.net.NetScanFileWorker;

import java.net.InetAddress;
import java.util.Deque;
import java.util.List;


/**
 Class ru.vachok.networker.restapi.fsworks.FilesWorkerFactory
 <p>
 
 @since 19.07.2019 (22:48) */
public abstract class FilesWorkerFactory extends AbstractNetworkerFactory implements FilesHelper {
    
    
    @Override
    public List<String> getCurrentScanLists() {
        return DiapazonScan.getCurrentPingStats();
    }
    
    @Override
    public Deque<InetAddress> getOnlineDevicesInetAddress() {
        return new NetScanFileWorker().getOnlineDevicesInetAddress();
    }
    
}