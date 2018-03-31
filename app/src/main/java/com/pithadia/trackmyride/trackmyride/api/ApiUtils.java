package com.pithadia.trackmyride.trackmyride.api;

/**
 * Created by rakshitpithadia on 3/30/18.
 */

public class ApiUtils {

    private static APIService apiService;

    private ApiUtils() {}

    public static final String BASE_URL = "https://hooks.slack.com/";

    public static APIService getAPIService() {

        if (apiService == null) {
            apiService = RetrofitClient.getClient(BASE_URL).create(APIService.class);
        }
        return apiService;
    }
}
