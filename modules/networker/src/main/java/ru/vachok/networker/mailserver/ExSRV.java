package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 @since 05.10.2018 (9:56) */
@Service("exsrv")
public class ExSRV {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExSRV.class.getSimpleName());

    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    private List<String> fileAsList = new ArrayList<>();

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    String fileAsStrings() {

        try {
            getRulesFromFile();
        } catch (NullPointerException e) {
            return e.getMessage();
        }
        return new String(new TForms().fromArray(fileAsList, false).getBytes(), StandardCharsets.UTF_8) + "<p>";
    }

    private void getRulesFromFile() {
        ConstantsFor.MAIL_RULES.clear();
        fileAsList.clear();
        try (InputStream inputStream = file.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while (bufferedReader.ready()) {
                String readLine = bufferedReader.readLine();
                fileAsList.add(readLine);
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
            getStaticRulesFile();
        }
        readRule(0);
    }

    private void getStaticRulesFile() {

        try (InputStream inputStream = getClass().getResourceAsStream("/static/texts/rules.txt");
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (bufferedReader.ready()) {
                fileAsList.add(bufferedReader.readLine());
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        readRule(0);
    }

    private void readRule(int start) {
        List<String> sub = fileAsList.subList(start, fileAsList.size());
        for (int index = 0; index < sub.size(); index++) {
            String s1 = null;
            try {
                s1 = new String(sub.get(index).getBytes(), "UNICODE");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (s1 != null && s1.contains("Priority")) {
                setRule(index);
            }
        }
        String msg = new TForms().fromArrayRules(ConstantsFor.MAIL_RULES, false);
        LOGGER.info(msg);
        msg = ConstantsFor.MAIL_RULES.size() + " rules map size";
        LOGGER.warn(msg);
    }

    private void setRule(int start) {
        MailRule newRule = new MailRule();
        List<String> ruleList = fileAsList.subList(start, fileAsList.size());
        for (int i = 0; i < ruleList.size(); i++) {
            String s = ruleList.get(i);
            if (s.toLowerCase().contains("description")) newRule.setDescription(s);
            if (s.toLowerCase().contains("conditions")) newRule.setConditions(s);
            if (s.toLowerCase().contains("exceptions")) newRule.setExceptions(s);
            if (s.toLowerCase().contains("actions")) newRule.setActions(s);
            if (s.toLowerCase().contains("query")) newRule.setQuery(s);
            if (s.toLowerCase().contains("name")) newRule.setName(s);
            if (s.contains("0.1 (8.0.535.0)")) {
                ConstantsFor.MAIL_RULES.put(ruleList.size() - start, newRule);
                break;
            }
        }

        String msg = start + " start. End rule.\nRULE ID IS " + (ruleList.size() - start);
        LOGGER.warn(msg);
        if (ruleList.size() - start > 6) readRule(ruleList.size() - start);
    }
}
