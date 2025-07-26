package com.unify.app.posts.saved;

import com.unify.app.posts.domain.models.PostDto;
import java.time.LocalDateTime;

public record SavedPostDto(String id, String userId, PostDto post, LocalDateTime savedAt) {}
