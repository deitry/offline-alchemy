package com.dmsvo.offlinealchemy.classes.base;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by DmSVo on 31.12.2017.
 */

@Entity
public class Article implements Serializable {

    @PrimaryKey
    private int id;
    private String name;
    private Date date;
    private List<String> tags;
    private String body;
    private List<Integer> comments; // храним id-шники
    private int commentCount = 0;   // количество комментариев
        // - если отличается от текущего количества, надо подгружать новые
    private int wasRead = 0;    // прочитана ли
    private int favorite = 0;   // добавлена в избранное
    private int loaded = 0;     // загружена целиком?

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Integer> getComments() {
        return comments;
    }

    public void setComments(List<Integer> comments) {
        this.comments = comments;
    }

    public void setCommentsIds(List<Comment> comments)
    {
        this.comments = new ArrayList<>();
        for (Comment comment : comments)
        {
            this.comments.add(comment.getId());
        }
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getWasRead() {
        return wasRead;
    }

    public void setWasRead(int wasRead) {
        this.wasRead = wasRead;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getLoaded() {
        return loaded;
    }

    public void setLoaded(int loaded) {
        this.loaded = loaded;
    }
}
