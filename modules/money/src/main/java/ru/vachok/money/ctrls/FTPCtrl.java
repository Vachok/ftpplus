package ru.vachok.money.ctrls;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.money.ftpclient.FtpHomeCamCheck;
import ru.vachok.money.ftpclient.HomePCFilesCheck;

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
    public String ftpMe() {
        FtpHomeCamCheck ftpHomeCamCheck = new FtpHomeCamCheck();
        String call = ftpHomeCamCheck.call() + "\n";
        HomePCFilesCheck homePCFilesCheck = new HomePCFilesCheck();
        Stream<String> call1 = homePCFilesCheck.call();
        Object[] objects = call1.toArray();
        StringBuilder sb = new StringBuilder();
        sb.append("TOTAL FILES IS - " + objects.length + "<p>");
        for (Object object : objects) {
            sb.append("\n" + object.toString() + "<p>");
        }
        sb.append("<p>" + call);
        return sb.toString();
    }
}