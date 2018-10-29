package ru.vachok.money.mapnav;


import org.springframework.stereotype.Component;

import java.util.StringJoiner;

/**
 @since 29.10.2018 (14:09) */
@Component
public class MapperUnit {

    private String userEnt;

    public String getUserEnt() {
        return userEnt;
    }

    public void setUserEnt(String userEnt) {
        this.userEnt = userEnt;
    }


    @Override
    public String toString() {
        return new StringJoiner("\n", MapperUnit.class.getSimpleName() + "\n", "\n")
            .add("userEnt='" + userEnt + "'\n")
            .toString();
    }
}
