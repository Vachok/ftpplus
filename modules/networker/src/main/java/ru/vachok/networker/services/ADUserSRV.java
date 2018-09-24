package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;


public class ADUserSRV {

    private static final Logger LOGGER = AppComponents.getLogger();

    private ADUser adUser = new ADUser();

    public List<String> adFileReader() {
        List<String> strings = new ArrayList<>();
        File adUsers = new File("allmailbox.txt");
        BufferedReader bufferedReader;
        try (FileReader fileReader = new FileReader(adUsers)) {
            bufferedReader = new BufferedReader(fileReader);
            while (bufferedReader.ready()) {
                strings.add(bufferedReader.readLine());
            }
        } catch (IOException | InputMismatchException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info(adUser.toString());
        return strings;
    }
}

