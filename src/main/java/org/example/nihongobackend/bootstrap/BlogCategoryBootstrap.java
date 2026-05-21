package org.example.nihongobackend.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nihongobackend.entity.blog.BlogCategory;
import org.example.nihongobackend.repository.blog.BlogCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bootstrap sample blog categories on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BlogCategoryBootstrap implements CommandLineRunner {

    private final BlogCategoryRepository blogCategoryRepository;

    @Override
    public void run(String... args) {
        if (blogCategoryRepository.count() > 0) {
            log.info("Blog categories already exist, skipping bootstrap");
            return;
        }

        log.info("Creating sample blog categories...");

        List<BlogCategory> categories = List.of(
                BlogCategory.builder()
                        .name("Ngữ pháp")
                        .slug("ngu-phap")
                        .description("Bài viết về ngữ pháp tiếng Nhật")
                        .build(),
                BlogCategory.builder()
                        .name("Từ vựng")
                        .slug("tu-vung")
                        .description("Học từ vựng tiếng Nhật")
                        .build(),
                BlogCategory.builder()
                        .name("Kanji")
                        .slug("kanji")
                        .description("Học chữ Hán tiếng Nhật")
                        .build(),
                BlogCategory.builder()
                        .name("JLPT")
                        .slug("jlpt")
                        .description("Luyện thi JLPT các cấp độ")
                        .build(),
                BlogCategory.builder()
                        .name("Video Lessons")
                        .slug("video-lessons")
                        .description("Bài học dạng video")
                        .build(),
                BlogCategory.builder()
                        .name("Tin tức")
                        .slug("tin-tuc")
                        .description("Tin tức về học tiếng Nhật")
                        .build(),
                BlogCategory.builder()
                        .name("Văn hóa")
                        .slug("van-hoa")
                        .description("Văn hóa Nhật Bản")
                        .build()
        );

        blogCategoryRepository.saveAll(categories);
        log.info("Created {} blog categories", categories.size());
    }
}
