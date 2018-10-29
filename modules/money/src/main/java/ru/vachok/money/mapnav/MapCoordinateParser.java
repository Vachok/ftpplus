package ru.vachok.money.mapnav;


/**
 @since 29.10.2018 (14:32) */
class MapCoordinateParser {

    private String userEnt;

    MapCoordinateParser(String userEnt) {
        this.userEnt = userEnt;
    }

    String whatAreYouDoing() {
        StringBuilder stringBuilder = new StringBuilder();
        if (userEnt.contains("-")) {
            String startStr = userEnt.split("-")[0].trim();
            String stopStr = userEnt.split("-")[1].trim();
            stringBuilder
                .append(startStr)
                .append(" точка старта.<br>")
                .append(stopStr)
                .append(" точка стопа.")
                .append("<p>")
                .append(new DistanceCalc().getKilometers(startStr, stopStr));
        } else {
            stringBuilder
                .append("Hello. К сожалению я не могу прочесть то, что вы ввели:<br>")
                .append("<code>" + userEnt + "</code>")
                .append("<br>")
                .append("Пожалуйста, вводите по-шаблону \"Точка старта - Точка стопа\", и я постараюсь вычислить для вас километраж.");
        }
        return stringBuilder.toString();
    }
}
