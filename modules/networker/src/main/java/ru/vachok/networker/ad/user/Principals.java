package ru.vachok.networker.ad.user;


import com.adauthenticator.ADAuthenticator;
import com.adauthenticator.ADAuthenticatorFactory;
import com.adauthenticator.model.ADGroup;
import com.adauthenticator.model.ADUser;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.services.MessageLocal;

import java.util.Collection;

/**
 *
 */
public class Principals {

    private static String userInputName = null;

    private static MessageToUser messageToUser = new MessageLocal();

    public static void setUserInputName(String userInputName) {
        Principals.userInputName = userInputName;
    }

    public static void main(String[] args) {
        ADAuthenticator adAuthenticator = ADAuthenticatorFactory.newInstance("eatmeat.ru");
        boolean isAuthenticate = adAuthenticator.authenticate("ikudryashov", "");
        messageToUser.infoNoTitles("isAuthenticate " + isAuthenticate);
        ADUser loggedInUser = adAuthenticator.retrieveLoggedInUser();
        Collection<ADGroup> loggedInUserGroups = loggedInUser.getGroups();
        for (ADGroup inUserGroup : loggedInUserGroups) {
            messageToUser.infoNoTitles(inUserGroup.getOrganizationalUnitNames().toString());
        }
    }
}
