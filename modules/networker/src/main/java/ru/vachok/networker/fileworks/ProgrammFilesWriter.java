package ru.vachok.networker.fileworks;



import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;


/**
 @since 06.04.2019 (15:53) */
public interface ProgrammFilesWriter extends Callable<String> {

    boolean writeFile(List<?> toWriteList);

    boolean writeFile(Stream<?> toWriteStream);

    boolean writeFile(Map<?, ?> toWriteMap);

    boolean writeFile(File toWriteFile);

    boolean writeFile(Exception e);

    String error(String fileName , Exception e);

    void setFileName(String fileName);
}
