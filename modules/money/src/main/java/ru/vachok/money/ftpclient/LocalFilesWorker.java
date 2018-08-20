package ru.vachok.money.ftpclient;



import org.apache.commons.io.FileUtils;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.ctrls.ErrCtrl;
import ru.vachok.money.logic.DecoderEnc;
import ru.vachok.money.logic.Utilit;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;


/**
 * @since 21.07.2018 (18:14)
 */
public class LocalFilesWorker implements Callable<String> {

    private static final File DIR_VID = new File("f:\\Video\\Captures\\IPCamera\\IV2405P_00626E6A45EA\\record\\");

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = LocalFilesWorker.class.getSimpleName();

    private static final InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);

    private final DecoderEnc decoderUTF = new ru.vachok.money.logic.UTF8();


    @Override
    public String call() {
        return countFilesAndSizes();
    }


    private String countFilesAndSizes() {
        if (Utilit.thisPCName().toLowerCase().contains("home")) {
            File[] localVidFilesFromFTP = DIR_VID.listFiles();
            int count = localVidFilesFromFTP.length;
            long size = 0;
            for (File f : localVidFilesFromFTP) {
                size += f.length();
            }
            return size / ConstantsFor.MEGABYTE + decoderUTF.toAnotherEnc(" мегабайт в ") + count + decoderUTF.toAnotherEnc(" файлах на жестком диске.\n" + DIR_VID.getAbsolutePath());
        } else throw new UnsupportedOperationException(decoderUTF.toAnotherEnc("Это возможно только на домашнем ПК!"));
    }


    private void txtMaker() {
        File[] videoFiles = DIR_VID.listFiles();
        Properties properties = initProperties.getProps();
        LinkedHashMap<String, File> filesWithNames = new LinkedHashMap<>();
        int length = videoFiles.length;
        Integer need = Integer.valueOf(properties.getProperty("need"));
        if (length >= need) {
            for (int i = 0; i < need; i++) {
                File vFile = videoFiles[i];
                String name = vFile.getName();
                filesWithNames.put(name , vFile);
            }
            txtWorker(filesWithNames);
        } else {
            new ErrCtrl().err(null);
        }
    }


    private void txtWorker( Map<String, File> filesWithNames ) {
        File txtFile = new File(DIR_VID + "\\111new.txt");
        try {
            FileUtils.touch(txtFile);
        } catch (IOException e) {
            ErrCtrl.stackErr(e);
        }
    }
}