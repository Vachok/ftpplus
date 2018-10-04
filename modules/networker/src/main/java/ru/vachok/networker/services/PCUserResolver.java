package ru.vachok.networker.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.LastNetScan;

/**
 @since 02.10.2018 (17:32) */
@Service
public class PCUserResolver {

    private LastNetScan lastNetScan;

    @Autowired
    public PCUserResolver(LastNetScan lastNetScan) {
        this.lastNetScan = lastNetScan;
    }

    public String getResolvedName(String pcName) {
        //todo 02.10.2018 (17:35)

        throw new UnsupportedOperationException("Not Ready yet 02.10.2018 (17:35)");
    }

}
