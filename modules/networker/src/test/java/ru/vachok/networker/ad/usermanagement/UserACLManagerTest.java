package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;


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
            AclEntry existsACL = UserACLManager.createACLForUserFromExistsACL(attributeView.getAcl().get(0), owner);
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
            UserACLManager.setACLToAdminsOnly(admOnlyTestFile.toPath().toAbsolutePath().normalize());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testCreateNewACL() {
        UserPrincipal owner = null;
        Path pathToTest = Paths.get("\\\\rups00.eatmeat.ru\\c$\\Users\\ikudryashov\\");
        try {
            owner = Files.getOwner(pathToTest);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        AclEntry acl = UserACLManager.createNewACL(owner);
        AclFileAttributeView attrACL = Files.getFileAttributeView(pathToTest, AclFileAttributeView.class);
        System.out.println("acl = " + acl);
        try {
            List<AclEntry> aclPathList = attrACL.getAcl();
            for (AclEntry entry : aclPathList) {
                if (entry.toString().contains("NT AUTHORITY\\SYSTEM")) {
                    System.out.println();
                    System.out.println("entry = " + entry);
                    throw new TODOException("29.08.2019 (22:23)");
                }
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetI() {
        UserACLManager parsingACL = UserACLManager.getI(UserACLManager.ACL_PARSING, Paths.get("."));
        String toStr = parsingACL.toString();
        Assert.assertTrue(toStr.contains("ACLParser["), toStr);
    }
}