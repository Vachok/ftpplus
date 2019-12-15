package ru.vachok.networker.restapi;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vachok.networker.componentsrepo.UsefulUtilities;


/**
 @since 15.12.2019 (19:42) */
@RestController
public class RestCTRL {


    @GetMapping("/status")
    public String appStatus() {
        return UsefulUtilities.getRunningInformation();
    }

}