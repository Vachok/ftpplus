package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

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
        fileAsStrings();
    }

    /**
     <b> {@link ExCTRL#uplFile(MultipartFile, Model)} </b>

     @return {@link ConstantsFor#MAIL_RULES}.values()
     */
    String getOFields() {

        StringBuilder stringBuilder = new StringBuilder();
        Consumer<String> consumer = (x) -> stringBuilder
            .append(x)
            .append("\n");
        try {
            for (MailRule mailRule : ConstantsFor.MAIL_RULES.values()) {
                mailRule.getOtherFields().forEach(consumer);
            }
        } catch (NullPointerException ignore) {
            //
        }
        return stringBuilder.toString();
    }

    String fileAsStrings() {
        try {
            getRulesFromFile();
        } catch (NullPointerException e) {
            return e.getMessage();
        }
        return new String(new TForms().fromArray(fileAsList, false).getBytes(), StandardCharsets.UTF_8) + "<p>";
    }

    /**
     <b>Преобразование файла в {@link List}</b>

     @see #fileAsList
     */
    private void getRulesFromFile() {
        Charset charset = StandardCharsets.UTF_16;
        ConstantsFor.MAIL_RULES.clear();
        fileAsList.clear();
        try (InputStream inputStream = file.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream)) {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while (bufferedReader.ready()) {
                byte[] bytes = bufferedReader.readLine().getBytes();
                ByteBuffer wrap = ByteBuffer.wrap(bytes);
                CharBuffer decode = charset.decode(wrap);

                fileAsList.add(new String(decode.array()).trim());
            }
            readRule();
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**<b>Чтение и парсинг {@link #fileAsList}</b>
     */
    private void readRule() {
        for (String s : fileAsList) {
            int index = fileAsList.indexOf(s);
            if (s.contains("Priority")) {
                setRule(index);
            }
        }
        String msg = new TForms().fromArrayRules(ConstantsFor.MAIL_RULES, false);
        LOGGER.info(msg);
        msg = ConstantsFor.MAIL_RULES.size() + " rules map size";
        LOGGER.warn(msg);
    }

    /**
     <b>Установщик {@link MailRule}</b>

     @param start внутренний ID правила. {@link #readRule()}
     */
    private void setRule(int start)  {
        ConcurrentMap<Integer, MailRule> map = ConstantsFor.MAIL_RULES;
        MailRule newRule = new MailRule();
        List<String> otherFields = new ArrayList<>();
        newRule.setRuleID(start);
        List<String> ruleList = fileAsList.subList(start, fileAsList.size());
        for (int i = 0; i < ruleList.size(); i++) {
            String s = new String(ruleList.get(i).getBytes());
            try {
                if (s.toLowerCase().contains("description")) newRule.setDescription(s.split(" : ")[1]);
                if (s.toLowerCase().contains("conditions")) newRule.setConditions(s.split(" : ")[1]);
                if (s.toLowerCase().contains("exceptions")) newRule.setExceptions(s.split(" : ")[1]);
                if (s.toLowerCase().contains("actions")) newRule.setActions(s.split(" : ")[1]);
                if (s.toLowerCase().contains("query")) newRule.setQuery(s.split(" : ")[1]);
                if (s.toLowerCase().contains("name")) newRule.setName(s.split(" : ")[1]);
                if (s.toLowerCase().contains("state")) newRule.setName(s.split(" : ")[1]);
                if (s.contains("0.1 (8.0.535.0)")) {
                    break;
                } else otherFields.add(s);
            } catch (ArrayIndexOutOfBoundsException e) {
                otherFields.add(s);
                String msg = otherFields.size() + " otherFields";
                LOGGER.warn(msg);
            }
            map.put(ruleList.size() - start, newRule);
        }
        String msg = newRule.getRuleID() + " ID. End rule.\nRULE name IS " + newRule.getName() + ", rules size is " + ConstantsFor.MAIL_RULES.size();
        LOGGER.warn(msg);
        newRule.setOtherFields(otherFields);
    }

    private void getStaticRulesFile() {
        try (InputStream inputStream = getClass().getResourceAsStream("/static/texts/rules.txt");
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (bufferedReader.ready()) {
                fileAsList.add(new String(bufferedReader.readLine().getBytes(), "UNICODE"));
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        readRule();
    }
}
