// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @since 16.06.2019 (9:00) */
public class SSHFactoryTest {
    
    
    @Test
    public void testCall() {
        SSHFactory sshFactory = new SSHFactory.Builder("192.168.13.42", "ls", getClass().getSimpleName()).build();
        String sshCall = sshFactory.call();
        Assert.assertTrue(sshCall.contains("!_passwords.xlsx"), sshCall);
    }
}