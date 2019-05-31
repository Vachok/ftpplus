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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;


public class UploaderTest {
    
    
    @Test(enabled = true)
    public void testUpload() {
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
        while (!fileNames.isEmpty()) {
            String toAppend = parseQueue(fileNames);
            FileSystemWorkerOST.appendStringToFile("dn.list", toAppend);
            if (toAppend.equalsIgnoreCase("copy completed")) {
                throw new RejectedExecutionException(FileSystemWorkerOST.readFileToString("dn.list"));
            }
        }
        ;
    }
    
    private String parseQueue(Queue<String> fileNames) {
        String x = fileNames.poll();
        if (x.equalsIgnoreCase("Copy completed")) {
            return FileSystemWorkerOST.readFileToString("dn.list");
        }
        System.setProperty("encoding", "UTF8");
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
            fileWorker = new Uploader(copyPaths[0], copyPaths[1]);
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