package ru.vachok.networker.ad.usermanagement;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 @see OwnerFixerTest
 @since 29.07.2019 (13:23) */
public class OwnerFixer extends SimpleFileVisitor<Path> implements Runnable {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, OwnerFixer.class.getSimpleName());

    private final List<String> resultsList = new ArrayList<>();

    private final Path startPath;

    public OwnerFixer(Path startPath) {
        this.startPath = startPath;
    }

    @Override
    public void run() {
        resultsList.add("Starting: " + new Date() + " ; " + toString());
        try {
            Files.walkFileTree(startPath, this);
            FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".res", resultsList);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("OwnerFixer.run: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            checkRights(dir);
        }
        return FileVisitResult.CONTINUE;
    }

    private void checkRights(Path dir) throws IOException {
        UserPrincipal owner = Files.getOwner(dir);
        if (!owner.toString().contains("BUILTIN\\Администраторы")) {
            setParentOwner(dir, owner);

        }
    }

    private void setParentOwner(Path dir, @NotNull UserPrincipal userPrincipal) {
        try {
            Path pathSetOwner = Files.setOwner(dir, Files.getOwner(dir.getRoot()));
            resultsList.add(pathSetOwner + " owner: " + Files.getOwner(dir));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("CommonRightsChecker.setParentOwner: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("resultsList", AbstractForms.fromArray(resultsList));
        jsonObject.add(ConstantsFor.JSON_PARAM_NAME_STARTPATH, startPath.normalize().toAbsolutePath().toString());
        return jsonObject.toString();
    }
}
