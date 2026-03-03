package com.example.citoyenactif.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.citoyenactif.data.local.converter.UserRoleConverter;
import com.example.citoyenactif.data.local.dao.UserDao;
import com.example.citoyenactif.data.local.entity.UserEntity;

@Database(entities = {UserEntity.class}, version = 1)
@TypeConverters({UserRoleConverter.class})
public abstract class DataBase extends RoomDatabase {
    public abstract UserDao userDao();
}
