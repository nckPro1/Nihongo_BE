package org.example.nihongobackend.controller.user;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.user.ChangePasswordRequest;
import org.example.nihongobackend.dto.request.user.UpdateProfileRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.user.TransactionItemResponse;
import org.example.nihongobackend.service.user.UserProfileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AuthUserResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        AuthUserResponse data = userProfileService.updateAvatar(file);
        return ApiResponse.success("Avatar updated", data);
    }

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AuthUserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        AuthUserResponse data = userProfileService.updateProfile(request);
        return ApiResponse.success("Profile updated", data);
    }

    @PostMapping("/change-password")
    public ApiResponse<Object> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(request);
        return ApiResponse.success("Password has been changed", null);
    }

    @GetMapping("/transactions")
    public ApiResponse<List<TransactionItemResponse>> transactions() {
        List<TransactionItemResponse> data = userProfileService.listTransactions();
        return ApiResponse.success("OK", data);
    }
}
