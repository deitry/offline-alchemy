package com.dmsvo.offlinealchemy.classes.base;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by DmSVo on 31.12.2017.
 */

@Entity(foreignKeys = @ForeignKey(
        entity = Article.class,
        parentColumns = "id",
        childColumns = "articleId"))
public class Comment implements Serializable {

    @PrimaryKey
    private int id;
    private int articleId;
    private String author;
    private Date date;
    private int parentId;
    private String body;
    private int level;

//    @Ignore
//    public List<Comment> responses = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
