package ru.vachok.networker.accesscontrol.sshactions;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;


/**
 @since 18.06.2019 (15:36) */
public class SshActsTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    
    @Test
    public void testAllowDomainAdd() {
        SshActs sshActs = new SshActs();
        String domainAddString = sshActs.allowDomainAdd();
        Assert.assertTrue(domainAddString.contains("www.velkomfood.ru"), domainAddString);
    }
    
    @Test
    public void testAllowDomainDel() {
        SshActs sshActs = new SshActs();
        String allowDomainDelString = sshActs.allowDomainDel();
        Assert.assertFalse(allowDomainDelString.contains("www.velkomfood.ru"));
    }
    
    @Test
    public void testWhatSrvNeed() {
        SshActs sshActs = new SshActs();
        String srvNeed = sshActs.whatSrvNeed();
        Assert.assertEquals(srvNeed, "192.168.13.42");
    }
}