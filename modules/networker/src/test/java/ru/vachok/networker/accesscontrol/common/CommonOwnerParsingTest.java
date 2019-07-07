// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 26.06.2019 (17:09) */
@SuppressWarnings("ALL") public class CommonOwnerParsingTest {
    
    
    private TestConfigure testConfigure = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private String ownerToSearchPatt = "petrov";
    
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    @Test
    public void realRunTest() {
        CommonOwnerParsing commonOwnerParsing = new CommonOwnerParsing("kudr", 10000);
        List<String> ownedFilesGetter = commonOwnerParsing.userOwnedFilesGetter();
        Assert.assertTrue(ownedFilesGetter.size() >= 1, String.valueOf(ownedFilesGetter.size()));
        System.out.println(new TForms().fromArray(ownedFilesGetter, false));
    }
    
    @Test
    public void toString1() {
        String toString = new CommonOwnerParsing("kudr").toString();
        Assert.assertTrue(toString.contains("kudr"), toString);
    }
    
    @Test(enabled = false)
    public void testUserOwnedFilesGetter() {
        Path pathToRead = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя\\common.own");
        List<String> noBuiltinAdministrators = null;
        try (InputStream inputStream = new FileInputStream(pathToRead.toAbsolutePath().normalize().toString());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, ConstantsFor.CP_WINDOWS_1251);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            noBuiltinAdministrators = readOwners(bufferedReader);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        Assert.assertTrue(noBuiltinAdministrators.size() > 0);
        Map<String, List<String>> mapOwners = mapOwners(noBuiltinAdministrators);
        for (Map.Entry<String, List<String>> entry : mapOwners.entrySet()) {
            System.out.print("User = " + entry.getKey());
            System.out.println(" owned files = " + entry.getValue().size());
        }
        for (String key : mapOwners.keySet()) {
            if (key.toLowerCase().contains(ownerToSearchPatt)) {
                List<String> ownedFilesList = mapOwners.get(key);
                System.out.println(key + " files: " + ownedFilesList.size());
                System.out.println(new TForms().fromArray(ownedFilesList, false));
            }
        }
        
    }
    
    private Map<String, List<String>> mapOwners(List<String> administrators) {
        Map<String, List<String>> fileUserMap = new ConcurrentHashMap<>();
        administrators.parallelStream().forEach(fileUser->{
            try {
                String[] splitFileUser = fileUser.split(ConstantsFor.STR_OWNEDBY);
                if (!fileUserMap.containsKey(splitFileUser[1])) {
                    List<String> stringList = new ArrayList<>();
                    stringList.add(splitFileUser[0]);
                    fileUserMap.put(splitFileUser[1], stringList);
                }
                else {
                    fileUserMap.get(splitFileUser[1]).add(splitFileUser[0]);
                }
            }
            catch (IndexOutOfBoundsException ignore) {
                //
            }
        });
        Assert.assertTrue(fileUserMap.size() > 0);
        return fileUserMap;
    }
    
    private List<String> readOwners(BufferedReader bufferedReader) {
        List<String> builtinAdministrators = new ArrayList<>();
        long linesLimit;
        if (ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.HOSTNAME_DO213)) {
            linesLimit = Long.MAX_VALUE;
        }
        else {
            linesLimit = 4000;
        }
        bufferedReader.lines().limit(linesLimit).distinct().forEach(owner->{
            if (!owner.contains("BUILTIN\\Administrators")) {
                builtinAdministrators.add(owner);
            }
        });
        return builtinAdministrators;
    }
}