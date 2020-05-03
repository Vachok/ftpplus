package ru.vachok.networker.ad.usermanagement;


import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;


/**
 Class ru.vachok.networker.ad.usermanagement.UserACLRestorer
 <p>

 @since 28.04.2020 (11:56) */
class UserACLRestorer extends UserACLManagerImpl implements UserACLManager {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserACLRestorer.class.getSimpleName());

    private final Map<Path, String> pathAcl = new ConcurrentHashMap<>();

    private Path startPath;

    UserACLRestorer(Path startPath) {
        super(startPath);
        this.startPath = startPath;
    }

    @Override
    public String getResult() {
        restoreFromLocal();
        return AbstractForms.fromArray(pathAcl);
    }

    private void restoreFromLocal() {
        try {
            Files.walkFileTree(startPath, this);
            Set<Map.Entry<Path, String>> entries = pathAcl.entrySet();
            for (Map.Entry<Path, String> entry : entries) {
                UserACLManager instance = UserACLManager.getInstance(UserACLManager.FILE_PARSER, entry.getKey());
                instance.setClassOption(entry.getValue());
                System.out.println(entry.getKey() + " = " + instance.getResult());
            }
        }
        catch (IOException e) {
            messageToUser.error("UserACLRestorer.restoreFromLocal", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }

    @Override
    public void setClassOption(Object classOption) {
        if (classOption instanceof Path) {
            this.startPath = (Path) classOption;
        }
        else {
            throw new IllegalArgumentException(classOption.toString());
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", UserACLRestorer.class.getSimpleName() + "[\n", "\n]")
            .add("startPath = " + startPath)
            .toString();
    }

    @Override
    public String addAccess(UserPrincipal newUser) {
        throw new UnsupportedOperationException("addAccess");
    }

    @Override
    public String removeAccess(UserPrincipal oldUser) {
        throw new UnsupportedOperationException("removeAccess");
    }

    @Override
    public String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser) {
        throw new UnsupportedOperationException("replaceUsers");
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (dir.toFile().isDirectory()) {
            String fileName = dir.normalize().toAbsolutePath().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.FILENAME_OWNER;

            String replaced = FileSystemWorker.readFile(fileName).replace("<br>", "");
            if (replaced.contains("Users:")) {
                replaced = replaced.split("Users:")[1];
            }
            pathAcl.put(dir, replaced);
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.CONTINUE;
    }
}