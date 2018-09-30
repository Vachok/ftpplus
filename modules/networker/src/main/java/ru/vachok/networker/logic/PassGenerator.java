package ru.vachok.networker.logic;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.security.SecureRandom;

/**
 * @since 10.09.2018 (11:49)
 */
public class PassGenerator {

    private static final Logger LOGGER = AppComponents.getLogger();

    public String generatorPass(int howMuchSym) {
        if (howMuchSym <= 0) howMuchSym = 30;
        SecureRandom instanceStrong = new SecureRandom();
        byte[] bytes = instanceStrong.generateSeed(howMuchSym);
        String youNewPass = new String(bytes);
        String msg = youNewPass + " your password";
        LOGGER.warn(msg);
        return youNewPass;
    }
}
