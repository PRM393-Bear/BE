package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.community.CommunityPostReq;
import com.example.PRM.dto.request.community.PostCommentReq;
import com.example.PRM.dto.response.community.CommunityPostRes;
import com.example.PRM.dto.response.community.PostCommentRes;
import com.example.PRM.entity.CommunityPost;
import com.example.PRM.entity.PostComment;
import com.example.PRM.entity.PostLike;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.event.PostInteractionNotificationEvent;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.CommunityPostRepository;
import com.example.PRM.repository.PostCommentRepository;
import com.example.PRM.repository.PostLikeRepository;
import com.example.PRM.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceImplTest {

    @InjectMocks
    private CommunityPostServiceImpl communityPostService;

    @Mock
    private CommunityPostRepository communityPostRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserDetails userDetails;

    private User user;
    private User adminUser;
    private Role orgRole;
    private Role userRole;
    private Role adminRole;
    private CommunityPost post;
    private UUID postId;

    @BeforeEach
    void setUp() {
        orgRole = new Role();
        orgRole.setRoleName("ORGANIZATION");

        userRole = new Role();
        userRole.setRoleName("USER");

        adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("orgUser");
        user.setRole(orgRole);
        user.setFullName("Org User");

        adminUser = new User();
        adminUser.setUserId(UUID.randomUUID());
        adminUser.setUserName("admin");
        adminUser.setRole(adminRole);

        postId = UUID.randomUUID();
        post = new CommunityPost();
        post.setId(postId);
        post.setUser(user);
        post.setContent("Test content");
        post.setImages(Arrays.asList("img1.jpg"));

        lenient().when(userDetails.getUsername()).thenReturn("orgUser");
    }

    @Test
    void createPost_ShouldReturnPostRes_WhenUserIsOrganization() {
        CommunityPostReq req = new CommunityPostReq();
        req.setContent("New Content");

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.save(any(CommunityPost.class))).thenReturn(post);

        CommunityPostRes result = communityPostService.createPost(req, userDetails);

        assertNotNull(result);
        assertEquals(post.getContent(), result.getContent());
        verify(communityPostRepository, times(1)).save(any(CommunityPost.class));
    }

    @Test
    void createPost_ShouldThrowForbidden_WhenUserIsNotOrganization() {
        user.setRole(userRole);
        CommunityPostReq req = new CommunityPostReq();

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> communityPostService.createPost(req, userDetails));
        verify(communityPostRepository, never()).save(any(CommunityPost.class));
    }

    @Test
    void updatePost_ShouldUpdate_WhenUserIsAuthor() {
        CommunityPostReq req = new CommunityPostReq();
        req.setContent("Updated Content");

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.save(any(CommunityPost.class))).thenReturn(post);

        CommunityPostRes result = communityPostService.updatePost(postId, req, userDetails);

        assertNotNull(result);
        verify(communityPostRepository, times(1)).save(post);
    }

    @Test
    void deletePost_ShouldDelete_WhenUserIsAuthor() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        communityPostService.deletePost(postId, userDetails);

        verify(postLikeRepository, times(1)).deleteAllByPost(post);
        verify(postCommentRepository, times(1)).deleteAllByPost(post);
        verify(communityPostRepository, times(1)).delete(post);
    }

    @Test
    void deletePost_ShouldThrowForbidden_WhenUserIsNotAuthorAndNotAdmin() {
        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID());
        otherUser.setRole(userRole);
        otherUser.setUserName("otherUser");

        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(otherUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> communityPostService.deletePost(postId, userDetails));
    }

    @Test
    void toggleLike_ShouldAddLike_WhenNotLiked() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());

        communityPostService.toggleLike(postId, userDetails);

        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        verify(postLikeRepository, never()).delete(any(PostLike.class));
    }

    @Test
    void toggleLike_ShouldRemoveLike_WhenAlreadyLiked() {
        PostLike like = new PostLike();
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(like));

        communityPostService.toggleLike(postId, userDetails);

        verify(postLikeRepository, times(1)).delete(like);
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    void addComment_ShouldSaveCommentAndNotify() {
        User commenter = new User();
        commenter.setUserId(UUID.randomUUID());
        commenter.setUserName("commenter");
        commenter.setFullName("Commenter Name");

        PostCommentReq req = new PostCommentReq();
        req.setContent("Nice post!");

        PostComment savedComment = new PostComment();
        savedComment.setId(UUID.randomUUID());
        savedComment.setPost(post);
        savedComment.setUser(commenter);
        savedComment.setContent("Nice post!");

        when(userDetails.getUsername()).thenReturn("commenter");
        when(userRepository.findByUserName("commenter")).thenReturn(Optional.of(commenter));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.save(any(PostComment.class))).thenReturn(savedComment);

        PostCommentRes res = communityPostService.addComment(postId, req, userDetails);

        assertNotNull(res);
        verify(postCommentRepository, times(1)).save(any(PostComment.class));
        verify(eventPublisher, times(1)).publishEvent(any(PostInteractionNotificationEvent.class));
    }

    @Test
    void getAllPosts_ShouldReturnPageOfPosts() {
        Page<CommunityPost> page = new PageImpl<>(Collections.singletonList(post));
        when(communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(page);
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));

        Page<CommunityPostRes> result = communityPostService.getAllPosts(0, 10, userDetails);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(post.getId(), result.getContent().get(0).getId());
    }
}
