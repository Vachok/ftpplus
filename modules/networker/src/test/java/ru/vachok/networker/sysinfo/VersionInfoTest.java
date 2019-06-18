// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;


/**
 @since 15.06.2019 (14:00) */
@SuppressWarnings("ALL") public class VersionInfoTest {
    
    @Test
    public void testSetParams() {
        String setParamsString = AppComponents.versionInfo().toString();
        Assert.assertTrue(setParamsString.contains(ConstantsFor.thisPC()), setParamsString);
    }
    
    @Test
    public void getParamsTEST() {
        VersionInfo infoVers = AppComponents.versionInfo();
        String versString = infoVers.toString();
        Assert.assertFalse(versString.contains("rups00"), versString);
        Assert.assertEquals(AppComponents.getUserPref().get(ConstantsFor.PR_APP_VERSION, ""), infoVers.getAppVersion());
    }
}