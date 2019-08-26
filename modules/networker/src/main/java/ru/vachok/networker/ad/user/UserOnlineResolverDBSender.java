package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 @see ru.vachok.networker.ad.user.UserOnlineResolverDBSenderTest
 @since 22.08.2019 (14:14) */
class UserOnlineResolverDBSender extends UserInfo {
    
    
    private Object classOption;
    
    private String forADUser;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private UserOnlineResolverDBSender.WalkerToUserFolder walkerToUserFolder;
    
    
    
    private static class WalkerToUserFolder extends SimpleFileVisitor<Path> implements Callable<String> {
        
        
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
    
    @Override
    public void setClassOption(Object classOption) {
        this.classOption = classOption;
    }
    
    @Override
    public List<String> getPCLogins(String pcName, int resultsLimit) {
        this.classOption = pcName;
        
        this.walkerToUserFolder = new UserOnlineResolverDBSender.WalkerToUserFolder(pcName);
        StringBuilder pathBuilder = new StringBuilder();
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        
        try {
            pathBuilder.append("\\\\").append(pcName).append(ConstantsFor.DOMAIN_EATMEATRU).append("\\c$\\users\\");
            if (new NameOrIPChecker(pcName).isLocalAddress()) {
                Files.walkFileTree(Paths.get(pathBuilder.toString()), Collections.singleton(FileVisitOption.FOLLOW_LINKS), 1, walkerToUserFolder);
                List<String> timePath = walkerToUserFolder.getTimePath();
                Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(()->sendUserToDB(timePath));
            }
        }
        catch (ArrayIndexOutOfBoundsException | IOException e) {
            messageToUser.error(MessageFormat.format("ADUserResolver.getPossibleVariantsOfUser {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
        }
        return walkerToUserFolder.getTimePath();
    }
    
    private void sendUserToDB(List<String> tP) {
        Collections.sort(tP);
        try {
            String lastUser = tP.get(tP.size() - 1);
            String pcName = (String) classOption;
            if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
                pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
            }
            PCInfo.autoResolvedUsersRecord(pcName, lastUser);
        }
        catch (IndexOutOfBoundsException ignore) {
            //
        }
    }
    
    @Override
    public String getInfo() {
        String retStr = "null";
        try {
            retStr = MessageFormat.format("For user {1}, resolved pc (LIMIT 20) :\n{0} ", getInfoAbout((String) this.classOption), this.forADUser);
        }
        catch (IndexOutOfBoundsException e) {
            messageToUser.error(e.getMessage() + " see line: 92");
        }
        return retStr;
    }
    
    @Override
    public String getInfoAbout(String pcName) {
        this.classOption = pcName;
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : getUserLogins(pcName, 20)) {
            stringBuilder.append(parseList(name));
        }
        return stringBuilder.toString();
    }
    
    @Override
    public List<String> getUserLogins(String pcName, int resultsLimit) {
        this.classOption = pcName;
        this.walkerToUserFolder = new UserOnlineResolverDBSender.WalkerToUserFolder(pcName);
        this.classOption = walkerToUserFolder.call();
        return walkerToUserFolder.getTimePath();
    }
    
    private @NotNull String parseList(@NotNull String name) {
        String[] splitNamePC = name.split(".eatmeat.ru : ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(splitNamePC[0]).append("\n");
        try {
            this.forADUser = splitNamePC[1].replaceFirst("\\Q\\\\E", "").split("\\Q\\\\E")[0];
            
        }
        catch (IndexOutOfBoundsException e) {
            this.forADUser = e.getMessage();
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UserOnlineResolverDBSender.class.getSimpleName() + "[\n", "\n]")
                .add("classOption = " + classOption)
                .add("resolvedPC = '" + forADUser + "'")
                .add("walkerToUserFolder = " + !(walkerToUserFolder == null))
                .toString();
    }
    
    
}
