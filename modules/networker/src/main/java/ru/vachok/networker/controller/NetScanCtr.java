package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.NetScannerSvc;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * @since 30.08.2018 (12:55)
 */
@Controller
public class NetScanCtr {

    private NetScannerSvc netScannerSvc;

    /*Fields*/
    private static final String SOURCE_CLASS = NetScanCtr.class.getSimpleName();

    private static InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);

    private static final Logger LOGGER = AppComponents.getLogger();

    private static Properties properties = initProperties.getProps();

    private String netscanString = "netscan";

    /*Instance*/
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc) {
        this.netScannerSvc = netScannerSvc;
    }

    @GetMapping("/netscan")
    public String netScan(HttpServletRequest request, Model model) {
        String lastscan = properties.getProperty("lastscan");
        long l = Long.parseLong(lastscan) + TimeUnit.MINUTES.toMillis(25);
        if(l > System.currentTimeMillis()){
            String msg = TimeUnit.MILLISECONDS.toSeconds(l - System.currentTimeMillis()) + " seconds left";
            LOGGER.warn(msg);
            model.addAttribute("pc", msg);
        }
        else
            if(request.getQueryString()!=null){
            netScannerSvc.setQer(request.getQueryString());
            List<String> pcNames = netScannerSvc.getPCNames(request.getQueryString());
            model.addAttribute("date", new Date().toString());
            model.addAttribute("pc", new TForms().fromArray(pcNames));
        } else {
            List<String> pCsAsync = netScannerSvc.getPCsAsync();
                model.addAttribute("date",
                    ( float ) TimeUnit.MILLISECONDS
                        .toSeconds(System.currentTimeMillis() - l) / ConstantsFor.ONE_HOUR_IN_MIN + " ago was last scan");
            model.addAttribute("pc", new TForms().fromArray(pCsAsync));
                properties.setProperty("lastscan", System.currentTimeMillis() + "");
                initProperties.delProps();
                initProperties.setProps(properties);
        }
        return netscanString;
    }
}
