package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.security.SecureRandom;

/**
 * @since 10.09.2018 (11:49)
 */
@Service("passgen")
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
