package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.networker.config.AppComponents;
import ru.vachok.networker.logic.PhotoConverter;

import javax.mail.*;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


/**
 @since 22.08.2018 (10:17) */
@Controller
public class ExecExtApps {

    private static final Logger LOGGER = AppComponents.logger();

    @RequestMapping ("/idea")
    public String lIdea(HttpServletRequest request) {
        String q = request.getQueryString();
        boolean alive = false;
        Process exec;
        try{
            exec = Runtime.getRuntime().exec("G:\\My_Proj\\.IdeaIC2017.3\\apps\\IDEA-C\\ch-0\\182.3684.101\\bin\\idea64.exe");
            alive = exec.isAlive();
        } catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }

        if(q!=null){
            if(q.contains("exe:")){
                String[] exes = q.split("exe:");
                LOGGER.info(String.format("exes = %s", Arrays.toString(exes)));
                LOGGER.info(String.format("exes[1] = %s", exes[1]));
                try{
                    exec = Runtime.getRuntime().exec(exes[1]);
                } catch(IOException e){
                    LOGGER.error(e.getMessage(), e);
                }
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
        PhotoConverter photoConverter = new PhotoConverter();
        Map<String, BufferedImage> stringBufferedImageMap = photoConverter.convertFoto();
        Set<String> keys = stringBufferedImageMap.keySet();
        Stream<String> stream = keys.stream();
        model.addAttribute("userimage", "static/images/" + stream.findAny().get());
        return model;
    }
}
