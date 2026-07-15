package com.example.PRM.controller;

import com.example.PRM.dto.request.community.CommunityPostReq;
import com.example.PRM.dto.request.community.PostCommentReq;
import com.example.PRM.dto.response.community.CommunityPostRes;
import com.example.PRM.dto.response.community.PostCommentRes;
import com.example.PRM.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/community-posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<CommunityPostRes> createPost(
            @RequestBody CommunityPostReq req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(communityPostService.createPost(req, userDetails));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<CommunityPostRes> updatePost(
            @PathVariable UUID postId,
            @RequestBody CommunityPostReq req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(communityPostService.updatePost(postId, req, userDetails));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        communityPostService.deletePost(postId, userDetails);
        return ResponseEntity.ok("Successfully deleted post");
    }

    @PutMapping("/{postId}/hide")
    public ResponseEntity<String> hidePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        communityPostService.hidePost(postId, userDetails);
        return ResponseEntity.ok("Successfully hid post");
    }

    @PutMapping("/{postId}/unhide")
    public ResponseEntity<String> unhidePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        communityPostService.unhidePost(postId, userDetails);
        return ResponseEntity.ok("Successfully unhid post");
    }

    @GetMapping
    public ResponseEntity<Page<CommunityPostRes>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(communityPostService.getAllPosts(page, size, userDetails));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        communityPostService.toggleLike(postId, userDetails);
        return ResponseEntity.ok("Successfully toggled like");
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<PostCommentRes> addComment(
            @PathVariable UUID postId,
            @RequestBody PostCommentReq req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(communityPostService.addComment(postId, req, userDetails));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<PostCommentRes>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(communityPostService.getComments(postId, page, size));
    }
}
