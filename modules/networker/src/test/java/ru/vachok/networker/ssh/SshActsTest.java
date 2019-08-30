// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ssh;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.util.concurrent.*;


/**
 @since 18.06.2019 (15:36) */
public class SshActsTest {
    
    
    private static final String VELKOMFOOD = "www.velkomfood.ru";
    
    @Test
    public void testAllowDomainAdd() {
        SshActs sshActs = new SshActs();
        Future<String> domainAddStringFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(sshActs::allowDomainAdd);
        try {
            String domainAddString = domainAddStringFuture.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(domainAddString.contains(VELKOMFOOD) | domainAddString.contains("Domain is "), domainAddString);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
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