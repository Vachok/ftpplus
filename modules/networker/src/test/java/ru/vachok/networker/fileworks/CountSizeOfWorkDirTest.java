// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import static org.testng.Assert.assertNull;


/**
 @see CountSizeOfWorkDir
 @since 24.06.2019 (22:07) */
public class CountSizeOfWorkDirTest {
    
    
    /**
     @see CountSizeOfWorkDir#call()
     */
    @Test
    public void testCall() {
        CountSizeOfWorkDir countSizeOfWorkDir = new CountSizeOfWorkDir();
        try {
            System.out.println("countSizeOfWorkDir.call() = " + countSizeOfWorkDir.call());
        }
        catch (Exception e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}