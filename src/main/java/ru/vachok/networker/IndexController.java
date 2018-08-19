package ru.vachok.networker;



import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.pasclass.Repo;
import ru.vachok.networker.pasclass.Visitor;
import ru.vachok.networker.workers.FtpCheck;
import ru.vachok.networker.workers.SaverByOlder;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static ru.vachok.networker.ApplicationConfiguration.logger;


/**
 * The type Index controller.
 */
@Controller
public class IndexController {

    private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();
    private static final int EXPIRY = 90;

    private MessageToUser messageToUser = new MessageCons();

    private Logger logger = ApplicationConfiguration.logger();


    /**
     * Map to show map.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @return the map
     *
     * @throws IOException      the io exception
     * @throws ServletException the servlet exception
     */
    @RequestMapping("/docs")
    @ResponseBody
    public Map<String, String> mapToShow( HttpServletRequest httpServletRequest , HttpServletResponse httpServletResponse ) throws IOException, ServletException {
        ExecutorService executorService = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        Runnable r = new SaverByOlder(SHOW_ME);
        SHOW_ME.put("addr" , httpServletRequest.getRemoteAddr());
        SHOW_ME.put("host" , httpServletRequest.getRequestURL().toString());
        SHOW_ME.forEach(( x , y ) -> messageToUser.info(this.getClass().getSimpleName() , x , y));
        Stream<String> stringStream = addrInLocale(httpServletRequest , httpServletResponse);
        stringStream.forEach(x -> SHOW_ME.put("addrInLocale" , x));
        SHOW_ME.put("status" , httpServletResponse.getStatus() + " " + httpServletResponse.getBufferSize() + " buff");
        String s = httpServletRequest.getQueryString();
        if (s != null) {
            SHOW_ME.put(this.toString() , s);
            if (s.contains("go")) httpServletResponse.sendRedirect("http://ftpplus.vachok.ru/docs");
            executorService.execute(r);
        }
        executorService.execute(r);
        Repo.getI(httpServletRequest.getSession().getCreationTime() , httpServletRequest.getRemoteAddr());
        return SHOW_ME;
    }


