package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;


public class SetADUser {

    private static final AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();

    private static final Logger LOGGER = AppComponents.getLogger();

    private ADUser adUser = ctx.getBean(ADUser.class);

    public static void main(String[] args) {
        new SetADUser().adFileReader();
    }

    public List<String> adFileReader() {
        List<String> strings = new ArrayList<>();
        File adUsers = new File("allmailbox.txt");
        BufferedReader bufferedReader = null;
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

