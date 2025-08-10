package com.unify.app.comments.domain;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface CommentRepository extends JpaRepository<Comment, String> {

  @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId")
  List<Comment> findCommentsByPostIdWithUser(@Param("postId") String postId);

  List<Comment> findByPostId(String postId);

  List<Comment> findByUserId(String userId);

  List<Comment> findByPostIdAndParentIsNull(String postId);

  List<Comment> findByParent(Comment parent);

  List<Comment> findByParentId(String parentId);

  @Query(
      "SELECT DISTINCT pc FROM Comment pc LEFT JOIN FETCH pc.replies r LEFT JOIN FETCH pc.parent LEFT JOIN FETCH pc.user u WHERE pc.post.id = :postId")
  List<Comment> findAllCommentsByPostId(@Param("postId") String postId);

  @Query(
      "SELECT DISTINCT pc FROM Comment pc LEFT JOIN FETCH pc.replies r LEFT JOIN FETCH pc.parent LEFT JOIN FETCH pc.user u WHERE pc.post.id = :postId AND pc.status = :status")
  List<Comment> findAllCommentsByPostIdAndStatus(
      @Param("postId") String postId, @Param("status") Integer status);

  @Query(
      "SELECT DISTINCT pc FROM Comment pc LEFT JOIN FETCH pc.replies r LEFT JOIN FETCH pc.parent WHERE pc.parent = :parent")
  List<Comment> findByParentWithReplies(@Param("parent") Comment parent);

  @Query("SELECT pc FROM Comment pc WHERE pc.parent.id = :parentId AND pc.status = :status")
  List<Comment> findByParentIdAndStatus(
      @Param("parentId") String parentId, @Param("status") Integer status);

  @Query("SELECT COUNT(pc) FROM Comment pc WHERE pc.post.id = :postId")
  long countByPostId(@Param("postId") String postId);

  @Query("SELECT COUNT(pc) FROM Comment pc WHERE pc.post.id = :postId AND pc.status = :status")
  long countByPostIdAndStatus(@Param("postId") String postId, @Param("status") Integer status);


}
