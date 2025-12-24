package ru.Edje_7.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Edje_7.dto.PaginationResponse;
import ru.Edje_7.dto.response.ApiResponse;
import ru.Edje_7.dto.response.TagResponse;
import ru.Edje_7.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tag management endpoints")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Get all tags")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<TagResponse>>> getAllTags(
            Pageable pageable,
            @RequestParam(required = false) String sort) {

        Page<TagResponse> tags = tagService.getAllTags(pageable);
        PaginationResponse<TagResponse> response = PaginationResponse.fromPage(tags);

        return ResponseEntity.ok(ApiResponse.success(response, "Tags retrieved successfully"));
    }

    @Operation(summary = "Get popular tags")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit) {

        List<TagResponse> tags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(ApiResponse.success(tags, "Popular tags retrieved successfully"));
    }

    @Operation(summary = "Get tag by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(@PathVariable Long id) {
        TagResponse tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success(tag, "Tag retrieved successfully"));
    }

    @Operation(summary = "Get tag by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagByName(@PathVariable String name) {
        TagResponse tag = tagService.getTagByName(name);
        return ResponseEntity.ok(ApiResponse.success(tag, "Tag retrieved successfully"));
    }

    @Operation(summary = "Get tag by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagBySlug(@PathVariable String slug) {
        TagResponse tag = tagService.getTagBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(tag, "Tag retrieved successfully"));
    }

    @Operation(summary = "Search tags")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<TagResponse>>> searchTags(
            @RequestParam String q,
            Pageable pageable) {

        Page<TagResponse> tags = tagService.searchTags(q, pageable);
        PaginationResponse<TagResponse> response = PaginationResponse.fromPage(tags);

        return ResponseEntity.ok(ApiResponse.success(response, "Tags search results"));
    }

    @Operation(summary = "Get posts count by tag")
    @GetMapping("/{id}/posts/count")
    public ResponseEntity<ApiResponse<Long>> getPostsCountByTag(@PathVariable Long id) {
        long count = tagService.getPostsCountByTag(id);
        return ResponseEntity.ok(ApiResponse.success(count, "Posts count retrieved successfully"));
    }

    @Operation(summary = "Create tag (admin only)")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@RequestParam String name) {
        TagResponse tag = tagService.createTag(name);
        return ResponseEntity.ok(ApiResponse.success(tag, "Tag created successfully"));
    }

    @Operation(summary = "Update tag (admin only)")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description) {

        TagResponse tag = tagService.updateTag(id, name, description);
        return ResponseEntity.ok(ApiResponse.success(tag, "Tag updated successfully"));
    }

    @Operation(summary = "Delete tag (admin only)")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tag deleted successfully"));
    }

    @Operation(summary = "Get trending tags")
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTrendingTags(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {

        List<TagResponse> tags = tagService.getTrendingTags(days, limit);
        return ResponseEntity.ok(ApiResponse.success(tags, "Trending tags retrieved successfully"));
    }

    @Operation(summary = "Merge tags (admin only)")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<TagResponse>> mergeTags(
            @RequestParam Long sourceTagId,
            @RequestParam Long targetTagId) {

        TagResponse tag = tagService.mergeTags(sourceTagId, targetTagId);
        return ResponseEntity.ok(ApiResponse.success(tag, "Tags merged successfully"));
    }
}