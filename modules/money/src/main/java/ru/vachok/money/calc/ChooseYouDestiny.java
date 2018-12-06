package ru.vachok.money.calc;


import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.security.SecureRandom;
import java.util.Properties;


/**
 @since 31.10.2018 (19:38) */
@Component ("destiny")
public class ChooseYouDestiny {

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

    String destinyCooser(String[] strings) {
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);
        Properties properties = initProperties.getProps();
        int secureRandom = new SecureRandom().nextInt(strings.length);
        for(int i = 0; i < strings.length; i++){
            if(strings[i].toLowerCase().contains(", ")){
                strings[i] = String.valueOf(properties.getOrDefault("ans", "Тогда выбери: <br>"));
                return strings[i] + strings[secureRandom];
            }
            else{
                return strings[secureRandom];
            }
        }
        throw new IllegalStateException();
    }
}