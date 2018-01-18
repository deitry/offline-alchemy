package com.dmsvo.offlinealchemy.classes.loader;

import com.dmsvo.offlinealchemy.classes.base.Comment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by DmSVo on 04.01.2018.
 */

public class CommentParser {

    static Comment BuildComment(JSONObject json, int articleId)
    {
        Comment comment = new Comment();
        comment.setArticleId(articleId);

        //      thread -> id
        //      uname -> author
        //      parent -> parent
        //      ctime -> time string
        //      ctime_ts -> time int
        //      article -> body
        try {
            comment.setId(json.getInt("dtalkid")); // thread
            comment.setAuthor(json.getString("uname"));
            comment.setParentId(json.optInt("parent"));
            //comment.setLevel(json.getInt("level"));

            // чтение даты из строки возможно только из строки ctime
//            Calendar cal = Calendar.getInstance();
//            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy, HH:mm:ss z", Locale.ENGLISH);
//            cal.setTime(sdf.parse(json.getString("ctime")));
//            comment.setDate(cal.getTime());
            comment.setDate(new Date((long)json.getInt("ctime_ts") * 1000));
                // судя по всему, чтобы получить время из ctime_ts, надо полученный int умножить на 1000
            int isLoaded = json.getInt("loaded");
            if (isLoaded > 0) {
                comment.setBody(json.optString("article"));
            } else {
                comment.setBody("");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
        return comment;
    }

    static public JSONArray MergeArrays(JSONArray base, JSONArray additional)
    {
        int len = additional.length();
        for (int i = 0; i < len; i++)
        {
            JSONObject obj = additional.optJSONObject(i);
            if (obj != null)
                base.put(obj);
        }
        return base;
    }

//    static List<Comment> SortComments(List<Comment> comments)
//    {
//        List<Comment> sorted = new ArrayList<>();
//        for (Comment com : comments)
//        {
//            if (com.getParentId() == 0)
//                sorted.ad
//        }
//    }
}
