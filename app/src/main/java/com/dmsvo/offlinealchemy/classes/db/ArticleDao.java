package com.dmsvo.offlinealchemy.classes.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.dmsvo.offlinealchemy.classes.base.Article;

import java.util.List;

/**
 * Created by DmSVo on 04.01.2018.
 */

@Dao
public interface ArticleDao {

    // Добавление Article в бд
    @Insert
    void insertAll(Article... articles);

    // Удаление Article из бд
    @Update
    void update(Article person);

    // Удаление Article из бд
    @Delete
    void delete(Article person);

    @Query("DELETE FROM article")
    void clearArticles();

    // Получение всех постов из бд
    @Query("SELECT * FROM article ORDER BY date DESC")
    List<Article> getAllArticles();

    // Получение поста с конкретным id
    @Query("SELECT * FROM article WHERE id = :id")
    Article getArticle(int id);

}