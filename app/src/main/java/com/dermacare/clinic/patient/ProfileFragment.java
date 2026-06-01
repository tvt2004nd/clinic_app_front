package com.dermacare.clinic.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dermacare.clinic.R;
import com.dermacare.clinic.auth.LoginActivity;
import com.dermacare.clinic.data.api.ApiClient;
import com.dermacare.clinic.data.api.UserService;
import com.dermacare.clinic.data.api.model.UserProfileResponse;
import com.dermacare.clinic.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String ARG_DOCTOR = "doctor";

    private ShapeableImageView imgProfile;
    private TextView tvName, tvEmail;
    private SessionManager session;
    private UserService userService;

    public static ProfileFragment newInstance(boolean isDoctor) {
        ProfileFragment f = new ProfileFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DOCTOR, isDoctor);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());
        userService = ApiClient.getUserService(requireContext());

        imgProfile = view.findViewById(R.id.imgProfile);
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);

        // Bind items
        TextView tvEditProfile = view.findViewById(R.id.tvEditProfile);
        TextView tvChangePassword = view.findViewById(R.id.tvChangePassword);

        tvEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        View btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        displayProfileInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayProfileInfo();
        fetchProfileFromServer();
    }

    private void displayProfileInfo() {
        if (tvName != null && tvEmail != null && session != null) {
            tvName.setText(session.getName());
            tvEmail.setText(session.getEmail());

            String avatarUrl = session.getAvatar();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                String fullUrl = avatarUrl;
                if (avatarUrl.startsWith("/")) {
                    fullUrl = ApiClient.BASE_URL + avatarUrl.substring(1);
                } else if (!avatarUrl.startsWith("http")) {
                    fullUrl = ApiClient.BASE_URL + avatarUrl;
                }
                Glide.with(this)
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.ic_nav_profile);
            }
        }
    }

    private void fetchProfileFromServer() {
        if (userService != null) {
            userService.getProfile().enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfileResponse profile = response.body();
                        session.saveProfile(
                                profile.getFullName(),
                                profile.getPhone(),
                                profile.getGender(),
                                profile.getDateOfBirth(),
                                profile.getAddress(),
                                profile.getAvatarUrl()
                        );
                        displayProfileInfo();
                    }
                }

                @Override
                public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                    // Ignore and use local session details
                }
            });
        }
    }
}
