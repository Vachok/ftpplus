// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import org.jetbrains.annotations.Contract;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import static org.testng.Assert.assertNull;


/**
 @see ActionOpenProgFolder
 @since 12.07.2019 (20:21) */
public class ActionOpenProgFolderTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 3));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    public ActionOpenProgFolderTest() {
        if (!System.getProperty("os.name").toLowerCase().contains(PropertiesNames.PR_WINDOWSOS)) {
            throw new UnsupportedOperationException(System.getProperty("os.name"));
        }
    }
    
    @Test
    @Ignore
    public void testActionPerformed() {
        Path workingNowPathRoot = Paths.get(".");
        String confirmDo = messageToUser
            .confirm(ConstantsFor.APPNAME_WITHMINUS.replace("-", ""), ActionOpenProgFolder.TITLE_MSG, ActionOpenProgFolder.TITLE_MSG);
        if (confirmDo.equals("ok")) {
            try {
                Process execOpen = Runtime.getRuntime().exec(MessageFormat.format("explorer \"{0}\n", workingNowPathRoot));
            }
            catch (IOException e) {
                assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
        }
        else {
            thrNewUserChangeExceptionTest();
        }
    }
    
    @Contract(" -> fail")
    private static void thrNewUserChangeExceptionTest() {
        throw new TODOException("28.07.2019 (23:05)");
    }
}