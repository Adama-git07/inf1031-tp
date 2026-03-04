package com.example.citoyenactif.ui.citizen.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.citoyenactif.data.local.entity.UserEntity;
import com.example.citoyenactif.data.repository.UserRepository;
import com.example.citoyenactif.databinding.ProfileFragmentBinding;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.example.citoyenactif.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfilFragment extends Fragment {

    private ProfileFragmentBinding binder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binder = ProfileFragmentBinding.inflate(inflater, container, false);

        binder.btnLogout.setOnClickListener(v -> logout());

        loadConnectedUser();

        return binder.getRoot();
    }

    private void loadConnectedUser() {
        long userId = SessionManager.getUserId(requireContext());

        if (userId <= 0) {
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        new Thread(() -> {
            UserRepository repo = new UserRepository(requireContext());
            UserEntity user = repo.getById(userId);

            requireActivity().runOnUiThread(() -> {
                if (user == null) {
                    Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                    logout();
                    return;
                }

                binder.tvFullName.setText(user.username); // pas de prénom/nom dans UserEntity
                binder.tvUsername.setText("Username : " + user.username);
                binder.tvEmail.setText("Email : " + user.email);

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(new Date(user.createdAt));
                binder.tvCreatedAt.setText("Compte créé : " + date);
            });
        }).start();
    }

    private void logout() {
        SessionManager.clear(requireContext());

        Intent intent = new Intent(requireActivity(), AuthentificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder = null;
    }
}