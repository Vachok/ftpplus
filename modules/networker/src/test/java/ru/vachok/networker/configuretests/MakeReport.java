package ru.vachok.networker.configuretests;


import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 @since 17.07.2019 (9:35) */
public class MakeReport implements TestConfigure {


    private MessageToUser messageToUser;

    private final String testName;

    public MakeReport(String testName) {
        this.testName = testName;

    }

    @Override
    public PrintStream getPrintStream() throws IOException {
        if (!new File(TEST_FOLDER).exists()) {
            Files.createDirectories(Paths.get(TEST_FOLDER));
        }
        OutputStream outputStream = new FileOutputStream(TEST_FOLDER + testName);
        return new PrintStream(outputStream, true);

    }

    @Override
    public void before() {
        throw new InvokeEmptyMethodException("17.07.2019 (10:00)");

    }

    @Override
    public void after() {
        throw new InvokeEmptyMethodException("17.07.2019 (10:00)");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MakeReport{");
        sb.append(", testName='").append(testName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
