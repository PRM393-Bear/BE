package com.example.PRM.service;

import com.example.PRM.dto.request.community.CommunityPostReq;
import com.example.PRM.dto.request.community.PostCommentReq;
import com.example.PRM.dto.response.community.CommunityPostRes;
import com.example.PRM.dto.response.community.PostCommentRes;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface CommunityPostService {
    CommunityPostRes createPost(CommunityPostReq req, UserDetails userDetails);
    CommunityPostRes updatePost(UUID postId, CommunityPostReq req, UserDetails userDetails);
    void deletePost(UUID postId, UserDetails userDetails);
    void hidePost(UUID postId, UserDetails userDetails);
    void unhidePost(UUID postId, UserDetails userDetails);
    Page<CommunityPostRes> getAllPosts(int page, int size, UserDetails userDetails);
    void toggleLike(UUID postId, UserDetails userDetails);
    PostCommentRes addComment(UUID postId, PostCommentReq req, UserDetails userDetails);
    Page<PostCommentRes> getComments(UUID postId, int page, int size);
}
