package com.dmsvo.offlinealchemy.classes.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;

/**
 * Created by DmSVo on 04.01.2018.
 */

@Database(entities = {Article.class, Comment.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class AppDb extends RoomDatabase {
    public abstract ArticleDao getArticleDao();
    public abstract CommentDao getCommentDao();

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE article "
                    + " ADD COLUMN loaded INTEGER NOT NULL DEFAULT 0");
        }
    };
}
