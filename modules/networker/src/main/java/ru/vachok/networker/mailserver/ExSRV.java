// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.controller.ExCTRL;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.UsefulUtilites;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 @since 05.10.2018 (9:56)
 @see ru.vachok.networker.mailserver.ExSRVTest*/
@Service(ModelAttributeNames.ATT_EXSRV)
public class ExSRV {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExSRV.class.getSimpleName());
    
    /**
     Файл правил-транспорта.
     <p>
     {@code Get-TransportRule | fl > this.file )}
     */
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }
    
    private ConcurrentMap<Integer, MailRule> localMap = UsefulUtilites.getMailRules();
    
    private Queue<String> fileAsQueue = new LinkedList<>();
    
    public void setFile(MultipartFile file) {
        this.file = file;
    }

    /**
     <b> {@link ExCTRL#uplFile(MultipartFile, Model)} </b>

     @return {@link ConstantsFor#MAIL_RULES}.values()
     */
    public String getOFields() {

        StringBuilder stringBuilder = new StringBuilder();
        Consumer<String> consumer = (x) -> stringBuilder
            .append(x)
            .append("\n");
        try {
            for (MailRule mailRule : UsefulUtilites.getMailRules().values()) {
                mailRule.getOtherFields().forEach(consumer);
            }
        } catch (NullPointerException ignore) {
            //
        }
        return stringBuilder.toString();
    }
    
    public String fileAsStrings() {
        try {
            getRulesFromFile();
        } catch (NullPointerException e) {
            return e.getMessage();
        }
        return new String(new TForms().fromArray(fileAsQueue, false).getBytes(), StandardCharsets.UTF_8) + "<p>";
    }

    /**
     <b>Преобразование файла в {@link List}</b>
 
     @see #fileAsQueue
     */
    private void getRulesFromFile() {
        Charset charset = StandardCharsets.UTF_16;
        localMap.clear();
        fileAsQueue.clear();
        try (InputStream inputStream = file.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            while (inputStreamReader.ready()) {
                fileAsQueue.add(bufferedReader.readLine());
                readRule();
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**<b>Чтение и парсинг {@link #fileAsQueue}</b>
     */
    private void readRule() {
        while (fileAsQueue.iterator().hasNext()) {
            String stringFromFile = fileAsQueue.poll();
            if (Objects.requireNonNull(stringFromFile).contains("Priority")) {
                throw new IllegalStateException("22.06.2019 (17:14)");
            }
        }
        String msg = MailRule.fromArrayRules(localMap, false);
        LOGGER.info(msg);
        msg = localMap.size() + " rules map size";
        LOGGER.warn(msg);
    }

    /**
     <b>Установщик {@link MailRule}</b>

     @param start внутренний ID правила. {@link #readRule()}
     */
    private void setRule(int start)  {
        ConcurrentMap<Integer, MailRule> map = localMap;
        MailRule newRule = new MailRule();
        List<String> otherFields = new ArrayList<>();
        newRule.setRuleID(start);
        throw new IllegalStateException("22.06.2019 (17:15)");
        
        /* 22.06.2019 (17:15)
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
        String msg = newRule.getRuleID() + " ID. End rule.\nRULE name IS " + newRule.getName() +
            ", rules size is " + ConstantsFor.getMailRules().size() + "/" + localMap.size();
        LOGGER.warn(msg);
        newRule.setOtherFields(otherFields);*/
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExSRV{");
        sb.append(", localMap=").append(localMap.size());
        sb.append(", fileAsQueue=").append(fileAsQueue.size());
        sb.append('}');
        return sb.toString();
    }
}
