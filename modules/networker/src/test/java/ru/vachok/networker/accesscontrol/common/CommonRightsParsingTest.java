// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 @since 26.06.2019 (17:09) */
@SuppressWarnings("ALL") public class CommonRightsParsingTest {
    
    
    private TestConfigure testConfigure = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    
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
    public void testUserRightsGetter() {
        List<String> builtinAdministrators = new ArrayList<>();
        Path pathToRead = Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Внутренняя\\common.own");
        
        try (InputStream inputStream = new FileInputStream(pathToRead.toAbsolutePath().normalize().toString());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "windows-1251");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            bufferedReader.lines().forEach(owner->{
                if (owner.contains("BUILTIN\\Administrators")) {
                    builtinAdministrators.add(owner);
                }
            });
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        Assert.assertTrue(builtinAdministrators.size() > 0);
        testConfigure.getPrintStream().println("new TForms().fromArray(builtinAdministrators, false) = " + new TForms().fromArray(builtinAdministrators, false));
    }
}