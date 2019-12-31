package ru.vachok.networker.restapi;


import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.message.MessageToUser;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Paths;


/**
 @see RestCTRLTest
 @since 15.12.2019 (19:42) */
@RestController
public class RestCTRL {


    @GetMapping("/status")
    public String appStatus() {
        return UsefulUtilities.getRunningInformation();
    }

    @GetMapping("/pc")
    public String uniqPC(HttpServletRequest request) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.REST_PC_UNIQ);
        if (request.getQueryString() != null) {
            return informationFactory.getInfo();
        }
        else {
            informationFactory.setClassOption(true);
            return informationFactory.getInfoAbout("");
        }
    }

    @GetMapping("/file")
    public String fileShow(@NotNull HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String filesShow = userAgent;
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            File toShow = new File(request.getQueryString());
            if (toShow.exists()) {
                filesShow = FileSystemWorker.readFile(toShow);
            }
        }
        else {

            filesShow = getFileShow(userAgent);
        }

        if (userAgent.toLowerCase().contains(ConstantsFor.OKHTTP)) {
            MessageToUser.getInstance(MessageToUser.EMAIL, "Get /file from " + UsefulUtilities.thisPC()).info(filesShow);
        }
        return filesShow;
    }

    @NotNull
    private String getFileShow(String userAgent) {
        StringBuilder stringBuilder = new StringBuilder();
        File file = Paths.get(".").toAbsolutePath().normalize().toFile();
        if (file.listFiles() == null) {
            throw new InvokeIllegalException(file.getAbsolutePath());
        }
        else {
            for (File listFile : file.listFiles()) {
                stringBuilder.append(listFile.getName());
                if (userAgent.toLowerCase().contains(ConstantsFor.OKHTTP)) {
                    stringBuilder.append("\n");
                }
                else {
                    stringBuilder.append("<br>");
                }
            }
        }
        return stringBuilder.toString();
    }

}