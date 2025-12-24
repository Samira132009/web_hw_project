package ru.Edje_7.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Edje_7.dto.response.TagResponse;
import ru.Edje_7.entity.Tag;
import ru.Edje_7.exceptions.ResourceNotFoundException;
import ru.Edje_7.repository.TagRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<TagResponse> getAllTags(Pageable pageable) {
        return tagRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Cacheable(value = "tag", key = "#id")
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        return convertToResponse(tag);
    }

    @Transactional(readOnly = true)
    public Optional<Tag> getOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
                .or(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    newTag.setSlug(generateSlug(tagName));
                    return Optional.of(tagRepository.save(newTag));
                });
    }

    @Cacheable(value = "tag", key = "#name")
    @Transactional(readOnly = true)
    public TagResponse getTagByName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with name: " + name));

        return convertToResponse(tag);
    }

    @Transactional(readOnly = true)
    public TagResponse getTagBySlug(String slug) {
        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with slug: " + slug));

        return convertToResponse(tag);
    }

    @Transactional(readOnly = true)
    public Page<TagResponse> searchTags(String query, Pageable pageable) {
        return tagRepository.searchTags(query, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getPopularTags(int limit) {
        return tagRepository.findPopularTags(Pageable.ofSize(limit))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getPostsCountByTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        return tag.getPosts().size();
    }

    @CacheEvict(value = "tags", allEntries = true)
    @Transactional
    public TagResponse createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
        }

        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(generateSlug(name));

        Tag savedTag = tagRepository.save(tag);
        log.info("Created tag with id: {} and name: {}", savedTag.getId(), name);

        return convertToResponse(savedTag);
    }

    @CacheEvict(value = "tag", key = "#id")
    @Transactional
    public TagResponse updateTag(Long id, String name, String description) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (name != null && !name.equals(tag.getName())) {
            if (tagRepository.existsByName(name)) {
                throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
            }
            tag.setName(name);
            tag.setSlug(generateSlug(name));
        }

        if (description != null) {
            tag.setDescription(description);
        }

        Tag updatedTag = tagRepository.save(tag);
        log.info("Updated tag with id: {}", id);

        return convertToResponse(updatedTag);
    }

    @CacheEvict(value = {"tag", "tags"}, allEntries = true)
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (!tag.getPosts().isEmpty()) {
            throw new IllegalStateException("Cannot delete tag that has associated posts");
        }

        tagRepository.delete(tag);
        log.info("Deleted tag with id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTrendingTags(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return tagRepository.findTrendingTagsWithLimit(since, limit)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"tag", "tags"}, allEntries = true)
    @Transactional
    public TagResponse mergeTags(Long sourceTagId, Long targetTagId) {
        Tag sourceTag = tagRepository.findById(sourceTagId)
                .orElseThrow(() -> new ResourceNotFoundException("Source tag not found"));
        Tag targetTag = tagRepository.findById(targetTagId)
                .orElseThrow(() -> new ResourceNotFoundException("Target tag not found"));

        sourceTag.getPosts().forEach(post -> {
            post.removeTag(sourceTag);
            post.addTag(targetTag);
        });

        targetTag.setPostCount(targetTag.getPostCount() + sourceTag.getPostCount());

        tagRepository.delete(sourceTag);

        Tag savedTag = tagRepository.save(targetTag);
        log.info("Merged tag {} into tag {}", sourceTagId, targetTagId);

        return convertToResponse(savedTag);
    }

    private TagResponse convertToResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setSlug(tag.getSlug());
        response.setDescription(tag.getDescription());
        response.setPostCount(tag.getPostCount());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}