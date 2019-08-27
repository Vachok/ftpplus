package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 @see ru.vachok.networker.ad.user.LocalUserResolverDBSenderTest
 @since 22.08.2019 (14:14) */
class LocalUserResolverDBSender extends UserInfo {
    
    
    private Object classOption;
    
    private String forADUser;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private LocalUserResolverDBSender.WalkerToUserFolder walkerToUserFolder;
    
    @Override
    public int hashCode() {
        int result = classOption != null ? classOption.hashCode() : 0;
        result = 31 * result + (forADUser != null ? forADUser.hashCode() : 0);
        result = 31 * result + (walkerToUserFolder != null ? walkerToUserFolder.hashCode() : 0);
        return result;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.classOption = classOption;
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
        
        LocalUserResolverDBSender sender = (LocalUserResolverDBSender) o;
        
        if (classOption != null ? !classOption.equals(sender.classOption) : sender.classOption != null) {
            return false;
        }
        if (forADUser != null ? !forADUser.equals(sender.forADUser) : sender.forADUser != null) {
            return false;
        }
        return walkerToUserFolder != null ? walkerToUserFolder.equals(sender.walkerToUserFolder) : sender.walkerToUserFolder == null;
    }
    
    @Override
    public String getInfo() {
        List<String> pcLogins = getPCLogins((String) classOption, 10);
        StringBuilder retBuilder = new StringBuilder();
        try {
            pcLogins.stream().sorted().forEach(pcLogin->retBuilder.append(Paths.get(pcLogin.split(" ")[1]).getFileName().toString()).append(" "));
        }
        catch (IndexOutOfBoundsException e) {
            retBuilder.append(new ResolveUserInDataBase((String) classOption).getInfo());
        }
        return MessageFormat.format("{0} : {1}", retBuilder.toString(), classOption);
    }
    
    @Override
    public List<String> getPCLogins(String pcName, int resultsLimit) {
        this.classOption = pcName;
        this.walkerToUserFolder = new LocalUserResolverDBSender.WalkerToUserFolder(pcName);
        walkerToUserFolder.call();
        List<String> sortedTimePath = new ArrayList<>(walkerToUserFolder.getTimePath());
        Collections.sort(sortedTimePath);
        List<String> timePath = sortedTimePath.stream().sorted().limit(resultsLimit).collect(Collectors.toList());
        sendUserToDB(timePath);
        if (timePath.size() > 0) {
            return timePath;
        }
        else {
            return new ResolveUserInDataBase(pcName).getPCLogins(pcName, resultsLimit);
        }
    }
    
    private void sendUserToDB(@NotNull List<String> tP) {
        try {
            String lastUser = tP.get(tP.size() - 1);
            String pcName = (String) classOption;
            if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
                pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
            }
            UserInfo.autoResolvedUsersRecord(pcName, lastUser);
        }
        catch (IndexOutOfBoundsException ignore) {
            //
        }
    }
    


    private static class WalkerToUserFolder extends SimpleFileVisitor<Path> implements Callable<String> {
        
        
        private static final Pattern PATTERN = Pattern.compile(", ", Pattern.LITERAL);
        
        /**
         new {@link ArrayList}, список файлов, с отметками {@link File#lastModified()}
         
         @see #visitFile(Path, BasicFileAttributes)
         */
        private List<String> timePath = new ArrayList<>();
        
        private String pcName;
        
        WalkerToUserFolder(@NotNull String pcName) {
            if (pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
                this.pcName = pcName;
            }
            else {
                this.pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
            }
            File tmpFile = new File(pcName + ".log");
        }
        
        @Override
        public String call() {
            if (!(NetScanService.isReach(pcName)) || (!new NameOrIPChecker(pcName).isLocalAddress())) {
                return new ResolveUserInDataBase(pcName).getInfo();
            }
            else {
                return writeNamesToTMPFile();
            }
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
                    String lastUsersDirFileUsedName = walkFolders.split("Users")[1];
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
                Files.walkFileTree(Paths.get(pathAsStr), Collections.singleton(FOLLOW_LINKS), 1, this);
                Collections.sort(timePath);
                return timePath.get(timePath.size() - 1);
            }
            catch (IOException | IndexOutOfBoundsException e) {
                return e.getMessage() + " " + getClass().getSimpleName() + ".getLastTimeUse";
            }
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
            if (attrs.isDirectory() && !file.getFileName().toString().toLowerCase().contains("default") && !file.getFileName().toString().toLowerCase()
                .contains("public")) {
                long lastAccess = attrs.lastModifiedTime().toMillis();
                timePath.add(lastAccess + " " + file.toAbsolutePath().normalize() + " " + new Date(lastAccess) + " " + file.toFile().lastModified());
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
            return timePath;
        }
        
    }
    
    @Override
    public String getInfoAbout(String pcName) {
        this.classOption = pcName;
        StringBuilder stringBuilder = new StringBuilder();
        for (String usersFile : getPCLogins(pcName, 20)) {
            String appendTo = parseList(usersFile);
            stringBuilder.append(appendTo);
        }
        if (stringBuilder.length() == 0) {
            return new UnknownUser(this.toString()).getInfoAbout(pcName);
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String parseList(@NotNull String name) {
        String[] splitNamePC = name.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Path path = Paths.get(splitNamePC[1]);//\\do0045.eatmeat.ru\c$\Users\desktop.ini (example)
            if (path.toFile().isDirectory()) {
                this.forADUser = path.getFileName().toString();
                stringBuilder.append(classOption).append(" : ").append(forADUser).append("\n");
            }
            
        }
        catch (IndexOutOfBoundsException e) {
            this.forADUser = e.getMessage();
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", LocalUserResolverDBSender.class.getSimpleName() + "[\n", "\n]")
                .add("classOption = " + classOption)
                .add("resolvedPC = '" + forADUser + "'")
                .add("walkerToUserFolder = " + !(walkerToUserFolder == null))
                .toString();
    }
    
    
}
