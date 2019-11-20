package ru.vachok.networker.ad.inet;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.info.InformationFactory;

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
            throw new InvokeIllegalException(MessageFormat.format("SETTER FOR {0} class", SSHFactory.class.getTypeName()));
        }
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.sshCom = aboutWhat;
        SSHFactory factory = new SSHFactory.Builder(new AppComponents().sshActs().whatSrvNeed(), this.sshCom, this.getClass().getSimpleName()).build();
        String lsEtcPf = factory.call();
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