    /**
     * Addr in locale stream.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @return the stream
     *
     * @throws IOException the io exception
     */
    @RequestMapping("/vir")
    @ResponseBody
    public Stream<String> addrInLocale( HttpServletRequest httpServletRequest , HttpServletResponse httpServletResponse ) throws IOException {
        String re = "redirect:https://vachok.testquality.com/project/3260/plan/6672/test/86686";
        Cookie cooki = new Cookie("hi" , re);
        httpServletResponse.addCookie(cooki);
        ServletInputStream in = httpServletRequest.getInputStream();
        byte[] bs = new byte[0];
        while (in.isReady()) {
            in.read(bs);
        }

        messageToUser.info("HTTP Servlets Controller" , httpServletRequest.getServletPath() + re , "1 КБ resp: " + new String(bs , "UTF-8"));
        File[] files = new File("g:\\myEX\\").listFiles();
        int length = files.length;
        List<String> namesFile = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            File file = files[new Random().nextInt(length)];
            namesFile.add(file.getAbsolutePath());
        }
        String s = LocalDateTime.of(2018 , 10 , 14 , 7 , 0).format(DateTimeFormatter.ofPattern("dd/MM/yy"));
        String command = "\"C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\wmplayer.exe\"";
        Runtime.getRuntime().exec(command);
        Map<String, String> getEnv = System.getenv();
        getEnv.forEach(( x , y ) -> namesFile.add(x + "////" + y));
        namesFile.add(re);
        namesFile.add(new String(bs , "UTF-8"));
        namesFile.add(s);
        namesFile.add(httpServletRequest.toString());
        namesFile.add(httpServletRequest.getSession().getServletContext().getServerInfo());
        namesFile.add(httpServletRequest.getSession().getServletContext().getServletContextName());
        namesFile.add(httpServletRequest.getSession().getServletContext().getVirtualServerName());
        namesFile.add(httpServletRequest.getSession().getServletContext().getContextPath());
        namesFile.add(Arrays.toString(httpServletResponse.getHeaderNames().toArray()));
        return namesFile.stream().sorted();
    }


    /**
     * Exit app.
     *
     * @param httpServletRequest the http servlet request
     * @throws IOException the io exception
     */
    @RequestMapping("/stop")
    public void exitApp( HttpServletRequest httpServletRequest ) throws IOException {
        String s = httpServletRequest.getRequestURL().toString();
        messageToUser.infoNoTitles(s);
        String q = httpServletRequest.getQueryString();
        if (q != null) {
            messageToUser.infoNoTitles(q);
            if (q.contains("full")) Runtime.getRuntime().exec("shutdown /p /f");
            if (q.contains("restart")) Runtime.getRuntime().exec("shutdown /r /f");
        } else System.exit(0);
    }


    /**
     * Index string.
     *
     * @param model   the model
     * @param request the request
     * @return the string
     */
    @RequestMapping(value = {"/" , "/index"}, method = RequestMethod.GET)
    public String index( Model model , HttpServletRequest request ) throws IOException {
        long time = request.getSession().getCreationTime();
        String remoteAddr = request.getRemoteAddr();
        String q = request.getQueryString();
        new Visitor(time , remoteAddr);
        if (q != null) {
            messageToUser.infoNoTitles(q);
            if (q.contains("ftp")) new FtpCheck();
        }
        logger().info(new Date(time) + " was - " + remoteAddr);
        String message = "Привет землянин... Твоя сессия идёт " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - request.getSession().getCreationTime()) + " сек...\n" + request.getSession().getMaxInactiveInterval() + " getMaxInactiveInterval\n" + request.getSession().getId() + " ID сессии\n" + "запрошен URL: " + request.getRequestURL().toString();
        Cookie[] requestCookies = request.getCookies();
        File dirCOOK = new File("cook");
        boolean mkdir = dirCOOK.mkdir();
        Enumeration<String> attributeNames = request.getSession().getAttributeNames();
        StringBuilder sb = new StringBuilder();
        while (attributeNames.hasMoreElements()) sb.append(attributeNames.nextElement());
        if (requestCookies != null) {
            setCookies(requestCookies , dirCOOK , remoteAddr , mkdir , sb , model);
        }

        model.addAttribute("message" , message);
        logger().info("dirCOOK = " + dirCOOK.getAbsolutePath());
        return "index";
    }


    private void setCookies( Cookie[] requestCookies , File dirCOOK , String remoteAddr , boolean mkdir , StringBuilder sb , Model model ) {
        for (Cookie cookie : requestCookies) {
            try {
                cookie.setDomain(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                logger.error(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()).replaceAll(", " , "\n"));
            }
            cookie.setMaxAge(EXPIRY);
            cookie.setPath(dirCOOK.getAbsolutePath());
            Runtime runtime = Runtime.getRuntime();
            cookie.setValue(remoteAddr + runtime.availableProcessors() + " processors\n" + runtime.freeMemory() + "/" + runtime.totalMemory() + " memory\n" + model.asMap().toString().replaceAll(", " , "\n"));
            cookie.setComment(remoteAddr + " ip\n" + sb.toString());
            if (mkdir) {
                logger().info(dirCOOK.getAbsolutePath());
            }
            try (FileOutputStream outputStream = new FileOutputStream(dirCOOK.getAbsolutePath() + "\\cook" + System.currentTimeMillis() + ".txt")) {
                String s = "Domain: " + cookie.getDomain() + " name: " + cookie.getName() + " comment: " + cookie.getComment() + "\n" + cookie.getPath() + "\n" + cookie.getValue() + "\n" + new Date(System.currentTimeMillis());
                byte[] bytes = s.getBytes();
                outputStream.write(bytes , 0 , bytes.length);
            } catch (IOException e) {
                logger.error(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()).replaceAll(", " , " "));
            }
        }
    }
}
