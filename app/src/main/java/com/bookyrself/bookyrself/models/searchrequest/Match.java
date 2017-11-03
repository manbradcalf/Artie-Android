
package com.bookyrself.bookyrself.models.searchrequest;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Match {

    @SerializedName("citystate")
    private String mCitystate;

    public String getCitystate() {
        return mCitystate;
    }

    public void setCitystate(String citystate) {
        mCitystate = citystate;
    }

}
