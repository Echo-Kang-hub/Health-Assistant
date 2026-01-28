package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PagedResponse<T> {
    @SerializedName("items")
    private List<T> items;
    @SerializedName("page")
    private int page;
    @SerializedName("pageSize")
    private int pageSize;
    @SerializedName("total")
    private int total;

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotal() {
        return total;
    }
}
