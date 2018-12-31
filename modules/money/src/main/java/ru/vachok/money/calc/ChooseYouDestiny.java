package ru.vachok.money.calc;


import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;

import java.security.SecureRandom;


/**
 @since 31.10.2018 (19:38) */
@Component (ConstantsFor.DESTINY)
public class ChooseYouDestiny {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ChooseYouDestiny.class.getSimpleName();

    private String variantOne;

    private String variantTwo;

    private String variantThree;

    private String variantFour;

    public String getVariantOne() {
        return variantOne;
    }

    public void setVariantOne(String variantOne) {
        this.variantOne = variantOne;
    }

    public String getVariantTwo() {
        return variantTwo;
    }

    public void setVariantTwo(String variantTwo) {
        this.variantTwo = variantTwo;
    }

    public String getVariantThree() {
        return variantThree;
    }

    public void setVariantThree(String variantThree) {
        this.variantThree = variantThree;
    }

    public String getVariantFour() {
        return variantFour;
    }

    public void setVariantFour(String variantFour) {
        this.variantFour = variantFour;
    }

    String destinyChooser() {
        String[] strings = {variantOne, variantTwo, variantThree, variantFour};
        int secureRandom = new SecureRandom().nextInt(strings.length);
        return strings[secureRandom];
    }
}