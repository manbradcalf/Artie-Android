
package com.bookyrself.bookyrself.models;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class SearchRequest {

    @SerializedName("index")
    private String mIndex;
    @SerializedName("q")
    private String mQ;
    @SerializedName("type")
    private String mType;

    public String getIndex() {
        return mIndex;
    }

    public void setIndex(String index) {
        mIndex = index;
    }

    public String getQ() {
        return mQ;
    }

    public void setQ(String q) {
        mQ = q;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

}
