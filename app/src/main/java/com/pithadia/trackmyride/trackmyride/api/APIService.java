package com.pithadia.trackmyride.trackmyride.api;

import com.pithadia.trackmyride.trackmyride.data.Data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by rakshitpithadia on 3/30/18.
 */

public interface APIService {

    @POST("/services/{channel_key}")
    Call<Data> sendLocation(@Path(value = "channel_key") String channelKey, @Body Data data);
}
