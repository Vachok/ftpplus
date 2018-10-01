package ru.vachok.networker.componentsrepo;


import org.springframework.stereotype.Component;

/**
 @since 01.10.2018 (9:46) */
@Component
public class ServiceInform {

    private String sppedDelay;

    public String getSppedDelay() {
        return sppedDelay;
    }

    public void setSppedDelay(String sppedDelay) {
        this.sppedDelay = sppedDelay;
    }
}
