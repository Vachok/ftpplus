// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @since 15.06.2019 (14:00) */
public class VersionInfoTest {
    
    
    @Test
    public void testGetPropertiesFrom() {
    }
    
    @Test
    public void testSetPropertiesFrom() {
    }
    
    @Test
    public void testGetAppBuild() {
    }
    
    @Test
    public void testGetAppVersion() {
    }
    
    @Test
    public void testGetBuildTime() {
    }
    
    @Test
    public void testSetParams() {
        String setParamsString = new VersionInfo().setParams();
        System.out.println(setParamsString);
        Assert.assertTrue(setParamsString.contains("is SET"), setParamsString);
    }
    
    @Test
    public void getParamsTEST() {
        String getParamsStr = new VersionInfo().getParams();
        System.out.println(getParamsStr);
        Assert.assertTrue(getParamsStr.contains("is GET"), getParamsStr);
    }
}