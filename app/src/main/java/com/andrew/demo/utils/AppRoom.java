package com.andrew.demo.utils;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.andrew.demo.daos.PostDao;
import com.andrew.demo.models.Post;

@Database(entities = {Post.class}, version = 1, exportSchema = false)
public abstract class AppRoom extends RoomDatabase {

    public abstract PostDao postDao();

    private volatile static AppRoom INSTANCE;

    public static AppRoom getInstance(Context context) {
        if(INSTANCE == null) {
            synchronized (AppRoom.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppRoom.class, "app_room").build();
                }
            }
        }

        return INSTANCE;
    }
}
