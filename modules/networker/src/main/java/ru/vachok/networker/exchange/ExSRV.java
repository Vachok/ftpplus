package ru.vachok.networker.exchange;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.TForms;

import java.io.IOException;
import java.io.InputStream;

/**
 @since 05.10.2018 (9:56) */
@Service("exsrv")
public class ExSRV {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExSRV.class.getSimpleName());

    private RulesBean rulesBean;

    private MultipartFile file;

    public ExSRV(RulesBean rulesBean) {
        this.rulesBean = rulesBean;
    }

    public RulesBean getRulesBean() {
        return rulesBean;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getRulesFromFile() {
        String s;
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = new byte[inputStream.available()];
            while (inputStream.available() > 0) {
                int read = inputStream.read(bytes);
                String msg = read + " bytes read.";
                LOGGER.info(msg);
            }
            s = new String(bytes);
        } catch (IOException e) {
            return e.getMessage() + new TForms().fromArray(e, true);
        }
        return s;
    }
}
