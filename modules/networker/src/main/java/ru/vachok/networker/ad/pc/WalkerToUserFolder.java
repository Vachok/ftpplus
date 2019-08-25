package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanService;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 @see ru.vachok.networker.ad.pc.WalkerToUserFolderTest
 @since 22.11.2018 (14:46) */
public class WalkerToUserFolder extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
    
    /**
     new {@link ArrayList}, список файлов, с отметками {@link File#lastModified()}
     
     @see #visitFile(Path, BasicFileAttributes)
     */
    private final List<String> timePath = new ArrayList<>();
    
    private String lastUsersDirFileUsedName;
    
    private String pcName;
    
    private File tmpFile;
    
    public WalkerToUserFolder(String pcName) {
        this.pcName = pcName;
        this.tmpFile = new File(pcName + ".log");
    }
    
    @Contract(pure = true)
    public List<String> getTimePath() {
        return timePath;
    }
    
    @Override
    public String call() {
        if (!(NetScanService.isReach(pcName)) || (!new NameOrIPChecker(pcName).isLocalAddress())) {
            return MessageFormat.format("{0} NO PING PC name: {1}", this.getClass().getSimpleName(), pcName);
        }
        else {
            return startWalk();
        }
    }
    
    private String startWalk() {
        return writeNamesToTMPFile();
    }
    
    private String writeNamesToTMPFile() {
        File[] files;
        File pcNameFile = new File("null");
        try {
            pcNameFile = Files.createTempFile(this.getClass().getSimpleName(), ".tmp").toFile();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        
        try (OutputStream outputStream = new FileOutputStream(pcNameFile)) {
            try (PrintWriter writer = new PrintWriter(outputStream, true)) {
                String pathAsStr = new StringBuilder().append("\\\\").append(pcName).append("\\c$\\Users").toString();
                String walkFolders = walkUsersFolderIfPCOnline(pathAsStr);
                lastUsersDirFileUsedName = walkFolders.split("Users")[1];
                files = new File(pathAsStr).listFiles();
                String userNamedFile = PATTERN.matcher(Arrays.toString(files)).replaceAll(Matcher.quoteReplacement("\n"));
                writer
                    .append(userNamedFile)
                    .append("\n\n\n")
                    .append(lastUsersDirFileUsedName);
            }
        }
        catch (IOException | ArrayIndexOutOfBoundsException ignored) {
            //
        }
        pcNameFile.deleteOnExit();
        return pcNameFile.toPath().toAbsolutePath().normalize().toString();
    }
    
    private String walkUsersFolderIfPCOnline(String pathAsStr) {
        try {
            Files.walkFileTree(Paths.get(pathAsStr), Collections.singleton(FOLLOW_LINKS), 2, this);
            Collections.sort(timePath);
            return timePath.get(timePath.size() - 1);
        }
        catch (IOException | IndexOutOfBoundsException e) {
            return e.getMessage() + " " + getClass().getSimpleName() + ".getLastTimeUse";
        }
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        FileSystemWorker.appendObjectToFile(tmpFile, dir);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        timePath.add(file.toFile().lastModified() + " " + file + " " + new Date(file.toFile().lastModified()) + " " + file.toFile().lastModified());
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc.getMessage());
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WalkerToUserFolder{");
        sb.append("timePath=").append(timePath);
        sb.append('}');
        return sb.toString();
    }
    
}