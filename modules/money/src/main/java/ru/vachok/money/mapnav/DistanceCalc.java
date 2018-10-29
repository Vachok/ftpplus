package ru.vachok.money.mapnav;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 @since 29.10.2018 (14:45) */
class DistanceCalc {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistanceCalc.class.getSimpleName());

    private static File file = new File("map.html");

    long getKilometers(String startStr, String stopStr) {
        URL url = null;
        String spec = "https://www.google.com/maps/dir/" + startStr + "/" + stopStr;
        try {
            url = new URL(spec);
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try (InputStream inputStream = url.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            BufferedReader br = new BufferedReader(inputStreamReader);
            while (br.ready()) {
                fileOutputStream.write(br.readLine().getBytes());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        scriptExecutor();
        return file.length();
    }

    void scriptExecutor() {
        ScriptEngineManager scriptEngine = new ScriptEngineManager();
        ScriptEngine nashorn = scriptEngine.getEngineByName("nashorn");
        try (Reader reader = new FileReader(new File("map.js"))) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (bufferedReader.ready()) {
                LOGGER.info(bufferedReader.readLine().replace(";", ";\n"));
            }
            ScriptContext context = nashorn.getContext();
            Bindings bindings = nashorn.createBindings();
            bindings.forEach((x, y) -> {
                LOGGER.info(x);
                LOGGER.info(y.toString());
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}
