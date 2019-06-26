package ru.vachok.networker.configuretests;


import java.io.PrintStream;


public interface TestConfigure {
    
    
    PrintStream getPrintStream();
    void beforeClass();
    void afterClass();
    
}
