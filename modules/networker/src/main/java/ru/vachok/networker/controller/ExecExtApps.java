package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.PassGenerator;
import ru.vachok.networker.services.PhotoConverter;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;


/**
 @since 22.08.2018 (10:17) */
@Controller
public class ExecExtApps {

    private static final AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();
    private static final Logger LOGGER = AppComponents.getLogger();

    @RequestMapping ("/idea")
    @ResponseBody
    public String lIdea(HttpServletRequest request) {
        String q = request.getQueryString();
        boolean alive = false;
        Process exec;
        try{
            exec = Runtime.getRuntime()
                .exec("G:\\My_Proj\\.IdeaIC2017.3\\apps\\IDEA-C\\ch-0\\182.3684.101\\bin\\idea64.exe");
            alive = exec.isAlive();
        } catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }

        if(q!=null && q.contains("exe:")){
            String[] exes = q.split("exe:");
            String format1 = String.format("exes = %s", Arrays.toString(exes));
            LOGGER.info(format1);
            String format = String.format("exes[1] = %s", exes[1]);
            LOGGER.info(format);
            try{
                exec = Runtime.getRuntime().exec(exes[1]);
                String msg = exec.isAlive() + " app start";
                LOGGER.info(msg);
            }
            catch(IOException e){
                LOGGER.error(e.getMessage(), e);
            }
        }
        return "IDEA is " + alive;
    }

    @GetMapping ("/clsmail")
    public String cleanMailbox(Model model) throws MessagingException {
        MailMessages mailMessages = new MailMessages();
        Folder inbox = mailMessages.getInbox();
        Message[] messages = inbox.getMessages();
        if(messages.length <= 0) showRandomFoto(model);
        for(Message m : messages){
            m.setFlag(Flags.Flag.DELETED, true);
            boolean setAnswered = m.isSet(Flags.Flag.DELETED);
            model.addAttribute("mboxout", (m.getSubject() + ", " + m.getSentDate() + " deleted: " + setAnswered + " "));
        }
        inbox.close(true);
        return "clsmail";
    }

    public Model showRandomFoto(Model model) {
        PhotoConverter bean = ctx.getBean(PhotoConverter.class);
        return model;
    }

    @GetMapping("/gen")
    public String passGen(HttpServletRequest request, Model model) {
        PassGenerator passGenerator = ctx.getBean(PassGenerator.class);
        int howMuchBytes = 30;
        if (request.getQueryString() != null) {
            try {
                howMuchBytes = Integer.parseInt(request.getQueryString());
                model.addAttribute("pass", passGenerator.generatorPass(howMuchBytes));
            } catch (Exception e) {
                model.addAttribute("pass", e.getMessage());
                return "ad";
            }
        }
        model.addAttribute("title", howMuchBytes);
        model.addAttribute("pass", passGenerator.generatorPass(howMuchBytes));
        return "ad";
    }
}
