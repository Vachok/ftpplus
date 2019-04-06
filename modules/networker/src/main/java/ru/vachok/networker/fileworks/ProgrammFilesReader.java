package ru.vachok.networker.fileworks;



import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;


/**
 @since 06.04.2019 (16:19) */
public interface ProgrammFilesReader extends Callable {


    String readFile(File fileToRead);

    List<String> readFileAsList(File fileToRead);
}
