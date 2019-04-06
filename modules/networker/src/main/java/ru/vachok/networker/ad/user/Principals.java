package ru.vachok.networker.ad.user;



import com.adauthenticator.ADAuthenticator;
import com.adauthenticator.ADAuthenticatorFactory;
import com.adauthenticator.model.ADGroup;
import com.adauthenticator.model.ADUser;

import java.awt.*;
import java.util.Collection;

/**
 *
 */
public class Principals {
    
    
    private static String userInputName;
    
    public static void setUserInputName(String userInputName) {
        Principals.userInputName = userInputName;
    }

    public static void main(String[] args) {
        ADAuthenticator adAuthenticator = ADAuthenticatorFactory.newInstance("eatmeat.ru");
        boolean isAuthenticate = adAuthenticator.authenticate("ikudryashov", "netzero0912");
        ADUser loggedInUser = adAuthenticator.retrieveLoggedInUser();
        Collection<ADGroup> loggedInUserGroups = loggedInUser.getGroups();
    
        for (ADGroup inUserGroup : loggedInUserGroups) {
            throw new IllegalComponentStateException("Not Comply");
        }
    }
}
