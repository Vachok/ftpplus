// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.stats;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.info.InformationFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;


/**
 @see StatsTest
 @since 19.05.2019 (23:04) */
public interface Stats extends InformationFactory {


    static long getIpsInet() {
        try {
            Files.deleteIfExists(new File(FileNames.INETSTATSIP_CSV).toPath());
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new WeeklyInternetStats().readIPsWithInet(true);
    }

    static boolean isSunday() {
        String osName = System.getProperty("os.name");
        return !osName.toLowerCase().contains("free") && !osName.toLowerCase().contains("lin") && LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

    @Contract("_ -> new")
    static @NotNull Stats getInstance(@NotNull String type) {
        switch (type) {
            case InformationFactory.STATS_WEEKLY_INTERNET:
                return new WeeklyInternetStats();
            case InformationFactory.STATS_SUDNAY_PC_SORT:
                return new ComputerUserResolvedStats();
            default:
                throw new IllegalArgumentException(MessageFormat.format("NOT CORRECT INSTANCE: {0} in {1}", type, Stats.class.getSimpleName()));
        }
    }

    @Override
    String getInfoAbout(String aboutWhat);

    @Override
    String getInfo();

    @Override
    void setClassOption(Object option);
}
