package ru.vachok.money.ctrls;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.money.ApplicationConfiguration;
import ru.vachok.money.ftpclient.FtpHomeCamCheck;
import ru.vachok.money.ftpclient.HomePCFilesCheck;

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 * @since 20.08.2018 (23:38)
 */
@Controller
public class FTPCtrl {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FTPCtrl.class.getSimpleName();


    @GetMapping("/ftp")
    @ResponseBody
    public String ftpMe(HttpServletResponse response) {
        if(!ApplicationConfiguration.pcName().equalsIgnoreCase("home")) return "Only at home available!<p>"+ ApplicationConfiguration.pcName()+"<p>"+
              TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()-
                    new Calendar.Builder().setDate(1984,0,7).build().getTimeInMillis());
        else{
        FtpHomeCamCheck ftpHomeCamCheck = new FtpHomeCamCheck();
        String call = ftpHomeCamCheck.call() + "\n";
        HomePCFilesCheck homePCFilesCheck = new HomePCFilesCheck();
        Stream<String> call1 = homePCFilesCheck.call();
        Object[] objects = call1.toArray();
        StringBuilder sb = new StringBuilder();
        sb.append(objects.length).append(" TOTAL FILES<p>");
        for (Object object : objects) {
            sb.append(object.toString()).append("<p>");
        }
        sb.append("<p>").append(call);
        return sb.toString();
        }
    }
}