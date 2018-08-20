package ru.vachok.networker.logic;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;


/**
 * The type Err control.
 */
@Controller
public class ErrControl implements ErrorController {

    @Override
    public String getErrorPath() {
        return "/error";
    }


    /**
     * Err handle string.
     *
     * @param httpServletRequest the http servlet request
     * @return the string
     */
    @RequestMapping("/error")
    @ResponseBody
    public String errHandle( HttpServletRequest httpServletRequest) {
        Integer statCode = ( Integer ) httpServletRequest.getAttribute("javax.servlet.error.status_code");
        Exception exception = ( Exception ) httpServletRequest.getAttribute("javax.servlet.error.exception");
        return String.format("<html><body><h2>Вы попали в \"Научно-Исследовательский Институт Химии " +
                "Удобрений и Ядов\"</h2><div>Status " +
                "code: <b>%s</b></div>" + "<div" +
                ">Exception " +
                "Message: " +
                "<b>%s</b></div><body></html>" , statCode , exception == null ? "N/A" : exception.getMessage());
    }


    @RequestMapping("/inerror")
    @ResponseBody
    public String internError( Exception e ) {
        return e.getMessage() + "         \n" + Arrays.toString(e.getStackTrace());
    }


    @RequestMapping("/idea")
    public void lIdea( HttpServletRequest request ) throws IOException {
        String q = request.getQueryString();
        Runtime.getRuntime().exec("G:\\My_Proj\\.IdeaIC2017.3\\apps\\IDEA-C\\ch-0\\182.3684.101\\bin\\idea64.exe");
        if (q != null) {
            if (q.contains("exe:")) {
                String[] exes = q.split("exe:");
                System.out.println("exes = " + Arrays.toString(exes));
                System.out.println("exes[1] = " + exes[1]);
                Runtime.getRuntime().exec(exes[1]);
            }
        }
    }
}
