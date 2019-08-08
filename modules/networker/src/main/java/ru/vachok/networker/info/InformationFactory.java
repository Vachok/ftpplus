package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.user.TvPcInformation;


/**
 @since 09.04.2019 (13:16) */
public interface InformationFactory {
    
    
    String getInfoAbout(String aboutWhat);
    
    void setInfo(Object info);
    
    static @NotNull String getApplicationRunInformation() {
        return TvPcInformation.getRunningInformation();
    }
}
