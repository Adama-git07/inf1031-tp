package com.example.citoyenactif.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "agents",
        indices = {@Index(value = {"agentNumber"}, unique = true)}
)
public class AgentEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String firstName;
    public String agentNumber;
    public String password;
}
