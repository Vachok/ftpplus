// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import ru.vachok.networker.statistics.PCStats;
import ru.vachok.networker.statistics.Stats;
import ru.vachok.networker.statistics.WeeklyInternetStats;


/**
 Сбор статы по-недельно
 <p>
 Устойчивость (in/(in+out)): 2/(2+6) = 0.25 (устойчив на 75%);
 
 @see WeeklyInternetStats
 @see PCStats
 @since 08.12.2018 (0:12) */
public class WeekStats extends Stats {
    
    
    private String aboutWhat;
    
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return getInfo();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.aboutWhat = classOption.toString();
    }
    
    public String getInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        if (aboutWhat.equals("inet")) {
            stringBuilder.append(Stats.getInetStats().getInfo());
        }
        else if (aboutWhat.equals("pc")) {
            stringBuilder.append(Stats.getPCStats().getInfo());
        }
        else {
            stringBuilder.append("Please, tell me about what you want STATS?");
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeekStats{");
        sb.append("aboutWhat='").append(aboutWhat).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
