package ru.vachok.money.ctrls;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;
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
    public String ftpMe( HttpServletResponse response , Model model ) {
        if (ConstantsFor.isMyPC()) {
            long l = TimeUnit.MILLISECONDS.
                    toMinutes(System.currentTimeMillis() - new Calendar.Builder().setDate(ConstantsFor.YEAR_BIRTH , 0 , 7).build().getTimeInMillis());
            model.addAttribute("life" , l);
        }

        FtpHomeCamCheck ftpHomeCamCheck = new FtpHomeCamCheck();
        String call = ftpHomeCamCheck.call() + "<br>";
        HomePCFilesCheck homePCFilesCheck = new HomePCFilesCheck();
        Stream<String> call1 = homePCFilesCheck.call();
        Object[] objects = call1.toArray();
        StringBuilder sb = new StringBuilder();
        String callStr = "<p>" + call + "</p>";
        model.addAttribute("call" , callStr);
        Float f = ((float) objects.length / ConstantsFor.FILES_TO_ENC_BLOCK);
        String callStr1 = "<p>" + objects.length + " TOTAL FILES is " + f + " converts<br></p>";
        model.addAttribute("call1" , callStr1);
        sb.append("<p>");
        for (Object object : objects) {
            sb.append(object.toString()).append("<br>");
        }
        sb.append("</p>");
        model.addAttribute("ftp" , sb.toString());
        return "ftp";
    }
}