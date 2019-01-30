package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.ad.ActDirectoryCTRL;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 {@link ActionListener} для {@link ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)}
 <p>

 @see MessageToTray
 @since 30.01.2019 (10:01) */
public class ListenUserInfo implements ActionListener {

    private final String queryString;

    private final String attributeValue;

    private final String finalAdSrvDetails;

    public ListenUserInfo(String queryString, String attributeValue, String finalAdSrvDetails) {
        this.queryString = queryString;
        this.attributeValue = attributeValue;
        this.finalAdSrvDetails = finalAdSrvDetails;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new MessageSwing(737, 737, 66, 30).infoNoTitles(queryString + "\n\n" + attributeValue + "\n" + finalAdSrvDetails);
    }
}
