package com.dmsvo.offlinealchemy.classes.db;

import android.arch.persistence.room.TypeConverter;
import android.support.annotation.IntegerRes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by DmSVo on 06.01.2018.
 */

public class Converters {

    final String delimiter = ";";

    @TypeConverter
    public Date fromTimestamp(long value) {
        return value == 0 ? null : new Date(value);
    }

    @TypeConverter
    public long dateToTimestamp(Date date) {
        if (date == null) {
            return 0;
        } else {
            return date.getTime();
        }
    }

    @TypeConverter
    public List<Integer> fromStringToListInt(String s)
    {
        List<Integer> result = new ArrayList<>();

        if (!s.isEmpty()) {
            String[] arr = s.split(delimiter);

            for (String el : arr) {
//                try {
                    result.add(Integer.parseInt(el));
//                } catch (Throwable e){
//
//                }
            }
        }
        return result;
    }

    @TypeConverter
    public String fromListIntToString(List<Integer> ints)
    {
        String result = "";
        for (Integer el : ints)
        {
            result += el.toString() + delimiter;
        }
        return result;
    }

    @TypeConverter
    public List<String> fromStringToListStr(String s)
    {
        String[] arr = s.split(delimiter);
        List<String> result = new ArrayList<>();

        for (String el : arr)
        {
            result.add(el);
        }

        return result;
    }

    @TypeConverter
    public String fromListStrToString(List<String> strs)
    {
        String result = "";
        for (String el : strs)
        {
            result += el + delimiter;
        }
        return result;
    }
}
