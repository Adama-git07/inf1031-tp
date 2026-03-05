package com.example.citoyenactif.data.repository;

import android.content.Context;

import com.example.citoyenactif.data.local.dao.AgentDao;
import com.example.citoyenactif.data.local.database.DatabaseClient;
import com.example.citoyenactif.data.local.entity.AgentEntity;

public class AgentRepository {
    private final AgentDao agentDao;

    public AgentRepository(Context context) {
        agentDao = DatabaseClient.getInstance(context).getDb().agentDao();
    }

    public AgentEntity login(String agentNumber, String password) {
        return agentDao.login(agentNumber, password);
    }

    public AgentEntity getById(long id) {
        return agentDao.getById(id);
    }
}
