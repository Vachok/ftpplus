package ru.vachok.money.mapnav;


/**
 @since 29.10.2018 (14:32) */
class MapCoordinateParser {

    private StringBuilder stringBuilder = new StringBuilder();

    private String startStr;

    private String stopStr;

    private String userEnt;

    MapCoordinateParser(String userEnt) {
        this.userEnt = userEnt;
        if (userEnt.contains("-")) {
            startStr = userEnt.split("-")[0].trim();
            stopStr = userEnt.split("-")[1].trim();
        } else if (userEnt.isEmpty() || userEnt.isBlank()) {
            startStr = "Павловская Слобода";
            stopStr = "Истра";
        } else {
            stringBuilder
                .append("Hello. К сожалению я не могу прочесть то, что вы ввели:<br><code>")
                .append(userEnt)
                .append("</code><br>Пожалуйста, вводите по-шаблону \"Точка старта - Точка стопа\", и я постараюсь вычислить для вас километраж.");
        }
    }

    String getResultsAsText() {
        String info = new DistanceCalc(startStr, stopStr).getInfo();
        stringBuilder.append(startStr)
            .append(" точка старта.<br>")
            .append(stopStr)
            .append(" точка стопа.")
            .append("<p>")
            .append(info);
        return stringBuilder.toString();
    }
}
