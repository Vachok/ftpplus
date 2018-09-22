package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.AppCtx;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * @since 21.09.2018 (11:33)
 */
@Controller
public class ServiceInfoCtrl {

    private VisitorSrv visitorSrv;

    private Visitor visitor;

    /*Instances*/
    @Autowired
    public ServiceInfoCtrl() {
        this.visitorSrv = new VisitorSrv();
        this.visitor = visitorSrv.getVisitor();
    }

    @GetMapping("/serviceinfo")
    public String infoMapping(Model model, HttpServletRequest request) throws AccessDeniedException {
        visitorSrv.makeVisit(request);
        if (request.getRemoteAddr().contains("0:0:0:0") ||
            request.getRemoteAddr().contains("10.10.111") ||
            request.getRemoteAddr().contains(ConstantsFor.NO0027)) {
            model.addAttribute("title", "PF IS " + pingBool() + " now: " + LocalTime.now().toString());
            model.addAttribute("ping", pingVPN());
            model.addAttribute("urls", new TForms().fromArray(AppCtx.getClassLoaderURLList()));
            model.addAttribute("request", prepareRequest(request));
            model.addAttribute("visit", visitor.toString() +
                "\nUNIQ:" + visitorSrv.uniqUsers() + "\n" +
                visitor.getDbInfo());

            model.addAttribute("genstamp", "Generated: " +
                new Date().getTime() +
                ". Up: " +
                TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP));
            return "vir";
        } else throw new AccessDeniedException("Sorry. Denied");
    }

    private boolean pingBool() {
        try{
            if(InetAddress.getByName("srv-git.eatmeat.ru").isReachable(1000)){
                return true;
            }
            else{
                return false;
            }
        }
        catch(IOException e){
            return false;
        }
    }

    private String pingVPN() {
        try{
            InetAddress byName = InetAddress.getByName("srv-git.eatmeat.ru");
            return "<b>" + byName.isReachable(1000) + "</b> pf.eatmeat.ru<br>";
        }
        catch(IOException e){
            return e.getMessage();
        }
    }

    private String prepareRequest(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<center><h3>Заголовки</h3></center>");
        stringBuilder.append(new TForms().fromEnum(request.getHeaderNames(), true));
        stringBuilder.append("<center><h3>Атрибуты</h3></center>");
        stringBuilder.append(new TForms().fromEnum(request.getAttributeNames(), true));
        stringBuilder.append("<center><h3>Параметры</h3></center>");
        stringBuilder.append(new TForms().fromEnum(request.getParameterNames(), true));
        return stringBuilder.toString();
    }
}
