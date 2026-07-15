package com.example.PRM.repository;

import com.example.PRM.entity.CommunityPost;
import com.example.PRM.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
    long countByPost(CommunityPost post);
    Page<PostComment> findByPostOrderByCreatedAtAsc(CommunityPost post, Pageable pageable);
    void deleteAllByPost(CommunityPost post);
}
