package ru.vachok.money.ctrls;



import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @since 26.08.2018 (1:25)
 */
public class FreeBSD {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FreeBSD.class.getSimpleName();


    public List<File> getPortmasterL() {
        List<File> bsdFilesList = new ArrayList<>();
        File[] file = new File(FileUtils.getUserDirectory().getAbsolutePath()).listFiles();
        for (File file1 : file) {
            if (file1.getName().contains("portmaster")) {
                bsdFilesList.add(file1);
                return bsdFilesList;
            }
        }
        throw new UnsupportedOperationException("NO files with \"portmaster\"");
    }
}