package com.dmsvo.offlinealchemy.classes.base;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by DmSVo on 13.01.2018.
 */

@Entity
public class Tag implements Comparable<Tag> {
    @PrimaryKey
    @NonNull
    private String data;    // собственно, тег
    private int count = 0;      // сколько в бд статей с таким тегом

    public Tag() {}
    public Tag(String tag) { data = tag; }
    public Tag(String tag, int count) { data = tag; this.count = count; }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void inc() { count++; }

    @Override
    public int compareTo(@NonNull Tag tag) {
        if (this == tag) return 0;

        String data2 = tag.getData();
        if (data.equals(data2)) return 0;

        return data.compareTo(data2);
    }
}
