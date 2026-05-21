package org.example.nihongobackend.service.user;

import org.example.nihongobackend.dto.request.user.ChangePasswordRequest;
import org.example.nihongobackend.dto.request.user.UpdateProfileRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.user.TransactionItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserProfileService {

    AuthUserResponse updateAvatar(MultipartFile file);

    AuthUserResponse updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    List<TransactionItemResponse> listTransactions();
}
