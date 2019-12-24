package ru.vachok.networker.restapi;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.info.InformationFactory;

import javax.servlet.http.HttpServletRequest;


/**
 @see RestCTRLTest
 @since 15.12.2019 (19:42) */
@RestController
public class RestCTRL {


    @GetMapping("/status")
    public String appStatus() {
        return UsefulUtilities.getRunningInformation();
    }

    @GetMapping("/pc")
    public String uniqPC(HttpServletRequest request) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.REST_PC_UNIQ);
        if (request.getQueryString() != null) {
            return informationFactory.getInfo();
        }
        else {
            informationFactory.setClassOption(true);
            return informationFactory.getInfoAbout("");
        }
    }

}