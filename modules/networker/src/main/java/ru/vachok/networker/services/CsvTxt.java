package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.TForms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 @since 11.10.2018 (9:16) */
public class CsvTxt {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvTxt.class.getSimpleName());

    private List<String> csvList = new ArrayList<>();

    private MultipartFile file;

    private List<String> xlsList = new ArrayList<>();

    private List<String> txtList = new ArrayList<>();

    private List<String> psCommandsList = new ArrayList<>();
    
    public List<String> getPsCommandsList() {
        parseCSV();
        return psCommandsList;
    }

    private ConcurrentMap<String, File> files = new ConcurrentHashMap<>();

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    private void parseCSV() throws ArrayIndexOutOfBoundsException {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> displNames = new ArrayList<>();
        ConcurrentMap<String, String> odinAssList = new ConcurrentHashMap<>();
        txtList.forEach(x -> {
            if (x.contains("DisplayName")) {
                try {
                    String replace = x.split(" : ")[1].replace(", ", ",");
                    displNames.add(replace);
                } catch (ArrayIndexOutOfBoundsException ignore) {
                    //
                }

            }
        });
        csvList.forEach(x -> {
            String[] strings = x.split(";");
            try {
                strings[1] = strings[1].replaceFirst(" ", ",").split(" ")[0];
                odinAssList.put(strings[1].replaceFirst(",", ", "), strings[4]);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                //
            }

            stringBuilder.append("<p>");
        });
        equivalentList(odinAssList);
        String fromArray = new TForms().fromArray(odinAssList, true);
        String msg = new String(fromArray.getBytes(), StandardCharsets.UTF_8);
        LOGGER.info(msg);
    }

    private void equivalentList(ConcurrentMap<String, String> odinAssList) {
        Set<String> stringSet = odinAssList.keySet();
        for (String x : stringSet) {
            String commStr = "Set-User -Identity \"" +
                x +
                "\" -Title \"" +
                odinAssList.get(x) +
                "\"";
            psCommandsList.add(new String(commStr.getBytes(), StandardCharsets.UTF_8));
        }
    }
    
    public ConcurrentMap<String, File> getFiles() {
        return files;
    }
    
    public String readFileToString() {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
             BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            while (bufferedReader.ready()) {
                if (file.getOriginalFilename().toLowerCase().contains(".xls")) xlsList.add(bufferedReader.readLine());
                else if (file.getOriginalFilename().toLowerCase().contains(".txt")) txtList.add(bufferedReader.readLine());
                else if (file.getOriginalFilename().toLowerCase().contains(".csv")) csvList.add(bufferedReader.readLine());
                else return "File is not TXT, XLS or CSV";
            }
            File dest = new File("file");
            this.file.transferTo(dest);
            chkFile(dest, stringBuilder);
            stringBuilder
                .append("<h2><center>MAPPED ")
                .append(files.size())
                .append(" Files</h2></center>");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return e.getMessage();
        }
        return stringBuilder.toString();
    }
    
    public List<String> getXlsList() {
        return xlsList;
    }

    String parseXlsx() {
        AtomicReference<String> keyName = new AtomicReference<String>();
        Set<String> strings = files.keySet();
        strings.iterator().forEachRemaining(x -> {
            if (x.contains(".xls")) keyName.set(x);
        });
        return new String((keyName.get() + "<br>" + new TForms().fromArray(csvList, true) + "<p>" +
            new TForms().fromArray(txtList, true)).getBytes(), StandardCharsets.UTF_8);
    }
    
    public List<String> getTxtList() {
        return txtList;
    }

    private void chkFile(File dest, StringBuilder stringBuilder) {
        boolean namesCont = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().contains(".xls") ||
            file.getOriginalFilename().toLowerCase().contains(".txt") ||
            file.getOriginalFilename().toLowerCase().contains(".csv");
        if (!namesCont) stringBuilder.append("File does not excel or text");
        else if (files.size() < 2) files.put(file.getOriginalFilename(), dest);
        else stringBuilder
                .append("MAP size is ")
                .append(files.size())
                .append("<br>")
                .append(new TForms().fromArray(files, true));
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CsvTxt{");
        sb.append("csvList=").append(csvList);
        sb.append(", xlsList=").append(xlsList);
        sb.append(", txtList=").append(txtList);
        sb.append(", psCommandsList=").append(psCommandsList);
        sb.append(", files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
