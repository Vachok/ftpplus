package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
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
        readRule();
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
        readRule();
    }

    private void readRule() {
        MailRule newRule = new MailRule();
        int i = 0;
        for (String readLine : fileAsList) {
            int index = i++;
            if (readLine.contains("Priority")) {
                final int indexOfRule = index;
                do {
                    if (readLine.toLowerCase().contains("description")) newRule.setDescription(readLine);
                    if (readLine.toLowerCase().contains("conditions")) newRule.setConditions(readLine);
                    if (readLine.toLowerCase().contains("exceptions")) newRule.setExceptions(readLine);
                    if (readLine.toLowerCase().contains("actions")) newRule.setActions(readLine);
                    if (readLine.toLowerCase().contains("query")) newRule.setQuery(readLine);
                    if (readLine.toLowerCase().contains("name")) newRule.setName(readLine);
                } while (readLine.contains("0.1 (8.0.535.0)"));
                ConstantsFor.MAIL_RULES.put(indexOfRule, newRule);
                String msg = index + " string index.End rule.\nRULE ID IS " + indexOfRule;
                LOGGER.warn(msg);
            }
        }
        String msg = new TForms().fromArrayRules(ConstantsFor.MAIL_RULES, false);
        LOGGER.info(msg);
        msg = ConstantsFor.MAIL_RULES.size() + " rules map size";
        LOGGER.warn(msg);
    }
}
