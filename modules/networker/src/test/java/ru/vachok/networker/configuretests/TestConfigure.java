package ru.vachok.networker.configuretests;


import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;

import java.io.IOException;
import java.io.PrintStream;


public interface TestConfigure {
    
    
    String TEST_FOLDER = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "tests" + ConstantsFor.FILESYSTEM_SEPARATOR;
    
    PrintStream getPrintStream() throws IOException;
    
    void before();
    
    void after();
    
}
