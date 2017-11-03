
package com.bookyrself.bookyrself.models.searchrequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Date {

    @SerializedName("gte")
    @Expose
    private String gte;
    @SerializedName("lte")
    @Expose
    private String lte;

    public String getGte() {
        return gte;
    }

    public void setGte(String gte) {
        this.gte = gte;
    }

    public String getLte() {
        return lte;
    }

    public void setLte(String lte) {
        this.lte = lte;
    }

}
