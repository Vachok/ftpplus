// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.junit.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;

import java.io.File;


/**
 @see UpakFiles
 @since 06.07.2019 (7:32) */
public class UpakFilesTest {
    
    
    @Test
    public void testUpak() {
        UpakFiles upakFiles = new UpakFiles();
        try {
            String upakResult = upakFiles.packFile(new File("g:\\tmp_a.v.komarov.pst"));
        }
        catch (InvokeEmptyMethodException e) {
            System.err.println(e.getMessage());
            Assert.assertNotNull(e);
        }
    }
}