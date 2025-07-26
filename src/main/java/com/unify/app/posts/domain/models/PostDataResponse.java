package com.unify.app.posts.domain.models;

import java.util.List;
import javax.print.attribute.standard.Media;

public record PostDataResponse(String id, String captions, List<Media> media) {}
