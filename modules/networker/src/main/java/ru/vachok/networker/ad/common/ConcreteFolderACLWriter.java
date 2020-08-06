// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.Principal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 @see ConcreteFolderACLWriterTest
 @since 22.07.2019 (11:20) */
class ConcreteFolderACLWriter implements Runnable {


    private final String fileName;

    private final Path currentPath;

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ConcreteFolderACLWriter.class.getSimpleName());

    private final long size;

    public ConcreteFolderACLWriter(Path currentPath) {
        this.currentPath = currentPath;
        this.fileName = FileNames.FILENAME_OWNER;
        size = 0;
    }

    ConcreteFolderACLWriter(Path dir, long size) {
        this.currentPath = dir;
        this.size = size;
        this.fileName = FileNames.FILENAME_OWNER;
    }

    @Override
    public void run() {
        try {
            UserPrincipal owner = Files.getOwner(currentPath);
            AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(currentPath, AclFileAttributeView.class);
            writeACLs(owner, aclFileAttributeView);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonConcreteFolderACLWriter.run: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, getClass().getSimpleName());
        jsonObject.add(PropertiesNames.HASH, this.hashCode());
        jsonObject.add(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
        jsonObject.add(PropertiesNames.FILENAME, fileName);
        jsonObject.add("currentPath", currentPath.toString());
        jsonObject.add("size", size);
        return jsonObject.toString();
    }

    private void writeACLs(@NotNull Principal owner, @NotNull AclFileAttributeView users) {
        String fileName = buildFileNAme();
        String filePathStr = currentPath.toAbsolutePath().normalize().toString();
        try {
            filePathStr = FileSystemWorker
                .writeFile(fileName, MessageFormat.format("Checked at: {0} size ({1} meg).\nOWNER: {2}\nUsers:\n{3}", LocalDateTime.now(), size / ConstantsFor.MBYTE,
                    owner.toString(), AbstractForms.fromArray(users.getAcl().toArray())));
        }
        catch (IOException e) {
            messageToUser.warn(ConcreteFolderACLWriter.class.getSimpleName(), e.getMessage(), " see line: 82 ***");
        }
        finally {
            if (!filePathStr.contentEquals(String.valueOf(false))) {
                assignAttr(filePathStr);
            }
        }
    }

    private @NotNull String buildFileNAme() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(currentPath);
        stringBuilder.append(ConstantsFor.FILESYSTEM_SEPARATOR);
        stringBuilder.append(fileName);
        return stringBuilder.toString();
    }

    private void assignAttr(String filePathStr) {
        final File fileOwnerFile = new File(filePathStr);
        try {
            Files.setAttribute(Paths.get(fileOwnerFile.getAbsolutePath()), ConstantsFor.ATTRIB_HIDDEN, true);
            fileOwnerFile.setLastModified(MyCalen.getLongFromDate(26, 12, 1991, 17, 30));
        }
        catch (IOException e) {
            messageToUser.warn(ConcreteFolderACLWriter.class.getSimpleName(), e.getMessage(), " see line: 93 ***");
        }
    }

    private void setAdminOnly(@NotNull File fileOwnerFile) {
        UserPrincipal domainAdmin = null;
        try {
            domainAdmin = Files.getOwner(currentPath.getRoot());
        }
        catch (IOException e) {
            messageToUser.warn(ConcreteFolderACLWriter.class.getSimpleName(), e.getMessage(), " see line: 111 ***");
        }
        AclFileAttributeView attributeView = Files.getFileAttributeView(fileOwnerFile.toPath().getRoot().toAbsolutePath().normalize(), AclFileAttributeView.class);
        List<AclEntry> listACL = new ArrayList<>();
        try {
            for (AclEntry aclEntry : attributeView.getAcl()) {
                if (aclEntry.principal().equals(domainAdmin) || aclEntry.principal().getName().contains("СИСТЕМА") || aclEntry.principal().getName()
                    .contains("SYSTEM")) {
                    listACL.add(aclEntry);
                }
            }
            if (domainAdmin != null) {
                Files.setOwner(fileOwnerFile.toPath().toAbsolutePath().normalize(), domainAdmin);
            }
            Files.getFileAttributeView(fileOwnerFile.toPath().toAbsolutePath().normalize(), AclFileAttributeView.class).setAcl(listACL);
        }
        catch (IOException e) {
            messageToUser.warn(ConcreteFolderACLWriter.class.getSimpleName(), e.getMessage(), " see line: 128 ***");
        }
    }
}
