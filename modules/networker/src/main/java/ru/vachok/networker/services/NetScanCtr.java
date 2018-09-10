package ru.vachok.networker.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.TForms;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;


/**
 * @since 30.08.2018 (12:55)
 */
@Controller
public class NetScanCtr {

    private NetScannerSvc netScannerSvc;

    /*Instance*/
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc) {
        this.netScannerSvc = netScannerSvc;
    }

    @GetMapping("/netscan")
    public String netScan(HttpServletRequest request, Model model) {
        if (request.getQueryString() != null) {
            netScannerSvc.setQer(request.getQueryString());
            List<String> pcNames = netScannerSvc.getPCNames(request.getQueryString());
            model.addAttribute("date", new Date().toString());
            model.addAttribute("pc", new TForms().fromArray(pcNames));
            return "netscan";
        } else {
            List<String> pCsAsync = netScannerSvc.getPCsAsync();
            model.addAttribute("pc", new TForms().fromArray(pCsAsync));
            return "netscan";
        }
    }
}
