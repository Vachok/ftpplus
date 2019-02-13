package ru.vachok.networker.ad.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 Работа с правами пользователя.

 @since 13.02.2019 (15:43) */
@Service
public class UserRightsOnCommon {

    private ADUser adUser;

    @Autowired
    public UserRightsOnCommon(ADUser adUser) {
        this.adUser = adUser;
    }


}
