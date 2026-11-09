package com.example.citoyenactif.data.local.proxies;

import com.example.citoyenactif.model.dto.LoginResponseDTO;
import com.example.citoyenactif.model.dto.UserLoginDTO;
import com.example.citoyenactif.model.dto.UserCreateDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AccountProxy extends Proxy {

    @POST("/api/users/login")
    Call<LoginResponseDTO> login(@Body UserLoginDTO dto);

}