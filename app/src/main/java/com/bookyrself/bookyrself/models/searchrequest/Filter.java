
package com.bookyrself.bookyrself.models.searchrequest;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Filter {

    @SerializedName("bool")
    private Bool mBool;

    public Bool getBool() {
        return mBool;
    }

    public void setBool(Bool bool) {
        mBool = bool;
    }

}
