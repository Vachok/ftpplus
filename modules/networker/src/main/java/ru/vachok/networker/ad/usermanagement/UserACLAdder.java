package ru.vachok.networker.ad.usermanagement;


import com.google.gson.JsonObject;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 @see UserACLAdderTest
 @since 25.07.2019 (13:27) */
class UserACLAdder extends UserACLManagerImpl {


    private UserPrincipal newUser;

    private int foldersCounter;

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserACLAdder.class.getSimpleName());

    private int filesCounter;

    private List<AclEntry> neededACLs;

    private String rights;

    private Path startPath;

    @Override
    public String getResult() {
        return AbstractForms.fromArray(neededACLs);
    }
    
    @Override
    public String toString() {
        final JsonObject jo = new JsonObject();
        jo.addProperty(ConstantsFor.JSONNAME_CLASS, "UserACLAdder");
        jo.addProperty(ConstantsFor.JSON_PARAM_NAME_STARTPATH, startPath.toString());
        jo.addProperty(", rights", rights);
        jo.addProperty(", newUser", String.valueOf(newUser));
        jo.addProperty(", neededACLs", AbstractForms.fromArray(neededACLs));
        jo.addProperty(", foldersCounter", foldersCounter);
        jo.addProperty(", filesCounter", filesCounter);
        return jo.toString();
    }
    
    UserACLAdder(Path path, UserPrincipal newUser, String rights) {
        this(path);
        this.rights = rights;
        this.newUser = newUser;
    }

    private UserACLAdder(UserPrincipal newUser) {
        super(Paths.get(ModelAttributeNames.COMMON).toAbsolutePath().normalize());
        this.newUser = newUser;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        foldersCounter++;
        try {
            createACLs(dir);
            List<AclEntry> tipEnters = new ArrayList<>(neededACLs);
            Files.getFileAttributeView(dir, AclFileAttributeView.class).setAcl(tipEnters);
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonAdder.preVisitDirectory: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return FileVisitResult.CONTINUE;
        }
    }

    UserACLAdder(Path path, String rights) {
        super(path);
        try {
            this.newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format(ConstantsFor.ACLADDER_0_1, e.getMessage(), e.getClass().getName()));
        }
        finally {
            this.rights = rights;
        }
    }

    UserACLAdder(Path startPath) {
        super(startPath);
        try {
            this.newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format(ConstantsFor.ACLADDER_0_1, e.getMessage(), e.getClass().getName()));
        }
        finally {
            this.rights = "rw";
        }
    }

    @Override
    public void setClassOption(Object classOption) {
        this.newUser = (UserPrincipal) classOption;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        messageToUser.warn(UserACLAdder.class.getSimpleName(), exc.getMessage(), " see line: 118 ***");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("add = " + dir);
        System.out.println("newUser = " + newUser);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        filesCounter++;
        try {
            createACLs(file);
            List<AclEntry> tipEnters = new ArrayList<>(neededACLs);
            Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(tipEnters);
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonAdder.visitFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return FileVisitResult.CONTINUE;
        }
    }

    private void createACLs(Path dir) throws IOException {
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
        List<AclEntry> currentACLs = aclFileAttributeView.getAcl();
        AclEntry addAcl = createACLFor(newUser, rights);
        currentACLs.add(addAcl);
        this.neededACLs = currentACLs;
    }
}
