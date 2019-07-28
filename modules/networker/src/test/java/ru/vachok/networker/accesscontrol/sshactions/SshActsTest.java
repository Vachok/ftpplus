// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.sshactions;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @since 18.06.2019 (15:36) */
public class SshActsTest {
    
    
    private static final String VELKOMFOOD = "www.velkomfood.ru";
    
    @Test
    public void testAllowDomainAdd() {
        SshActs sshActs = new SshActs();
        String domainAddString = sshActs.allowDomainAdd();
        Assert.assertTrue(domainAddString.contains(VELKOMFOOD) | domainAddString.contains("Domain is "), domainAddString);
    }
    
    @Test
    public void testAllowDomainDel() {
        SshActs sshActs = new SshActs();
        String allowDomainDelString = sshActs.allowDomainDel();
        Assert.assertFalse(allowDomainDelString.contains(VELKOMFOOD), allowDomainDelString);
    }
    
    @Test
    public void testWhatSrvNeed() {
        SshActs sshActs = new SshActs();
        String srvNeed = sshActs.whatSrvNeed();
        Assert.assertEquals(srvNeed, "192.168.13.42");
    }
}