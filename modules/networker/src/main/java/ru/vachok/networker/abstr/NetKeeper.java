package ru.vachok.networker.abstr;


import java.io.File;
import java.net.InetAddress;
import java.util.Deque;
import java.util.List;


public interface NetKeeper extends Keeper {
    
    
    Deque<InetAddress> getOnlineDevicesInetAddress();
    
    List<String> getCurrentScanLists();
    
    List<File> getCurrentScanFiles();
}
