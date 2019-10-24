package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 @see ru.vachok.networker.ad.user.LocalUserResolverTest
 @since 22.08.2019 (14:14) */
class LocalUserResolver extends UserInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, LocalUserResolver.class.getSimpleName());
    
    private Object pcName;
    
    private String userName;
    
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
    public List<String> getLogins(String pcName, int resultsLimit) {
        pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        this.scanUSERSFolder = new LocalUserResolver.ScanUSERSFolder(pcName);
        
        try {
            ThreadPoolTaskExecutor taskExecutor = AppComponents.threadConfig().getTaskExecutor();
            Future<String> stringFuture = taskExecutor.submit(scanUSERSFolder);
            String futureString = stringFuture.get(5, TimeUnit.SECONDS);
            messageToUser.info(MessageFormat.format("{1} futureString = {0}", futureString, stringFuture.isDone()));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            messageToUser.error(MessageFormat.format("LocalUserResolver.getPCLogins {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
            messageToUser.warn(new UnknownUser(this.getClass().getSimpleName()).getInfoAbout(pcName));
        }
        
        List<String> timePath = new ArrayList<>(scanUSERSFolder.getTimePath());
        Collections.reverse(timePath);
        return timePath.stream().limit(resultsLimit).collect(Collectors.toList());
    }
    
    @Override
    public String getInfoAbout(String pcName) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String usersFile : getLogins(pcName, 1)) {
            String appendTo = parseList(usersFile);
            stringBuilder.append(appendTo);
        }
        if (stringBuilder.length() == 0) {
            return new ResolveUserInDataBase(this.toString()).getInfoAbout(pcName);
        }
        return stringBuilder.toString();
    }
    
    @Override
    public void setClassOption(Object option) {
        this.pcName = option;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", LocalUserResolver.class.getSimpleName() + "[\n", "\n]")
            .add("pcName = " + pcName)
            .add("userName = '" + userName + "'")
            .add("scanUSERSFolder = " + scanUSERSFolder)
            .toString();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return new UnknownUser(this.toString()).getInfo();
        }
        List<String> pcLogins = getLogins((String) pcName, 1);
        String retStr;
        try {
            this.userName = Paths.get(pcLogins.get(0).split(" ")[1]).getFileName().toString();
            if (((String) pcName).matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
                pcName = PCInfo.getInstance(String.valueOf(pcName)).getInfo();
            }
            retStr = pcName + " : " + userName;
        }
        catch (IndexOutOfBoundsException e) {
            retStr = resolvePCUserOverDB((String) pcName);
        }
        return retStr;
    }
    


    private static class ScanUSERSFolder extends SimpleFileVisitor<Path> implements Callable<String> {
        
        
        private static final MessageToUser messageToUser = MessageToUser
            .getInstance(MessageToUser.LOCAL_CONSOLE, LocalUserResolver.ScanUSERSFolder.class.getSimpleName());
        
        /**
         new {@link ArrayList}, список файлов, с отметками {@link File#lastModified()}
         
         @see #visitFile(Path, BasicFileAttributes)
         */
        private List<String> timePath = new ArrayList<>();
        
        private String pcName;
        
        private String pathAsStr;
        
        @Contract(pure = true)
        List<String> getTimePath() {
            Collections.sort(timePath);
            return timePath;
        }
        
        ScanUSERSFolder(@NotNull String pcName) {
            this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName) + ConstantsFor.DOMAIN_EATMEATRU;
            this.pathAsStr = "\\\\" + this.pcName + "\\c$\\Users";
        }
        
        @Override
        public String call() {
            if (pcName.contains(ConstantsFor.STR_UNKNOWN)) {
                return "Bad PC: " + this.toString();
            }
            walkUsersFolderIfPCOnline();
            return writeNamesToTMPFile();
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("WalkerToUserFolder{");
            sb.append("timePath=").append(timePath);
            sb.append('}');
            return sb.toString();
        }
        
        private void walkUsersFolderIfPCOnline() {
            try {
                Files.walkFileTree(Paths.get(pathAsStr), Collections.singleton(FOLLOW_LINKS), 2, this);
            }
            catch (IOException | IndexOutOfBoundsException e) {
                messageToUser.error(e.getMessage() + " see line: 114");
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
            boolean isBadName = checkName(dir);
            if (!isBadName) {
                addToList(dir, attrs);
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
            boolean isBadName = checkName(file);
            if (!isBadName) {
                addToList(file, attrs);
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
    
        private boolean checkName(@NotNull Path path) {
            boolean notFileOrDir = !path.toFile().isFile() | !path.toFile().isDirectory();
            boolean isNameBad = path.toString().toLowerCase().contains("default") || path.getFileName().toString().toLowerCase().contains("public") || path
                    .toString().toLowerCase().contains("temp") || path.toString().contains("дминистр") || path.toString().contains("льзовател")
                    || path.toString().contains("ocadm") || path.getFileName().toString().contains("sers") || path.toString().contains("All") || path.toString().contains("Все ");
            return notFileOrDir & isNameBad;
        
        }
    
        private void addToList(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
            long lastModStamp = attrs.lastModifiedTime().toMillis();
            long lastAccessStamp = attrs.lastAccessTime().toMillis();
            if (lastAccessStamp > lastModStamp) {
                lastModStamp = lastAccessStamp;
            }
            timePath.add(lastModStamp + " " + file.toAbsolutePath().normalize().getParent() + " " + new Date(lastModStamp) + " " + lastModStamp);
        }
        
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
    
    
}
