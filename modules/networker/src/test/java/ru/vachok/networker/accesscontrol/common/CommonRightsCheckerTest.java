// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.*;


/**
 @see CommonRightsChecker
 @since 22.06.2019 (11:13) */
public class CommonRightsCheckerTest {
    
    
    private static final String ALLOW = ": ALLOW";
    
    Path currentPath;
    
    private Runnable checker = new CommonRightsChecker(Paths.get("\\\\10.10.111.1\\Torrents-FTP\\home\\").normalize().toAbsolutePath(), Paths.get(".")
        .toAbsolutePath().normalize());
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public CommonRightsCheckerTest() {
        currentPath = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\_IT_FAQ\\");
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            currentPath = Paths.get("z:\\home\\");
        }
    }
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test(enabled = false)
    public void testRunningLogic() {
        Path startPath = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\_AdminTools\\ru_vachok_inet_inetor_main\\ru.vachok.inet.inetor.main\\app\\inetor_main\\");
        Path logCopyPath = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\!!!Docs!!!\\");
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            logCopyPath = Paths.get("\\\\10.10.111.1\\Torrents-FTP\\logsCopy\\");
        }
    
        CommonRightsChecker rightsChecker = new CommonRightsChecker(logCopyPath);
    
        File rghCopyFile = new File(logCopyPath.toAbsolutePath().normalize() + "\\" + ConstantsFor.FILENAME_COMMONRGH);
        File ownCopyFile = new File(logCopyPath.toAbsolutePath().normalize() + "\\" + ConstantsFor.FILENAME_COMMONOWN);
    
        File ownFile = new File(ConstantsFor.FILENAME_COMMONOWN);
        File rghFile = new File(ConstantsFor.FILENAME_COMMONRGH);
        delOldLogs(ownCopyFile, rghCopyFile);
        
        final long currentMillis = System.currentTimeMillis();
    
        rightsChecker.setFileRemoteCommonPointRgh(rghCopyFile);
        Thread thread = new Thread(rightsChecker);
        thread.setDaemon(true);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> submit = executorService.submit(rightsChecker);
        try {
            submit.get(10, TimeUnit.SECONDS);
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            executorService.shutdownNow();
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(rghCopyFile.exists());
        Assert.assertTrue(rghCopyFile.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));
        Assert.assertTrue(ownCopyFile.exists());
        Assert.assertTrue(ownCopyFile.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));
    
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains("app"));
    
        rightsChecker = new CommonRightsChecker(Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\20_ТД\\Внутренняя\\Профиль_Плахиной\\v.plahina\\"), logCopyPath);
        rightsChecker.setFileRemoteCommonPointRgh(rghCopyFile);
    
        Future<?> submit1 = executorService.submit(rightsChecker);
        try {
            submit1.get(10, TimeUnit.SECONDS);
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            executorService.shutdownNow();
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException | InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains("READ_DATA/WRITE_DATA/APPEND_DATA"));
    
        FileSystemWorker.appendObjectToFile(ownCopyFile, currentMillis);
        FileSystemWorker.appendObjectToFile(rghCopyFile, currentMillis);
    
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
        thread.checkAccess();
        thread.interrupt();
    }
    
    @Test
    public void testPrincipal() {
        try {
            Path file = Paths
                .get("\\\\srv-fs.eatmeat.ru\\common_new\\07_УЦП\\Внутренняя\\003.Служба складской логистики\\004.Склад готовой продукции\\Архив\\Склад 2\\Москвы карта\\Data\\Msk\\21\\12.gif");
            UserPrincipal userPrincipal = Files.getOwner(file);
            if (userPrincipal.toString().contains(ConstantsFor.STR_UNKNOWN)) {
                Files.setOwner(file, Files.getOwner(file.getRoot()));
            }
            Assert.assertFalse(userPrincipal.toString().contains(ConstantsFor.STR_UNKNOWN), userPrincipal.toString());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test(enabled = false)
    public void checkACL() {
        Path pathToCheck = Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\_IT_FAQ\\");
        
        AclFileAttributeView fileAttributeView = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        try {
            for (AclEntry aclEntry : fileAttributeView.getAcl()) {
                String aclString = pathToCheck + " = " + aclEntry.principal();
                if (aclString.contains("Unknown")) {
                    fileAttributeView.setAcl(Files.getFileAttributeView(pathToCheck.getParent(), AclFileAttributeView.class).getAcl());
                }
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
        fileAttributeView = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        try {
            for (AclEntry aclEntry : fileAttributeView.getAcl()) {
                if (aclEntry.principal().toString().contains("Unknown")) {
                    stillUnknown(pathToCheck);
                }
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void showACL() {
        Path pathToShow = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\Z01.ПАПКИ_ОБМЕНА\\Коммерция-Маркетинг_Отчеты\\аналитика ТиФ\\_ЗП\\другие сети");
        try {
            System.out.println(pathToShow.toAbsolutePath().normalize());
            for (AclEntry aclEntry : Files.getFileAttributeView(pathToShow, AclFileAttributeView.class).getAcl()) {
                System.out.println(aclEntry.principal());
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    private void delOldLogs(@NotNull File ownCopyFile, @NotNull File rghCopyFile) {
        try {
            Files.deleteIfExists(rghCopyFile.toPath().toAbsolutePath().normalize());
            Files.deleteIfExists(ownCopyFile.toPath().toAbsolutePath().normalize());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    private void stillUnknown(@NotNull Path pathToCheck) {
        int nameCount = pathToCheck.getNameCount();
        for (int i = 0; i < nameCount - 1; i++) {
            System.out.println(i + ") " + pathToCheck.getName(i));
        }
        String rootPath = pathToCheck.getRoot().toAbsolutePath().normalize().toString();
        rootPath = rootPath + pathToCheck.getName(0) + ConstantsFor.FILESYSTEM_SEPARATOR + pathToCheck.getName(1);
        
        AclFileAttributeView fileAttributeViewSuperRoot = Files.getFileAttributeView(Paths.get(rootPath), AclFileAttributeView.class);
    
        try {
            List<AclEntry> superRootAcl = fileAttributeViewSuperRoot.getAcl();
            for (AclEntry entry : superRootAcl) {
                System.out.println(rootPath + " = " + entry);
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
        try {
            for (AclEntry aclEntry : Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class).getAcl()) {
                Assert.assertFalse(aclEntry.principal().toString().contains("Unknown"), aclEntry.principal().toString());
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testRealRun() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(checker);
        executorService.shutdown();
        try {
            executorService.awaitTermination(ConstantsFor.DELAY / 2, TimeUnit.SECONDS);
            executorService.shutdownNow();
        }
        catch (InterruptedException e) {
            System.err.println(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void writtingACLs() {
        try {
            Files.getOwner(currentPath);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        showACLMainDepartmentFolder$$COPY();
    }
    
    private FileVisitResult delTrash(@NotNull Path file, String pattern) {
        if (file.toAbsolutePath().toString().toLowerCase().contains(pattern)) {
            try {
                Files.delete(file);
                System.out.println("DELETE = " + file + " " + file.toFile().exists());
            }
            catch (IOException e) {
                file.toFile().deleteOnExit();
            }
        }
        return FileVisitResult.CONTINUE;
    }
    
    private void showACLMainDepartmentFolder$$COPY() {
        String rootPlusOne = currentPath.getRoot().toAbsolutePath().normalize().toString();
        rootPlusOne += currentPath.getName(0);
        
        Path rootPath = Paths.get(rootPlusOne);
        AclFileAttributeView rootPlusOneACL = Files.getFileAttributeView(rootPath, AclFileAttributeView.class);
        AclFileAttributeView currentFileACL = Files.getFileAttributeView(currentPath, AclFileAttributeView.class);
        
        try {
            currentFileACL.getAcl().forEach(acl->messageToUser.info(rootPath.toString(), String.valueOf(acl.type()), acl.toString()));
            rootPlusOneACL.getAcl().forEach(acl->messageToUser.info(currentPath.toString(), String.valueOf(acl.type()), acl.toString()));
        }
        catch (IOException e) {
            messageToUser
                .error(MessageFormat.format("CommonRightsChecker.showACLMainDepartmentFolder threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        try {
            CommonRightsChecker rightsChecker = new CommonRightsChecker(Paths.get(".").toAbsolutePath().normalize());
            rightsChecker.setCurrentPath(currentPath);
            rightsChecker.writeACLs(Files.getOwner(currentPath), rootPlusOneACL);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}