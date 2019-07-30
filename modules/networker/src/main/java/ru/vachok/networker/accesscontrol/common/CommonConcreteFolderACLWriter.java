package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.fsworks.FilesWorkerFactory;
import ru.vachok.networker.restapi.fsworks.UpakFiles;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.services.MyCalen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @since 22.07.2019 (11:20)
 * @see ru.vachok.networker.accesscontrol.common.CommonConcreteFolderACLWriterTest
 */
public class CommonConcreteFolderACLWriter extends FilesWorkerFactory implements Runnable {
    
    
    private String fileName;
    
    public CommonConcreteFolderACLWriter(Path currentPath) {
        this.currentPath = currentPath;
        this.fileName = ConstantsFor.FILENAME_OWNER;
    }
    
    private Path currentPath;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public CommonConcreteFolderACLWriter(Path dir, String fileName) {
        this.currentPath = dir;
        this.fileName = fileName;
    }
    
    @Override
    public UpakFiles getUpakFiles(int compLevel0to9) {
        return new UpakFiles(compLevel0to9);
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
    
    private void writeACLs(@NotNull Principal owner, @NotNull AclFileAttributeView users) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(currentPath);
        stringBuilder.append(ConstantsFor.FILESYSTEM_SEPARATOR);
        stringBuilder.append(fileName);
        String fileName = stringBuilder.toString();
        String filePathStr = currentPath.toAbsolutePath().normalize().toString();
        
        try {
            filePathStr = FileSystemWorker.writeFile(fileName, MessageFormat.format("Checked at: {2}.\nOWNER: {0}\nUsers:\n{1}",
                owner.toString(), new TForms().fromArray(users.getAcl().toArray()), LocalDateTime.now()));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonConcreteFolderACLWriter.writeACLs: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        File fileOwnerFile = new File(filePathStr);
        try {
            Files.setAttribute(Paths.get(fileOwnerFile.getAbsolutePath()), ConstantsFor.ATTRIB_HIDDEN, true);
            fileOwnerFile.setLastModified(MyCalen.getLongFromDate(26, 12, 1991, 17, 30));
            setAdminOnly(fileOwnerFile);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonConcreteFolderACLWriter.writeACLs: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private void setAdminOnly(@NotNull File fileOwnerFile) throws IOException {
        UserPrincipal domainAdmin = Files.getOwner(currentPath.getRoot());
        AclFileAttributeView attributeView = Files.getFileAttributeView(fileOwnerFile.toPath().toAbsolutePath().normalize(), AclFileAttributeView.class);
        List<AclEntry> listACL = new ArrayList<>();
        for (AclEntry aclEntry : attributeView.getAcl()) {
            if (aclEntry.principal().equals(domainAdmin) || aclEntry.principal().getName().contains("СИСТЕМА")) {
                listACL.add(aclEntry);
            }
        }
        Files.setOwner(fileOwnerFile.toPath().toAbsolutePath().normalize(), domainAdmin);
        Files.getFileAttributeView(fileOwnerFile.toPath().toAbsolutePath().normalize(), AclFileAttributeView.class).setAcl(listACL);
    }
    
    private @NotNull String isDelete() throws IOException {
        boolean isOWNFileDeleted = Files.deleteIfExists(new File(ConstantsFor.FILENAME_COMMONOWN).toPath().toAbsolutePath().normalize());
        boolean isRGHFileDeleted = Files.deleteIfExists(new File(ConstantsFor.FILENAME_COMMONRGH).toPath().toAbsolutePath().normalize());
        return new StringBuilder()
            .append("Starting a new instance of ")
            .append(getClass().getSimpleName())
            .append(" at ").append(new Date())
            .append("\ncommon.rgh and common.own deleted : ")
            .append(isRGHFileDeleted)
            .append(" ")
            .append(isOWNFileDeleted).toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonACLWriter{");
        sb.append("currentPath=").append(currentPath);
        sb.append('}');
        return sb.toString();
    }
}
