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
    void update(Article article);

    // Удаление Article из бд
    @Delete
    void delete(Article article);

    @Query("DELETE FROM article")
    void clearArticles();

    // Получение всех постов из бд
    @Query("SELECT * FROM article ORDER BY date DESC")
    List<Article> getAllArticles();

    // Получение части постов из бд с определённым тегом
    @Query("SELECT * FROM article WHERE tags LIKE :tag ORDER BY date DESC LIMIT :count OFFSET :offset")
    List<Article> getSomeArticles(String tag, int count, int offset);

    // Получение части постов из бд с определённым тегом
    @Query("SELECT * FROM article WHERE tags LIKE :tag AND wasRead = 0 ORDER BY date DESC LIMIT :count OFFSET :offset")
    List<Article> getSomeUnreadArticles(String tag, int count, int offset);

    // Получение части постов из бд с определённым тегом
    @Query("SELECT * FROM article WHERE tags LIKE :tag AND loaded = 0 ORDER BY date DESC LIMIT :count OFFSET :offset")
    List<Article> getSomeUnloadedArticles(String tag, int count, int offset);

    // Получение части постов из бд с определённым тегом
    @Query("SELECT * FROM article WHERE tags LIKE :tag AND favorite = 1 ORDER BY date DESC LIMIT :count OFFSET :offset")
    List<Article> getSomeFavoriteArticles(String tag, int count, int offset);

    // Получение части постов из бд
    @Query("SELECT * FROM article ORDER BY date DESC LIMIT :count OFFSET :offset")
    List<Article> getSomeArticles(int count, int offset);

    // Получение поста с конкретным id
    @Query("SELECT * FROM article WHERE id = :id")
    Article getArticle(int id);

    @Query("SELECT COUNT(*) FROM article")
    int getCount();

    @Query("SELECT COUNT(*) FROM article WHERE wasRead = 0")
    int getUnreadCount();

    @Query("SELECT COUNT(*) FROM article WHERE loaded = 0")
    int getUnloadCount();

    @Query("SELECT COUNT(*) FROM article WHERE favorite = 1")
    int getFavoriteCount();

    @Query("SELECT COUNT(*) FROM comment WHERE articleId = :id")
    int getCommentsCount(int id);

    // Получение поста с конкретным номером в бд
    @Query("SELECT * FROM article LIMIT 1 OFFSET :offset")
    Article getArticleByOffset(int offset);

    // Получение поста с конкретным тегом
    @Query("SELECT * FROM article WHERE tags LIKE :tag")
    List<Article> findByTag(String tag);

    // Получение поста с крайней датой
    @Query("SELECT * FROM article ORDER BY date DESC LIMIT 1")
    Article getLatest();

    // Поиск
    @Query("SELECT * FROM article "
            + "WHERE name LIKE :title "
            + "ORDER BY date DESC")
    List<Article> searchInTitle(String title);

    // Поиск
    @Query("SELECT * FROM article "
            + "WHERE body LIKE :content "
            + "ORDER BY date DESC")
    List<Article> searchInContent(String content);
}