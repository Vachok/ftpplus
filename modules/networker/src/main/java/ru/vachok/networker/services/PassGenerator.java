package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.networker.AppComponents;

import java.security.SecureRandom;

/**
 * @since 10.09.2018 (11:49)
 */
public class PassGenerator {

    private static final Logger LOGGER = AppComponents.getLogger(PassGenerator.class.getSimpleName());

    private static final int BYTES_LEN_BY_DEFAULT = 30;

    public String generatorPass(int howMuchSym) {
        if(howMuchSym <= 0){
            howMuchSym = BYTES_LEN_BY_DEFAULT;
        }
        SecureRandom instanceStrong = new SecureRandom();
        byte[] bytes = instanceStrong.generateSeed(howMuchSym);
        String youNewPass = new String(bytes);
        String msg = youNewPass + " your password\n" + instanceStrong.getAlgorithm();
        LOGGER.warn(msg);
        return youNewPass;
    }
}
