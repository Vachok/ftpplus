// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;


/**
 @see VersionInfo
 @since 15.06.2019 (14:00) */
@SuppressWarnings("ALL") public class VersionInfoTest {
    
    
    /**
     @see VersionInfo#setParams()
     */
    @Test
    public void testSetParams() {
        String setParamsString = ConstantsFor.APP_VERSION;
        Assert.assertTrue(setParamsString.contains(ConstantsFor.thisPC()), setParamsString);
        VersionInfo setParamsTry = AppComponents.versionInfo("rups00.eatmeat.ru");
        Assert.assertFalse(setParamsTry.getAppBuild().contains("rups00"), setParamsTry.toString());
    }
    
    /**
     @see VersionInfo#getParams()
     */
    @Test
    public void getParamsTEST() {
        VersionInfo infoVers = new VersionInfo(AppComponents.getProps(), ConstantsFor.thisPC());
        String versString = ConstantsFor.APP_VERSION;
        Assert.assertFalse(versString.contains("rups00"), versString);
        Assert.assertFalse(infoVers.getAppBuild().isEmpty());
        Assert.assertFalse(infoVers.getBuildTime().isEmpty());
        Assert.assertFalse(infoVers.getAppVersion().isEmpty());
    }
}