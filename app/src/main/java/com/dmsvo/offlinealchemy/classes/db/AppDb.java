package com.dmsvo.offlinealchemy.classes.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;
import com.dmsvo.offlinealchemy.classes.base.Tag;

/**
 * Created by DmSVo on 04.01.2018.
 */

@Database(entities = {
        Article.class,
        Comment.class,
        Tag.class
}, version = 4)
@TypeConverters({Converters.class})
public abstract class AppDb extends RoomDatabase {
    public abstract ArticleDao getArticleDao();
    public abstract CommentDao getCommentDao();
    public abstract TagDao getTagDao();

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE article "
                    + " ADD COLUMN loaded INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // создаём (?) таблицу tag
            database.execSQL("CREATE TABLE tag "
                    + " (data TEXT NOT NULL DEFAULT '', count INTEGER NOT NULL," +
                    " PRIMARY KEY(data))");
            // заполняем, если есть чем
        }
    };
}
