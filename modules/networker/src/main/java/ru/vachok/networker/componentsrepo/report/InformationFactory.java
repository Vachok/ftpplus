package ru.vachok.networker.componentsrepo.report;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.user.InformationFactoryImpl;


/**
 @since 09.04.2019 (13:16) */
public interface InformationFactory {
    
    
    String getInfoAbout(String aboutWhat);
    
    void setInfo();
    
    static @NotNull String getApplicationRunInformation() {
        return InformationFactoryImpl.getRunningInformation();
    }
}
