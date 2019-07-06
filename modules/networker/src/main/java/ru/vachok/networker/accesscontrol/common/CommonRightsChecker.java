// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest */
public class CommonRightsChecker extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final String STR_FILES_IS_NOT_EXISTS = " is not exists!";
    
    private static final String STR_KILOBYTES = " kilobytes";
    
    private final File commonOwn = new File(ConstantsFor.FILENAME_COMMONOWN);
    
    private final File commonRgh = new File(ConstantsFor.FILENAME_COMMONRGH);
    
    private File commonRghFile = new File("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя\\" + commonRgh.getName());
    
    /**
     @see ru.vachok.networker.accesscontrol.common.CommonRightsCheckerTest#testRun()
     */
    @Override public void run() {
        try {
            if (toCheckPath != null) {
                Files.walkFileTree(toCheckPath, this);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("copyExistsFiles() = " + copyExistsFiles());
        FileSystemWorker.appendObjectToFile(new File(getClass().getSimpleName() + ".res"), countFiles + " files, " + countDirs + " dirs\nAt: " + new Date());
        CommonRightsParsing commonRightsParsing = new CommonRightsParsing(toCheckPath, commonRghFile);
        commonRightsParsing.rightsWriterToFolderACL();
    }
    
    long countFiles;
    
    long countDirs;
    
    private Path toCheckPath;
    
    private Path logsCopyPath;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    public CommonRightsChecker(Path toCheckPath, @NotNull Path logsCopyPath) {
        this.toCheckPath = toCheckPath;
        this.logsCopyPath = logsCopyPath;
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonRightsChecker{");
        sb.append("commonOwn=").append(commonOwn);
        sb.append(", commonRgh=").append(commonRgh);
        sb.append(", countFiles=").append(countFiles);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        FileVisitResult fileVisitResult = FileVisitResult.CONTINUE;
        List<String> delPatterns = delPatterngList();
        for (String pattern : delPatterns) {
            fileVisitResult = delTrash(file, pattern.toLowerCase());
        }
        if (file.toFile().exists() && attrs.isRegularFile()) {
            checkRights(file);
            this.countFiles++;
        }
        return fileVisitResult;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            checkRights(dir);
            this.countDirs++;
        }
        return FileVisitResult.CONTINUE;
    }
    
    void setCommonRghFile(File commonRghFile) {
        this.commonRghFile = commonRghFile;
    }
    
    private FileVisitResult delTrash(Path file, String pattern) {
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
    
    private List<String> delPatterngList() {
        List<String> retList = new ArrayList<>();
        try (InputStream inputStream = getClass().getResourceAsStream("/static/delfromcommon.txt");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(retList::add);
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".delPatterngList");
        }
        return retList;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        StringBuilder stringBuilder = new StringBuilder()
            .append("Dir visited = ")
            .append(dir).append("\n")
            .append(countDirs).append(" total directories scanned; total files scanned: ").append(countFiles).append("\n");
        if (exc != null) {
            stringBuilder.append(new TForms().fromArray(exc, false));
        }
        System.out.println(stringBuilder);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    private void checkRights(Path pathToCheck) throws IOException {
        AclFileAttributeView fileAttributeView = Files.getFileAttributeView(pathToCheck, AclFileAttributeView.class);
        UserPrincipal userPrincipal = Files.getOwner(pathToCheck);
    
        if (userPrincipal.toString().contains(ConstantsFor.STR_UNKNOWN)) {
            Files.setOwner(pathToCheck, Files.getOwner(pathToCheck.getRoot()));
            userPrincipal = Files.getOwner(pathToCheck);
        }
    
        FileSystemWorker.appendObjectToFile(commonOwn, pathToCheck.toAbsolutePath().normalize() + ConstantsFor.STR_OWNEDBY + userPrincipal);
        FileSystemWorker.appendObjectToFile(commonRgh, pathToCheck.toAbsolutePath().normalize() + " | ACL: " + Arrays.toString(fileAttributeView.getAcl().toArray()));
    }
    
    private String isDelete() throws IOException {
        boolean isOWNFileDeleted = Files.deleteIfExists(commonOwn.toPath().toAbsolutePath().normalize());
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
    
    private boolean copyExistsFiles() {
        Path cRGHCopyPath = Paths.get(logsCopyPath.toAbsolutePath().normalize() + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + commonRgh.getName());
        Path cOWNCopyPath = Paths.get(logsCopyPath.toAbsolutePath().normalize() + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + commonOwn.getName());
    
        if (copyDoesNotNeed(cOWNCopyPath, cRGHCopyPath)) {
            throw new IllegalInvokeEx(getClass().getTypeName() + " copy files with common rights does not need");
        }
        else {
            boolean isOWNCopied = true;
            boolean isRGHCopied = true;
        
            if (commonOwn.exists()) {
                isOWNCopied = FileSystemWorker.copyOrDelFile(commonOwn, cOWNCopyPath, true);
            }
            if (commonRgh.exists()) {
                isRGHCopied = FileSystemWorker.copyOrDelFile(commonRgh, cRGHCopyPath, true);
            }
        
            return isOWNCopied & isRGHCopied;
        }
    }
    
    private boolean copyDoesNotNeed(Path cOWNCopyPath, Path cRGHCopyPath) {
        return (cOWNCopyPath.toFile().lastModified() > commonOwn.lastModified()) & (cRGHCopyPath.toFile().lastModified() > commonRgh.lastModified());
    }
}
