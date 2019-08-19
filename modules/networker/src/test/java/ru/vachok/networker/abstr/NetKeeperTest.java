// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.net.NetKeeper;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;


/**
 @see NetKeeper
 @since 02.08.2019 (0:23) */
public class NetKeeperTest {
    
    
    @Test
    public void testGetOnePcMonitor() {
        int size = NetKeeper.getOnePcMonitor().size();
        Assert.assertTrue(size == 0);
    }
    
    @Test
    public void testGetNetworkPCs() {
        int size = NetKeeper.getNetworkPCs().size();
        Assert.assertTrue(size == 0);
    }
    
    @Test
    public void testGetScanFiles() {
        Map<String, File> scanFiles = NetKeeper.getScanFiles();
    }
    
    @Test
    public void testGetCurrentScanLists() {
        List<String> scanLists = NetKeeper.getCurrentScanLists();
        String fromArray = new TForms().fromArray(scanLists);
        Assert.assertTrue(fromArray.isEmpty());
        scanFilesIsNotNine();
    }
    
    @Test
    public void testMakeFilesMap() {
        int filesMap = NetKeeper.makeFilesMap();
        Assert.assertTrue(filesMap == 9, String.valueOf(filesMap));
    }
    
    @Test
    public void testGetCurrentScanFiles() {
        List<File> scanFiles = NetKeeper.getCurrentScanFiles();
        String fromArray = new TForms().fromArray(scanFiles);
        Assert.assertTrue(fromArray.contains("lan_200205.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_213220.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_11vsrv.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_old1.txt"), fromArray);
    }
    
    @Test
    public void testGetAllDevices() {
        BlockingDeque<String> allDevices = NetKeeper.getAllDevices();
        Assert.assertTrue(allDevices.size() == 0);
    }
    
    @Test
    public void testGetKudrWorkTime() {
        List<String> kudrWorkTime = NetKeeper.getKudrWorkTime();
        Assert.assertTrue(kudrWorkTime.size() == 0);
    }
    
    private void scanFilesIsNotNine() {
        int size = NetKeeper.getCurrentScanFiles().size();
        Assert.assertTrue(size == 9);
    }
}