package ru.vachok.networker.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 Удаление временных файлов.

 @since 19.12.2018 (11:05) */
@SuppressWarnings("ClassWithoutmessageToUser")
class DeleterTemp extends FileSystemWorker implements Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());

    /**
     Запись лога в файл {@code DeleterTemp.class.getSimpleName() + "_log.txt"}.
     */
    private static PrintWriter printWriter;

    /**
     Счётчик файлов
     */
    private int filesCounter = 0;

    private List<String> fromFile = new ArrayList<>();

    private String patToDel = null;

    DeleterTemp(String patToDel) {
        this.patToDel = patToDel;
        this.fromFile.add(patToDel);
    }

    DeleterTemp() {
        try(OutputStream outputStream = new FileOutputStream(DeleterTemp.class.getSimpleName() + "_log.txt")){
            printWriter = new PrintWriter(outputStream, true);
        }
        catch(IOException e){
            messageToUser.error(new TForms().fromArray(e, false));
        }
        getList();
        run();
    }


    private void getList() {
        if(patToDel!=null){
            fromFile.add(patToDel);
        }
        else{
            try(InputStream inputStream = DeleterTemp.class.getResourceAsStream("/BOOT-INF/classes/static/config/temp_pat.cfg");
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader)){
                while(bufferedReader.ready()){
                    fromFile.add(bufferedReader.readLine());
                }
            }
            catch(IOException e){
                messageToUser.warn(new TForms().fromArray(e, false));
            }
        }
    }
    
    private void oldSSHLogDel() {
        File sshFolder = new File(".\\ssh\\");
        List<File> files = Arrays.asList(sshFolder.listFiles());
        files.forEach(x -> {
            if(x.lastModified() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)){
                try{
                    Files.deleteIfExists(x.toPath());
                } catch(IOException e){
                    messageToUser.error(e.getMessage());
                }
            }
        });
        
    }
    
    @Override
    public void run() {
        AppComponents.threadConfig().thrNameSet("delTmp");
        try {
            AppComponents.threadConfig().executeAsThread(this::oldSSHLogDel);
        } catch (RuntimeException e) {
            messageToUser.error(e.getMessage());
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Thread.currentThread().setName("DeleterTemp.visitFile");
        this.filesCounter = filesCounter + 1;
        String fileAbs = new StringBuilder()
            .append(file.toAbsolutePath().toString())
            .append(ConstantsFor.STR_DELETED).toString();
        if(more2MBOld(attrs)){
            Files.setAttribute(file, ConstantsFor.DOS_ARCHIVE, true);
            printWriter.println(new StringBuilder()
                .append(file.toAbsolutePath())
                .append(",")
                .append(( float ) file.toFile().length() / ConstantsFor.MBYTE)
                .append(",")
                .append(new Date(attrs.lastAccessTime().toMillis()))
                .append(",")
                .append(Files.readAttributes(file, "dos:*")).toString());
        }
        if(tempFile(file.toAbsolutePath())){
            try{
                Files.deleteIfExists(file);
            }
            catch(FileSystemException e){
                file.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }
            messageToUser.warn(fileAbs);
        }

        return FileVisitResult.CONTINUE;
    }

    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>

     @param attrs {@link BasicFileAttributes}
     @return <b>true</b> = lastAccessTime - ONE_YEAR and size bigger MBYTE*2
     */
    private boolean more2MBOld(BasicFileAttributes attrs) {
        return attrs
            .lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR) &&
            attrs
                .size() > ConstantsFor.MBYTE * 2;
    }

    /**
     Проверка файлика на "временность".
     <p>
     ClassPath - /BOOT-INF/classes/static/config/temp_pat.cfg <br> .\resources\static\config\temp_pat.cfg

     @param filePath {@link Path} до файла
     @return удалять / не удалять
     */
    private boolean tempFile(Path filePath) {
        return fromFile.stream().anyMatch(sP -> filePath.toString().toLowerCase().contains(sP));
    }
}
