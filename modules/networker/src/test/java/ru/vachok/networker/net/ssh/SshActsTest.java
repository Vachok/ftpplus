// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @since 18.06.2019 (15:36) */
public class SshActsTest {


    private static final String VELKOMFOOD = "www.velkomfood.ru";

    @Test
    public void testAllowDomainAdd() {
        SshActs sshActs = new SshActs();
        sshActs.setAllowDomain(ConstantsFor.SITENAME_VELKOMFOODRU);
        Future<String> domainAddStringFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->{
            try {
                return sshActs.allowDomainAdd();
            }
            catch (InvokeIllegalException e) {
                return e.getMessage();
            }
        });
        try {
            String domainAddString = domainAddStringFuture.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(domainAddString.contains(VELKOMFOOD) | domainAddString.contains("Domain is "), domainAddString);
            System.out.println("domainAddString = " + domainAddString);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testAllowDomainDel() {
        SshActs sshActs = new SshActs();
        Future<String> allowDomainDelString = AppComponents.threadConfig().getTaskExecutor().submit(sshActs::allowDomainDel);
        try {
            String s = allowDomainDelString.get(30, TimeUnit.SECONDS);
            Assert.assertFalse(s.contains(VELKOMFOOD), s);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    public void testWhatSrvNeed() {
        SshActs sshActs = new SshActs();
        String srvNeed = sshActs.whatSrvNeed();
        Assert.assertEquals(srvNeed, "192.168.13.42");
    }
}