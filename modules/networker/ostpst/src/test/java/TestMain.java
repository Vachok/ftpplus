import org.testng.annotations.Test;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;


public class TestMain {
    
    
    @Test
    public void launchProg() {
        MakeConvert makeConvert = new OstToPst();
        MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
        makeConvert.showFileContent();
    }
}
