
package com.bookyrself.bookyrself.models.searchrequest;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Date {

    @SerializedName("gte")
    private String mGte;
    @SerializedName("lte")
    private String mLte;

    public String getGte() {
        return mGte;
    }

    public void setGte(String gte) {
        mGte = gte;
    }

    public String getLte() {
        return mLte;
    }

    public void setLte(String lte) {
        mLte = lte;
    }

}
