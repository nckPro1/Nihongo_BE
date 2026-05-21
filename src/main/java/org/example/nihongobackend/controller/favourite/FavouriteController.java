package org.example.nihongobackend.controller.favourite;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.favourite.AddFavouriteRequest;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.favourite.FavouriteItemResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.favourite.UserFavouriteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favourites")
public class FavouriteController {

    private final UserFavouriteService userFavouriteService;
    private final UserRepository userRepository;

    public FavouriteController(UserFavouriteService userFavouriteService, UserRepository userRepository) {
        this.userFavouriteService = userFavouriteService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<List<FavouriteItemResponse>> list(@RequestParam(value = "type", required = false) String type) {
        User user = loadCurrentUser();
        return ApiResponse.success("OK", userFavouriteService.listForUser(user, type));
    }

    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody AddFavouriteRequest request) {
        User user = loadCurrentUser();
        userFavouriteService.add(user, request.getTargetType().trim().toUpperCase(), request.getTargetId());
        return ApiResponse.success("Đã thêm yêu thích", null);
    }

    @DeleteMapping
    public ApiResponse<Void> remove(
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") UUID targetId) {
        User user = loadCurrentUser();
        userFavouriteService.remove(user, targetType.trim().toUpperCase(), targetId);
        return ApiResponse.success("Đã bỏ yêu thích", null);
    }

    private User loadCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
