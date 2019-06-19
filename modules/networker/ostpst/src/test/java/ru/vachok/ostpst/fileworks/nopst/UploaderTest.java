// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.api.FileProperties;
import ru.vachok.ostpst.api.InitProperties;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;


public class UploaderTest {
    
    
    @Test(enabled = false)
    public void testUpload() {
        Queue<String> fileNames = getFileNames();
        InitProperties initProperties = new FileProperties("ostpst.properties");
        while (!fileNames.isEmpty()) {
            String toAppend = parseQueue(fileNames, initProperties);
            FileSystemWorkerOST.appendStringToFile("dn.list", toAppend);
            if (toAppend.equalsIgnoreCase("copy completed")) {
                throw new UnsupportedOperationException(FileSystemWorkerOST.readFileToString("dn.list"));
            }
        }
        ;
    }
    
    @Test(enabled = false)
    public void chkFiles() {
        var names = getFileNames();
        System.out.println(new TFormsOST().fromArray(names.stream()));
        names.forEach((x)->{
            try {
                long start = System.currentTimeMillis();
                System.out.println(new Date(start));
                var split = x.split(" to: ");
                var fileCopy = split[1].trim();
                System.out.println(fileCopy + " size GB =  " + new File(fileCopy).length() / ConstantsOst.KBYTE_BYTES / ConstantsOst.KBYTE_BYTES);
                var toAppend = x + " is " + chkMissed(Paths.get(split[0].replaceFirst("From: ", "")), Paths.get(fileCopy));
                System.out.println(toAppend);
                System.out.println(((float) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) / (float) 60) + " min");
                System.out.println(FileSystemWorkerOST.appendStringToFile("dn.list", toAppend));
            }
            catch (IndexOutOfBoundsException e) {
                Assert.assertNull(e, e.getMessage());
            }
        });
    }
    
    private Queue<String> getFileNames() {
        Queue<String> fileNames = new ConcurrentLinkedQueue<>();
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
        return fileNames;
    }
    
    
    private long chkMissed(Path fileCopy, Path fileOrig) {
        try {
            
            return Files.mismatch(fileCopy, fileOrig);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
            throw new UnsupportedOperationException(e.getMessage());
        }
    }
    
    private String parseQueue(Queue<String> fileNames, InitProperties initProperties) throws InvalidPathException, IndexOutOfBoundsException {
        String x = fileNames.poll();
        if (x != null && x.equalsIgnoreCase("Copy completed")) {
            return FileSystemWorkerOST.readFileToString("dn.list");
        }
        System.setProperty(ConstantsOst.STR_ENCODING, "UTF8");
        String[] copyPaths = new String[2];
        try {
            copyPaths[0] = x.split(" cpto: ")[0];
            copyPaths[1] = x.split(" cpto: ")[1];
        }
        catch (IndexOutOfBoundsException e) {
            copyPaths[0] = x;
            copyPaths[1] = x + ".tmp";
        }
        FileWorker fileWorker = null;
        try {
            fileWorker = new Uploader(copyPaths[0], copyPaths[1], initProperties);
        }
        catch (FileNotFoundException e) {
            Assert.assertNull(e, e.getMessage() + " " + getClass().getSimpleName());
        }
        
        ((Uploader) fileWorker).setBytesBuffer(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES * 30);
        File fileCopy = new File(copyPaths[1]);
        File fileOrig = new File(copyPaths[0]);
        String retStatus = "From: " + fileOrig.getAbsolutePath() + " to: " + fileCopy.getAbsolutePath();
        if (fileCopy.exists()) {
            long continuousCopyStatus = fileWorker.continuousCopy();
            retStatus = "fileWorker = " + continuousCopyStatus;
            if (continuousCopyStatus == -10) {
                retStatus = "Copy completed\n" + fileWorker;
            }
            System.out.println(retStatus);
        }
        else {
            fileWorker.processNewCopy();
        }
        Assert.assertTrue(fileCopy.isFile());
        if (fileCopy.length() != fileOrig.length()) {
            var missLong = chkMissed(fileCopy.toPath(), fileOrig.toPath());
            Assert.assertTrue(missLong < 0, missLong + " error from byte");
        }
        FileSystemWorkerOST.writeFile("dn.list", fileNames.stream());
        return retStatus;
    }
    
}