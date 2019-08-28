package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 @see ru.vachok.networker.ad.user.LocalUserResolverTest
 @since 22.08.2019 (14:14) */
class LocalUserResolver extends UserInfo {
    
    
    private Object pcName;
    
    private String userName;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private LocalUserResolver.ScanUSERSFolder scanUSERSFolder;
    
    @Override
    public int hashCode() {
        int result = pcName != null ? pcName.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (scanUSERSFolder != null ? scanUSERSFolder.hashCode() : 0);
        return result;
    }
    
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
    
        LocalUserResolver sender = (LocalUserResolver) o;
    
        if (pcName != null ? !pcName.equals(sender.pcName) : sender.pcName != null) {
            return false;
        }
        if (userName != null ? !userName.equals(sender.userName) : sender.userName != null) {
            return false;
        }
        return scanUSERSFolder != null ? scanUSERSFolder.equals(sender.scanUSERSFolder) : sender.scanUSERSFolder == null;
    }
    
    @Override
    public void setOption(Object option) {
        this.pcName = option;
    }
    
    @Override
    public String getInfoAbout(String pcName) {
        this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        StringBuilder stringBuilder = new StringBuilder();
        for (String usersFile : getPCLogins(String.valueOf(this.pcName), 1)) {
            String appendTo = parseList(usersFile);
            stringBuilder.append(appendTo);
        }
        if (stringBuilder.length() == 0) {
            return new UnknownUser(this.toString()).getInfoAbout(pcName);
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return new UnknownUser(this.toString()).getInfo();
        }
        List<String> pcLogins = getPCLogins((String) pcName, 1);
        String retStr;
        try {
            retStr = MessageFormat.format("{0} : {1}", pcLogins.get(0), pcName);
        }
        catch (IndexOutOfBoundsException e) {
            return new UnknownUser(this.toString()).getInfo();
        }
        return retStr;
    }
    
    @Override
    public List<String> getPCLogins(String pcName, int resultsLimit) {
        this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        this.scanUSERSFolder = new LocalUserResolver.ScanUSERSFolder((String) this.pcName);
        scanUSERSFolder.call();
        List<String> timePath = new ArrayList<>(scanUSERSFolder.getTimePath());
        Collections.reverse(timePath);
        return timePath.stream().limit(resultsLimit).collect(Collectors.toList());
        
    }
    
    private @NotNull String parseList(@NotNull String name) {
        String[] splitNamePC = name.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Path path = Paths.get(splitNamePC[1]);
            if (path.toFile().isDirectory()) {
                this.userName = path.getFileName().toString();
                stringBuilder.append(pcName).append(" : ").append(userName).append("\n");
            }
            if (path.toFile().isFile()) {
                this.userName = path.getParent().getFileName().toString();
                stringBuilder.append(pcName).append(" : ").append(userName).append("\n");
            }
        }
        catch (IndexOutOfBoundsException e) {
            this.userName = e.getMessage();
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", LocalUserResolver.class.getSimpleName() + "[\n", "\n]")
            .add("pcName = " + pcName)
            .add("userName = '" + userName + "'")
            .add("scanUSERSFolder = " + scanUSERSFolder)
            .toString();
    }
    


    private static class ScanUSERSFolder extends SimpleFileVisitor<Path> implements Callable<String> {
        
        
        private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
        
        /**
         new {@link ArrayList}, список файлов, с отметками {@link File#lastModified()}
         
         @see #visitFile(Path, BasicFileAttributes)
         */
        private List<String> timePath = new ArrayList<>();
        
        private String pcName;
        
        private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
        
        private String pathAsStr;
        
        ScanUSERSFolder(@NotNull String pcName) {
            this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName) + ConstantsFor.DOMAIN_EATMEATRU;
            this.pathAsStr = "\\\\" + this.pcName + "\\c$\\Users";
        }
        
        @Override
        public String call() {
            if (pcName.contains("Unknown")) {
                return "Bad PC: " + this.toString();
            }
            walkUsersFolderIfPCOnline();
            return writeNamesToTMPFile();
        }
        
        private String walkUsersFolderIfPCOnline() {
            try {
                Files.walkFileTree(Paths.get(pathAsStr), Collections.singleton(FOLLOW_LINKS), 1, this);
                return timePath.get(timePath.size() - 1);
            }
            catch (IOException | IndexOutOfBoundsException e) {
                return e.getMessage() + " " + getClass().getSimpleName() + ".getLastTimeUse";
            }
        }
        
        private String writeNamesToTMPFile() {
            File[] files;
            File pcNameFile = new File("null");
            try {
                pcNameFile = Files.createTempFile(this.getClass().getSimpleName(), ".tmp").toFile();
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat.format("ScanUSERSFolder.writeNamesToTMPFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
            
            pcNameFile.deleteOnExit();
            return pcNameFile.toPath().toAbsolutePath().normalize().toString();
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
            boolean isBadName = file.toString().toLowerCase().contains("default") || file.getFileName().toString().toLowerCase().contains("public") || file
                .toString().toLowerCase().contains("temp");
            if (!isBadName) {
                if (attrs.isDirectory()) {
                    long lastAccess = attrs.lastAccessTime().toMillis();
                    timePath.add(lastAccess + " " + file.toAbsolutePath().normalize() + " " + new Date(lastAccess) + " " + lastAccess);
                }
                if (attrs.isRegularFile()) {
                    long lastAccess = attrs.lastAccessTime().toMillis();
                    timePath.add(lastAccess + " " + file.toAbsolutePath().normalize() + " " + new Date(lastAccess) + " " + lastAccess);
                }
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file, @NotNull IOException exc) {
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
        
        @Contract(pure = true)
        List<String> getTimePath() {
            Collections.sort(timePath);
            return timePath;
        }
        
    }
}
