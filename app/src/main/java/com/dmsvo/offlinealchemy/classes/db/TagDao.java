package com.dmsvo.offlinealchemy.classes.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Tag;

import java.util.List;
import java.util.Set;

/**
 * Created by DmSVo on 13.01.2018.
 */

@Dao
public interface TagDao {
    // Добавление Article в бд
    @Insert
    void insertAll(Tag... tags);

    // Удаление Article из бд
    @Update
    void update(Tag tag);

    // Удаление Article из бд
    @Delete
    void delete(Tag tag);

    @Query("DELETE FROM tag")
    void clearTags();

    @Query("SELECT * FROM tag WHERE data = :data")
    Tag getTag(String data);

    // Получение всех тегов
    @Query("SELECT * FROM tag ORDER BY data ASC")
    List<Tag> getAllTags();

    // сколько всего тегов
    @Query("SELECT COUNT(*) FROM tag")
    int getCount();

//    @Query("INSERT INTO tag SET x=1, y=2 ON DUPLICATE KEY UPDATE x=x+1, y=y+2")
//    void insertOrUpdate(Tag tag);
}
