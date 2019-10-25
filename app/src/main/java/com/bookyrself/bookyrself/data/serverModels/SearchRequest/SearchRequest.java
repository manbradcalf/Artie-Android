package com.bookyrself.bookyrself.data.ServerModels.SearchRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchRequest {

    @SerializedName("index")
    @Expose
    private String index;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("query")
    @Expose
    private RequestBody requestBody;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

}
