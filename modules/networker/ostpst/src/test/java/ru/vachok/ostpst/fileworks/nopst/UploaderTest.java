// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class UploaderTest {
    
    
    @Test(enabled = false)
    public void testUpload() {
        List<String> fileNames = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("d:\\dn.list");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(fileNames::add);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        ;
        fileNames.forEach((x)->{
            FileWorker fileWorker = new Uploader(x, "d:\\pron\\" + new File(x).getName());
            ((Uploader) fileWorker).setBytesBuffer(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES * 42);
            System.out.println("fileWorker = " + fileWorker.continuousCopy());
        });
    }
}