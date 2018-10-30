package ru.vachok.money.mapnav;


import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.money.services.TForms;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 @since 29.10.2018 (14:45) */
class DistanceCalc {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistanceCalc.class.getSimpleName());

    private String startStr;

    private String stopStr;

    DistanceCalc(String startStr, String stopStr) {
        this.startStr = startStr;
        this.stopStr = stopStr;
    }

    String getInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        String matrixStr = "No Info";
        try {
            DistanceMatrix distanceMatrix = distanceMatrixApiRequest().await();
            DistanceMatrixRow[] rows = distanceMatrix.rows;
            for (DistanceMatrixRow row : rows) {
                stringBuilder.append(new TForms().toStringFromArray(row.elements, true));
            }
            matrixStr = stringBuilder.toString();
            getGeoContext().shutdown();
        } catch (ApiException | InterruptedException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return matrixStr;
    }

    private DistanceMatrixApiRequest distanceMatrixApiRequest() {
        DistanceMatrixApiRequest distanceMatrixApiRequest = DistanceMatrixApi.newRequest(getGeoContext());
        distanceMatrixApiRequest.mode(TravelMode.DRIVING);
        distanceMatrixApiRequest.origins(startStr);
        distanceMatrixApiRequest.destinations(stopStr);
        distanceMatrixApiRequest.units(Unit.METRIC);
        distanceMatrixApiRequest.departureTime(Instant.now());
        distanceMatrixApiRequest.trafficModel(TrafficModel.BEST_GUESS);
        return distanceMatrixApiRequest;
    }

    private GeoApiContext getGeoContext() {
        MapperUnit mapperUnit = new MapperUnit();
        GeoApiContext.Builder builder = new GeoApiContext.Builder();
        builder.apiKey(mapperUnit.getMapApiKey());
        builder.maxRetries(5);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
        return builder.build();
    }
}
