package ru.vachok.networker.exchange;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.TForms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    private List<String> fileAsList = new ArrayList<>();

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    String fileAsStrings(boolean init) {
        if (init) getStaticRulesFile();
        else getRulesFromFile();
        return new String(new TForms().fromArray(fileAsList).getBytes(), StandardCharsets.UTF_8) + "<p>" + rulesBean.toString();
    }

    private void getStaticRulesFile() {
        try (InputStream inputStream = getClass().getResourceAsStream("/static/texts/rules.txt");
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (bufferedReader.ready()) {
                fileAsList.add(bufferedReader.readLine());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void getRulesFromFile() {
        try (InputStream inputStream = file.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while (bufferedReader.ready()) {
                String readLine = bufferedReader.readLine();
                fileAsList.add(readLine);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            getStaticRulesFile();
        }
    }

    private RulesBean setRulesBeans(String readLine) {
        RulesBean newRule = new RulesBean();
        if (readLine.toLowerCase().contains("description")) newRule.setDescription(readLine);
        if (readLine.toLowerCase().contains("conditions")) newRule.setConditions(readLine);
        if (readLine.toLowerCase().contains("exceptions")) newRule.setExceptions(readLine);
        if (readLine.toLowerCase().contains("actions")) newRule.setActions(readLine);
        if (readLine.toLowerCase().contains("query")) newRule.setQuery(readLine);
        return newRule;
    }
}
