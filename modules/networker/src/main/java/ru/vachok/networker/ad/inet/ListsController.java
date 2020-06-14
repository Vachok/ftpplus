package ru.vachok.networker.ad.inet;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.text.MessageFormat;


/**
 @since 20.11.2019 (14:10) */
public class ListsController implements InformationFactory {


    private String sshCom;

    @SuppressWarnings("InstanceVariableOfConcreteClass") private SSHFactory sshFactory;

    @Override
    public String getInfo() {
        this.sshCom = "sudo ls /etc/pf";
        return getInfoAbout(sshCom);
    }

    @Override
    public void setClassOption(Object option) {
        if (option instanceof SSHFactory) {
            this.sshFactory = (SSHFactory) option;
        }
        else {
            throw new IllegalArgumentException(MessageFormat.format("SETTER FOR {0} class", SSHFactory.class.getTypeName()));
        }
    }

    @Override
    public String getInfoAbout(String aboutWhat) {
        this.sshCom = aboutWhat;
        SSHFactory factory = new SSHFactory.Builder(new AppComponents().sshActs().whatSrvNeed(), this.sshCom, this.getClass().getSimpleName()).build();
        String lsEtcPf = AppConfigurationLocal.getInstance().submitAsString(factory, 10);
        lsEtcPf = lsEtcPf.replaceAll("<br>", "");
        return lsEtcPf;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ListsController{");
        sb.append("sshFactory=").append(sshFactory);
        sb.append(", sshCom='").append(sshCom).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
