package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
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

        for (String s : fileAsList) {
            int index = fileAsList.indexOf(s);
            try {
                s = new String(s.getBytes(), "UNICODE");
                if (s.contains("Priority")) {
                    setRule(index);
                }
            } catch (UnsupportedEncodingException | CharacterCodingException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        String msg = new TForms().fromArrayRules(ConstantsFor.MAIL_RULES, false);
        LOGGER.info(msg);
        msg = ConstantsFor.MAIL_RULES.size() + " rules map size";
        LOGGER.warn(msg);
    }

    private void setRule(int start) throws CharacterCodingException, UnsupportedEncodingException {

        ConcurrentMap<Integer, MailRule> map = ConstantsFor.MAIL_RULES;
        MailRule newRule = new MailRule();
        List<String> otherFields = new ArrayList<>();
        newRule.setRuleID(start);
        List<String> ruleList = fileAsList.subList(start, fileAsList.size());
        for (int i = 0; i < ruleList.size(); i++) {
            String s1 = new String(ruleList.get(i).getBytes(), "UNICODE");
            String s = encodeTo(s1);
            try {
                if (s.toLowerCase().contains("description")) newRule.setDescription(s.split(" : ")[1]);
                if (s.toLowerCase().contains("conditions")) newRule.setConditions(s.split(" : ")[1]);
                if (s.toLowerCase().contains("exceptions")) newRule.setExceptions(s.split(" : ")[1]);
                if (s.toLowerCase().contains("actions")) newRule.setActions(s.split(" : ")[1]);
                if (s.toLowerCase().contains("query")) newRule.setQuery(s.split(" : ")[1]);
                if (s.toLowerCase().contains("name")) newRule.setName(s.split(" : ")[1]);
                if (s.toLowerCase().contains("state")) newRule.setName(s.split(" : ")[1]);
                if (s.contains("0.1 (8.0.535.0)")) {
                    map.put(ruleList.size() - start, newRule);
                    break;
                } else otherFields.add(s);
            } catch (ArrayIndexOutOfBoundsException e) {
                otherFields.add(s);
                String msg = otherFields.size() + " otherFields";
                LOGGER.warn(msg);
            }
        }
        String msg = newRule.getRuleID() + " ID. End rule.\nRULE name IS " + newRule.getName() + ", rules size is " + ConstantsFor.MAIL_RULES.size();
        LOGGER.warn(msg);
        newRule.setOtherFields(otherFields);
    }

    private String encodeTo(String s1) throws CharacterCodingException {
        CharsetEncoder utf8 = StandardCharsets.UTF_8.newEncoder();
        CharsetEncoder defEncoder = Charset.defaultCharset().newEncoder();
        char[] chars = new char[s1.length()];
        s1.getChars(0, s1.length(), chars, 0);
        CharBuffer wrap = CharBuffer.wrap(chars);
        ByteBuffer encode = utf8.encode(wrap);
        encode.asCharBuffer().append(wrap);
        String s = new String(encode.array());
        encode = defEncoder.encode(wrap);
        s = s + " | " + new String(encode.array());
        return s;
    }


}
