package com.dmsvo.offlinealchemy.classes.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.dmsvo.offlinealchemy.classes.base.Comment;

import java.util.List;

/**
 * Created by DmSVo on 04.01.2018.
 */

@Dao
public interface CommentDao {

    // Добавление комментария в бд
    @Insert
    void insertAll(Comment... comments);

    // Удаление комментария из бд
    @Delete
    void delete(Comment comment);

    // Удаление косментария  из бд
    @Update
    void update(Comment comment);

    @Query("DELETE FROM comment")
    void clearComments();

    // Получение всех комментариев из бд с условием
    @Query("SELECT * FROM comment WHERE articleid LIKE :articleId")
    List<Comment> getAllCommentsForArticle(int articleId);

    // Получение всех комментариев из бд с условием
    @Query("SELECT * FROM comment WHERE id = :id")
    Comment getComment(int id);

    // Получение всех комментариев из бд с условием
    @Query("SELECT * FROM comment WHERE articleId = :articleId AND parentId = :parentId")
    List<Comment> getAllChildComments(int articleId, int parentId);

    // Получение всех комментариев из бд с условием
    @Query("SELECT COUNT(*) FROM comment WHERE articleId = :articleId")
    int getCommentCount(int articleId);
}
