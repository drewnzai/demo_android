package com.andrew.demo.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.andrew.demo.models.Post;

import java.util.List;

@Dao
public interface PostDao {
    @Insert
    void insert(Post post);

    @Update
    void update(Post post);

    @Delete
    void delete(Post post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Post> posts);

    @Query("SELECT * FROM posts ORDER BY id DESC")
    List<Post> getAllPosts();

    @Query("SELECT * FROM posts WHERE id = :id")
    Post getById(int id);

    @Query("SELECT COUNT(*) FROM posts")
    int getPostCount();

    @Query("DELETE FROM posts")
    void deleteAll();
}
