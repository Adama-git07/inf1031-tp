package com.example.citoyenactif.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.citoyenactif.data.local.entity.AgentEntity;

import java.util.List;

@Dao
public interface AgentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert (AgentEntity agent);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<AgentEntity> agents);

    @Query("SELECT COUNT(*) FROM agents")
    int countAgent();

    @Query("SELECT * FROM agents WHERE agentNumber = :agentNumber AND password = :password LIMIT 1")
    AgentEntity login(String agentNumber, String password);

    @Query("SELECT * FROM agents WHERE agentNumber = :agentNumber LIMIT 1")
    AgentEntity findByAgentNumber(String agentNumber);

    @Query("SELECT * FROM agents ORDER BY id DESC")
    List<AgentEntity> getAll();

    @Query("SELECT * FROM agents WHERE id = :id LIMIT 1")
    AgentEntity getById(long id);
}
