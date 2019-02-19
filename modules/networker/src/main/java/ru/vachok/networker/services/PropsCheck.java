package ru.vachok.networker.services;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.List;
import java.util.Properties;


/**
 Проверяет {@link ConstantsFor#getProps()}
 <p>

 @since 19.02.2019 (1:08) */
public class PropsCheck {

    private MessageToUser messageToUser = new MessageLocal();

    private boolean isPropOk() {
        InitProperties initProperties = new DBRegProperties(ConstantsFor.FILE_RU_VACHOK_NETWORKER_CONSTANTS_FOR);
        File pFile = new File("ru_vachok_networker-ConstantsFor.properties");
        if(pFile.exists()){
            List<String> readFile = FileSystemWorker.readFileToList(pFile.getAbsolutePath());
            boolean isFileIsBigger = (readFile.size() - 2) > initProperties.getProps().size();
            if(isFileIsBigger){
                initProperties = new FileProps(ConstantsFor.FILE_RU_VACHOK_NETWORKER_CONSTANTS_FOR);
                Properties finalCProp = initProperties.getProps();
                readFile.remove(0);
                readFile.remove(1);
                readFile.forEach(x -> {
                    try{
                        finalCProp.put(x.split("=")[0], x.split("=")[1]);
                    }
                    catch(ArrayIndexOutOfBoundsException ignore){
                        //
                    }
                    messageToUser.infoNoTitles(new TForms().fromArray(finalCProp, false));
                });
            }
            return isFileIsBigger;
        }
        return true;
    }

}