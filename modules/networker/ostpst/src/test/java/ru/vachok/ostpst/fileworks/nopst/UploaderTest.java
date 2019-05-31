// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import com.mysql.jdbc.AssertionFailedException;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class UploaderTest {
    
    
    @Test(enabled = true)
    public void testUpload() {
        List<String> fileNames = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("dn.list");
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
            String[] copyPaths = x.split(" cpto: ");
            FileWorker fileWorker = new Uploader(copyPaths[0], copyPaths[1]);
            ((Uploader) fileWorker).setBytesBuffer(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES * 30);
            File fileCopy = new File(copyPaths[1]);
            File fileOrig = new File(copyPaths[0]);
            if (fileCopy.exists()) {
                System.out.println("fileWorker = " + fileWorker.continuousCopy());
            }
            else {
                fileWorker.processNewCopy();
            }
            Assert.assertTrue(fileCopy.isFile());
            if (fileCopy.length() != fileOrig.length()) {
                var missLong = chkMissed(fileCopy.toPath(), fileOrig.toPath());
                Assert.assertTrue(missLong < 0, missLong + " error from byte");
            }

            fileNames.remove(x);
        });
        FileSystemWorkerOST.writeFile("dn.list", fileNames.stream());
    }
    
    
    private long chkMissed(Path fileCopy, Path fileOrig) {
        try {
            
            return Files.mismatch(fileCopy, fileOrig);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
            throw new AssertionFailedException(e);
        }
    }
    
}