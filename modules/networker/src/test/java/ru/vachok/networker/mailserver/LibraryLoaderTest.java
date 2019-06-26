package ru.vachok.networker.mailserver;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 @since 13.05.2019 (13:15) */
public class LibraryLoaderTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test
    public void testCL() {
        LibraryLoader libraryLoader = new OstLoader("");
        try {
            String className = "ru.vachok.ostpst.MakeConvert";
            Class<?> ostLoader = libraryLoader.libraryLoad(null, "ru.vachok.ostpst.OstToPst");
            loadingMeth(ostLoader.getDeclaringClass());
        }
        catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException | InstantiationException e) {
            e.printStackTrace();
        }
    }
    
    private void loadingMeth(Class<?> aClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            method.setAccessible(true);
            System.out.println("method = " + method);
        }
    }
}