// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 @since 14.07.2019 (17:33) */
public interface InitProperties extends ru.vachok.mysqlandprops.props.InitProperties {
    
    
    String DB = "db";
    
    String FILE = "file";
    
    String ATAPT = "adapt";
    
    @Contract("_ -> new")
    static @NotNull InitProperties getInstance(String type) {
        switch (type) {
            case DB:
                return new DBPropsCallable();
            case ATAPT:
                return new InitPropertiesAdapter(DB);
            default:
                return new FilePropsLocal(ConstantsFor.class.getSimpleName());
        }
    }
    
    static void reloadApplicationPropertiesFromFile() {
        File propsFile = new File(ConstantsFor.class.getSimpleName() + FileNames.FILEEXT_PROPERTIES);
        try {
            Files.setAttribute(propsFile.toPath(), "dos:readonly", true);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
