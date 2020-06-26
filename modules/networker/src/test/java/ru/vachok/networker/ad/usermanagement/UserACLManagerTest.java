package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.*;


/**
 @see UserACLManager
 @since 26.08.2019 (15:52) */
public class UserACLManagerTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserACLManager.class
        .getSimpleName(), System.nanoTime());

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @Test
    public void testCreateACLForUserFromExistsACL() {
        try {
            UserPrincipal owner = Files.getOwner(Paths.get("."));
            AclFileAttributeView attributeView = Files.getFileAttributeView(Paths.get("."), AclFileAttributeView.class);
            AclEntry existsACL = UserACLManagerImpl.createACLForUserFromExistsACL(attributeView.getAcl().get(0), owner);
            Assert.assertFalse(existsACL.equals(attributeView.getAcl().get(0)), existsACL.toString());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    public void testSetACLToAdminsOnly() {
        File admOnlyTestFile = new File("testSetACLToAdminsOnly");
        try (OutputStream fileOutputStream = new FileOutputStream(admOnlyTestFile.getName())) {
            fileOutputStream.write("test".getBytes());
            UserACLManagerImpl.setACLToAdminsOnly(admOnlyTestFile.toPath().toAbsolutePath().normalize());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    public void testGetI() {
        UserACLManager parsingACL = UserACLManager.getInstance(UserACLManager.ACL_PARSING, Paths.get("."));
        String toStr = parsingACL.toString();
        Assert.assertTrue(toStr.contains("ACLParser{"), toStr);
    }

    @Test
    public void testRestoreACLs() {
        UserACLManager parsingACL = UserACLManager
            .getInstance(UserACLManager.RESTORE, Paths.get("\\\\srv-fs\\Common_new\\Z01.ПАПКИ_ОБМЕНА\\Коммерция-Маркетинг_Отчеты\\аналитика ТиФ\\_ЗП\\"));
        String result = parsingACL.getResult();
        Assert.assertTrue(result.contains("Генеральная группа") || result.contains("004.Коммерческая служба.запись"), result);
    }
}