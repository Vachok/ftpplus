package ru.vachok.money.ctrls;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ConstantsFor;
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
import java.util.Objects;
import java.util.concurrent.*;


/**
 Системная информация
 <p>

 @since 27.09.2018 (0:01) */
@Controller
public class SysInfoCtrl {


    /*Fields*/

    /**
     Название в сроке времени.
     */
    private static final String S_SPEND = " sec spend";

    private static final Logger LOGGER = AppComponents.getLogger();

    private ThreadConfig t = new ThreadConfig();

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
        final long stArt = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        long returnTimeNow = System.currentTimeMillis();
        LocalDate nowDate = LocalDate.now();
        DayOfWeek satDayOfWeek = DayOfWeek.SATURDAY;
        int toSatDays = satDayOfWeek.getValue() - nowDate.getDayOfWeek().getValue();
        Calendar.Builder builder = new Calendar.Builder();
        Calendar date = builder.setDate(
            nowDate.getYear(),
            nowDate.getMonthValue() - 1,
            nowDate.getDayOfMonth() + toSatDays).setTimeOfDay(0, 1, 0).build();
        long toSatLong = date.getTimeInMillis();
        Callable<TimeInfo> atomCall = new TimeChecker();
        TimeInfo atomInfo = null;
        try{

            atomInfo = atomCall.call();
            atomInfo.computeDetails();
        }
        catch(Exception e){
            LOGGER.warn(e.getMessage());
        }
        if(atomInfo!=null){
            returnTimeNow = atomInfo.getReturnTime();
        }
        stringBuilder
            .append("<br>До ")
            .append(date.getTime().toString())
            .append("<br><font color=\"yellow\">")
            .append(( float ) TimeUnit.MILLISECONDS.toHours(toSatLong - returnTimeNow) / ConstantsFor.ONE_DAY_HOURS)
            .append("</font> дней...<br>Точное UNIX-время: <font color=\"orange\">")
            .append(Objects.requireNonNull(atomInfo).getReturnTime())
            .append("</font><br>Human readable: ")
            .append(Objects.requireNonNull(atomInfo).getMessage().toString().replaceAll("\\Q[\\E", "")
                .replaceAll("\\Q]\\E", "").replaceAll(",", "<br>").replaceAll("\\Q:\\E", ": "));
        String msg = "SysInfoCtrl.getSomeShit method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + S_SPEND;
        LOGGER.info(msg);
        return stringBuilder.toString();
    }

    @GetMapping ("/cleandir")
    public String cleanDirectory(Model model) {
        final long stArt = System.currentTimeMillis();
        FilesCleaner filesCleanerHome = new FilesCleaner("\\\\10.10.111.1\\Torrents-FTP\\home\\");
        String toClean = getFilesToClean(filesCleanerHome);

        model.addAttribute("title", "Cleaning: " + filesCleanerHome.getStartDir());
        model.addAttribute("result", toClean);
        model.addAttribute("footer", new PageFooter().getTheFooter());

        MessageToUser messageToUser = new DBMessage();
        messageToUser.infoNoTitles(toClean + "\n\n");

        String msg = "SysInfoCtrl.cleanDirectory method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + S_SPEND;
        LOGGER.info(msg);
        return "ok";
    }

    private String getFilesToClean(FilesCleaner filesCleaner) {
        final long stArt = System.currentTimeMillis();
        Future<String> submit = t.getDefaultExecutor().submit(filesCleaner);
        try{
            String msg = "SysInfoCtrl.getFilesToClean method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + S_SPEND;
            LOGGER.info(msg);
            return submit.get();
        }
        catch(ExecutionException | InterruptedException e){
            Thread.currentThread().interrupt();
            String msg = "SysInfoCtrl.getFilesToClean method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + S_SPEND;
            LOGGER.info(msg);
            return e.getMessage();
        }
    }
}