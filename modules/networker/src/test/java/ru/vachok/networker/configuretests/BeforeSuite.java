package ru.vachok.networker.configuretests;


import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterSuite;
import ru.vachok.networker.IntoApplication;


/**
 @since 20.08.2019 (10:45) */
public class BeforeSuite {
    
    
    private static final ConfigurableApplicationContext CONTEXT = IntoApplication.getConfigurableApplicationContext();
    
    @org.testng.annotations.BeforeSuite
    public void setCtx() {
        CONTEXT.start();
    }
    
    @AfterSuite
    public void closeCtx() {
        CONTEXT.stop();
        CONTEXT.close();
    }
}
