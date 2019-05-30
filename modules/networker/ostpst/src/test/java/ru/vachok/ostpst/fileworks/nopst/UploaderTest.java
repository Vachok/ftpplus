// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;

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
        fileNames.stream().forEach(x->{
            String cpFilePathStr = /*PATH AS STRING HERE +*/  new File(x).getName();
            FileWorker fileWorker = new Uploader(x, cpFilePathStr);
            ((Uploader) fileWorker).setBytesBuffer(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES * 30);
            File fileCopy = new File(cpFilePathStr);
            File fileOrig = new File(x);
            if (fileCopy.exists()) {
                System.out.println("fileWorker = " + fileWorker.continuousCopy());
            }
            else {
                fileWorker.processNewCopy();
            }
            Assert.assertTrue(fileCopy.isFile());
            Assert.assertTrue(fileCopy.length() != fileOrig.length());
/*
            if (fileCopy.length() != fileOrig.length()) {
                var missLong = chkMissed(fileCopy.toPath(), fileOrig.toPath());
                Assert.assertTrue(missLong < 0, missLong + " error from byte");
            }
*/
            fileNames.remove(x);
        });
        FileSystemWorkerOST.writeFile("d:\\dn.list", fileNames.stream());
    }
    
/*
    private long chkMissed(Path fileCopy, Path fileOrig) {
        try {
            
            return Files.mismatch(fileCopy, fileOrig);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
            throw new AssertionFailedException(e);
        }
    }
*/
}