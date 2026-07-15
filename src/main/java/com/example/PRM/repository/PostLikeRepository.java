package com.example.PRM.repository;

import com.example.PRM.entity.CommunityPost;
import com.example.PRM.entity.PostLike;
import com.example.PRM.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    long countByPost(CommunityPost post);
    boolean existsByPostAndUser(CommunityPost post, User user);
    Optional<PostLike> findByPostAndUser(CommunityPost post, User user);
    void deleteAllByPost(CommunityPost post);
}
