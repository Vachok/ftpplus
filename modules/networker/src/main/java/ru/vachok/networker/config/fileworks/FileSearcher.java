package ru.vachok.networker.config.fileworks;


import ru.vachok.networker.TForms;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


public class FileSearcher extends FileSystemWorker {

    private String patternToSearch;

    private List<String> resList = new ArrayList<>();

    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
    }

    public List<String> getResList() {
        return resList;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && file.toFile().getName().toLowerCase().contains(patternToSearch)) {
            resList.add(file.toFile().getAbsolutePath());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (dir.toFile().isDirectory()) {
            String msg = dir.toString() + " просмотрено";
            LOGGER.info(msg);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public String toString() {
        if (resList.size() > 0) return new TForms().fromArray(resList, false);
        else return resList.size() + " nothing...";
    }
}
