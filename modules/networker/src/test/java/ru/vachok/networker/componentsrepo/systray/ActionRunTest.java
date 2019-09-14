// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;


/**
 @see ActionRun
 @since 30.07.2019 (10:59) */
@SuppressWarnings("CallToRuntimeExecWithNonConstantString")
public class ActionRunTest {
    
    
    private String commandToRun = "ping 8.8.8.8";
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @Test
    public void testActionPerformed$$COPY() {
        try {
            Runtime.getRuntime().exec(commandToRun);
        }
        catch (IOException e1) {
            messageToUser.error(e1.getMessage());
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = new ActionRun("explorer").toString();
        Assert.assertEquals(toStr, "ActionRun{commandToRun='explorer'}");
    }
    
    @Test
    public void experementalAction() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commandToRun);
            Assert.assertTrue(process.isAlive());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try (InputStream processInputStream = process.getInputStream();
             InputStreamReader bufferedInputStream = new InputStreamReader(processInputStream, "IBM866");
             BufferedReader bufferedReader = new BufferedReader(bufferedInputStream)) {
            String linesArr = Arrays.toString(bufferedReader.lines().toArray());
            Assert.assertTrue(linesArr.contains("Пакетов: отправлено = 4, получено = 4, потеряно = 0"), linesArr);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
}