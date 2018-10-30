package ru.vachok.money.mapnav;


import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.util.StringJoiner;

/**
 @since 29.10.2018 (14:09) */
@Component
public class MapperUnit {

    private String userEnt;

    private String mapApiKey;

    private String resultTitle;

    public String getResultTitle() {
        return resultTitle;
    }

    public void setResultTitle(String resultTitle) {
        this.resultTitle = resultTitle;
    }

    public MapperUnit() {
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + "Nav");
        mapApiKey = initProperties.getProps().getProperty("mapapikey");
    }

    public String getMapApiKey() {
        return mapApiKey;
    }

    public void setMapApiKey(String mapApiKey) {
        this.mapApiKey = mapApiKey;
    }

    public String getUserEnt() {
        return userEnt;
    }

    public void setUserEnt(String userEnt) {
        this.userEnt = userEnt;
    }


    @Override
    public String toString() {
        return new StringJoiner("\n", MapperUnit.class.getSimpleName() + "\n", "\n")
            .add("mapApiKey='" + mapApiKey + "'\n")
            .add("resultTitle='" + resultTitle + "'\n")
            .add("userEnt='" + userEnt + "'\n")
            .toString();
    }
}
