package ru.vachok.money.ctrls;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.config.ThreadConfig;
import ru.vachok.money.filesys.FilesCleaner;
import ru.vachok.money.services.DBMessage;
import ru.vachok.money.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 Системная информация
 <p>

 @since 27.09.2018 (0:01) */
@Controller
public class SysInfoCtrl {

    private ThreadConfig t = new ThreadConfig();

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    @GetMapping ("/sysinfo")
    public String gettingInfo(Model model, HttpServletRequest request) {
        AppVersion appVersion = new AppVersion();

        model.addAttribute("title", appVersion.getAppVBuild());
        model.addAttribute("result", appVersion.toString());
        model.addAttribute("ok", "<a href=\"/cleandir\">cleandir</a><br>");
        model.addAttribute("footer", new PageFooter().getTheFooter());
        model.addAttribute("destiny", getSomeShit());
        return "ok";
    }

    private String getSomeShit() {
        StringBuilder stringBuilder = new StringBuilder();

        LocalDate nowDate = LocalDate.now();
        DayOfWeek satDayOfWeek = DayOfWeek.SATURDAY;
        int firstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
        Calendar.Builder builder = new Calendar.Builder();
        int toSatDays = satDayOfWeek.getValue() - firstDayOfWeek;
        Date date = builder.setDate(
            nowDate.getYear(),
            nowDate.getMonthValue() - 1,
            nowDate.getDayOfMonth() + toSatDays).build().getTime();
        Callable<TimeInfo> atomCall = new TimeChecker();
        TimeInfo atomInfo = null;
        try{

            atomInfo = atomCall.call();
        }
        catch(Exception e){
            LOGGER.warn(e.getMessage());
        }
        stringBuilder
            .append("<br>До ")
            .append(date.toString())
            .append("<br><font color=\"yellow\">")
            .append(toSatDays)
            .append("</font> дней...<br>Точное UNIX-время: <font color=\"orange\">")
            .append(Objects.requireNonNull(atomInfo).getReturnTime())
            .append("</font><br>Human readable: ")
            .append(Objects.requireNonNull(atomInfo).getMessage().toString().replaceAll("\\Q[\\E", "")
                .replaceAll("\\Q]\\E", "").replaceAll(",", "<br>").replaceAll("\\Q:\\E", ": "));
        return stringBuilder.toString();
    }

    @GetMapping ("/cleandir")
    public String cleanDirectory(Model model) {
        FilesCleaner filesCleanerHome = new FilesCleaner("\\\\10.10.111.1\\Torrents-FTP\\home\\", true);
        String toClean = getFilesToClean(filesCleanerHome);

        model.addAttribute("title", "Cleaning: " + filesCleanerHome.getStartDir());
        model.addAttribute("result", toClean);
        model.addAttribute("footer", new PageFooter().getTheFooter());

        MessageToUser messageToUser = new DBMessage();
        messageToUser.infoNoTitles(toClean + "\n\n");

        return "ok";
    }

    private String getFilesToClean(FilesCleaner filesCleaner) {
        Future<String> submit = t.getDefaultExecutor().submit(filesCleaner);
        try{
            return submit.get();
        }
        catch(ExecutionException | InterruptedException e){
            Thread.currentThread().interrupt();
            return e.getMessage();
        }
    }
}