package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.community.CommunityPostReq;
import com.example.PRM.dto.request.community.PostCommentReq;
import com.example.PRM.dto.response.community.CommunityPostRes;
import com.example.PRM.dto.response.community.PostCommentRes;
import com.example.PRM.entity.*;
import com.example.PRM.event.PostInteractionNotificationEvent;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.CommunityPostRepository;
import com.example.PRM.repository.DonationEventRepository;
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

import java.time.OffsetDateTime;
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

    @Mock private CommunityPostRepository communityPostRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private PostCommentRepository postCommentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private DonationEventRepository donationEventRepository;
    @Mock private UserDetails userDetails;

    private User user;
    private User adminUser;
    private Role orgRole;
    private Role userRole;
    private Role adminRole;
    private CommunityPost post;
    private UUID postId;
    private DonationEvent donationEvent;
    private UUID donationEventId;

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

        donationEventId = UUID.randomUUID();
        donationEvent = new DonationEvent();
        donationEvent.setId(donationEventId);
        donationEvent.setTitle("Event Title");

        postId = UUID.randomUUID();
        post = new CommunityPost();
        post.setId(postId);
        post.setUser(user);
        post.setContent("Test content");
        post.setImages(Arrays.asList("img1.jpg"));
        post.setCreatedAt(OffsetDateTime.now());

        lenient().when(userDetails.getUsername()).thenReturn("orgUser");
    }

    // CREATE POST
    @Test
    void createPost_ShouldReturnPostRes_WhenUserIsOrganization() {
        CommunityPostReq req = new CommunityPostReq();
        req.setContent("New Content");
        req.setDonationEventId(donationEventId);

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(i -> {
            CommunityPost p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        CommunityPostRes result = communityPostService.createPost(req, userDetails);

        assertNotNull(result);
        assertEquals(req.getContent(), result.getContent());
        assertEquals(donationEventId, result.getDonationEventId());
        verify(communityPostRepository, times(1)).save(any(CommunityPost.class));
    }

    @Test
    void createPost_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.createPost(new CommunityPostReq(), userDetails));
    }

    @Test
    void createPost_ShouldThrowForbidden_WhenUserIsNotOrganization() {
        user.setRole(userRole);
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        assertThrows(ForbiddenException.class, () -> communityPostService.createPost(new CommunityPostReq(), userDetails));
    }

    @Test
    void createPost_ShouldThrowNotFound_WhenDonationEventNotFound() {
        CommunityPostReq req = new CommunityPostReq();
        req.setDonationEventId(donationEventId);
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> communityPostService.createPost(req, userDetails));
    }

    @Test
    void createPost_ShouldReturnPostRes_WhenDonationEventIdNull() {
        CommunityPostReq req = new CommunityPostReq();
        req.setContent("New Content");
        req.setDonationEventId(null);

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(i -> {
            CommunityPost p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        CommunityPostRes result = communityPostService.createPost(req, userDetails);

        assertNotNull(result);
        assertNull(result.getDonationEventId());
    }

    // UPDATE POST
    @Test
    void updatePost_ShouldUpdate_WhenUserIsAuthor() {
        CommunityPostReq req = new CommunityPostReq();
        req.setContent("Updated Content");

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.save(any(CommunityPost.class))).thenReturn(post);

        CommunityPostRes result = communityPostService.updatePost(postId, req, userDetails);

        assertNotNull(result);
        assertNull(post.getDonationEvent());
        verify(communityPostRepository, times(1)).save(post);
    }

    @Test
    void updatePost_ShouldUpdate_WithDonationEvent() {
        CommunityPostReq req = new CommunityPostReq();
        req.setContent("Updated Content");
        req.setDonationEventId(donationEventId);

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        when(communityPostRepository.save(any(CommunityPost.class))).thenReturn(post);

        CommunityPostRes result = communityPostService.updatePost(postId, req, userDetails);

        assertNotNull(result);
        assertEquals(donationEvent, post.getDonationEvent());
        verify(communityPostRepository, times(1)).save(post);
    }

    @Test
    void updatePost_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.updatePost(postId, new CommunityPostReq(), userDetails));
    }

    @Test
    void updatePost_ShouldThrowNotFound_WhenPostNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.updatePost(postId, new CommunityPostReq(), userDetails));
    }

    @Test
    void updatePost_ShouldThrowForbidden_WhenNotAuthor() {
        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID());
        otherUser.setUserName("otherUser");
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(otherUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> communityPostService.updatePost(postId, new CommunityPostReq(), userDetails));
    }

    @Test
    void updatePost_ShouldThrowNotFound_WhenDonationEventNotFound() {
        CommunityPostReq req = new CommunityPostReq();
        req.setDonationEventId(donationEventId);
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> communityPostService.updatePost(postId, req, userDetails));
    }

    // DELETE POST
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
    void deletePost_ShouldDelete_WhenAdmin() {
        when(userDetails.getUsername()).thenReturn("admin");
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        communityPostService.deletePost(postId, userDetails);

        verify(communityPostRepository, times(1)).delete(post);
    }

    @Test
    void deletePost_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.deletePost(postId, userDetails));
    }

    @Test
    void deletePost_ShouldThrowNotFound_WhenPostNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.deletePost(postId, userDetails));
    }

    @Test
    void deletePost_ShouldThrowForbidden_WhenNotAuthorAndNotAdmin() {
        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID());
        otherUser.setRole(userRole);
        otherUser.setUserName("otherUser");

        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(otherUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> communityPostService.deletePost(postId, userDetails));
    }

    // HIDE POST
    @Test
    void hidePost_ShouldHide_WhenAuthor() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        communityPostService.hidePost(postId, userDetails);
        assertTrue(post.isHidden());
        verify(communityPostRepository, times(1)).save(post);
    }

    @Test
    void hidePost_ShouldHide_WhenAdmin() {
        when(userDetails.getUsername()).thenReturn("admin");
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        communityPostService.hidePost(postId, userDetails);
        assertTrue(post.isHidden());
    }

    @Test
    void hidePost_ShouldThrowForbidden_WhenNotAuthorAndNotAdmin() {
        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID());
        otherUser.setRole(userRole);
        otherUser.setUserName("otherUser");
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(otherUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> communityPostService.hidePost(postId, userDetails));
    }

    @Test
    void hidePost_ShouldThrowNotFound_WhenPostNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.hidePost(postId, userDetails));
    }

    @Test
    void hidePost_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.hidePost(postId, userDetails));
    }

    // UNHIDE POST
    @Test
    void unhidePost_ShouldUnhide_WhenAuthor() {
        post.setHidden(true);
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        communityPostService.unhidePost(postId, userDetails);
        assertFalse(post.isHidden());
        verify(communityPostRepository, times(1)).save(post);
    }

    @Test
    void unhidePost_ShouldUnhide_WhenAdmin() {
        post.setHidden(true);
        when(userDetails.getUsername()).thenReturn("admin");
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        communityPostService.unhidePost(postId, userDetails);
        assertFalse(post.isHidden());
    }

    @Test
    void unhidePost_ShouldThrowForbidden_WhenNotAuthorAndNotAdmin() {
        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID());
        otherUser.setRole(userRole);
        otherUser.setUserName("otherUser");
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(otherUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> communityPostService.unhidePost(postId, userDetails));
    }

    @Test
    void unhidePost_ShouldThrowNotFound_WhenPostNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.unhidePost(postId, userDetails));
    }

    @Test
    void unhidePost_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.unhidePost(postId, userDetails));
    }

    // GET ALL POSTS
    @Test
    void getAllPosts_ShouldReturnPageOfPosts() {
        Page<CommunityPost> page = new PageImpl<>(Collections.singletonList(post));
        when(communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(page);
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));

        Page<CommunityPostRes> result = communityPostService.getAllPosts(0, 10, userDetails);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllPosts_ShouldHandleNullUserDetails() {
        Page<CommunityPost> page = new PageImpl<>(Collections.singletonList(post));
        when(communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(page);

        Page<CommunityPostRes> result = communityPostService.getAllPosts(0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).isLikedByMe());
    }

    @Test
    void getAllPosts_ShouldHandleUserNotFound() {
        Page<CommunityPost> page = new PageImpl<>(Collections.singletonList(post));
        when(communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(page);
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());

        Page<CommunityPostRes> result = communityPostService.getAllPosts(0, 10, userDetails);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // TOGGLE LIKE
    @Test
    void toggleLike_ShouldAddLike_WhenNotLiked() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());

        communityPostService.toggleLike(postId, userDetails);

        verify(postLikeRepository, times(1)).save(any(PostLike.class));
    }

    @Test
    void toggleLike_ShouldAddLikeAndNotify_WhenNotLikedByOtherUser() {
        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID());
        otherUser.setUserName("otherUser");
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUserName("otherUser")).thenReturn(Optional.of(otherUser));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostAndUser(post, otherUser)).thenReturn(Optional.empty());

        communityPostService.toggleLike(postId, userDetails);

        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        verify(eventPublisher, times(1)).publishEvent(any(PostInteractionNotificationEvent.class));
    }

    @Test
    void toggleLike_ShouldRemoveLike_WhenAlreadyLiked() {
        PostLike like = new PostLike();
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(like));

        communityPostService.toggleLike(postId, userDetails);

        verify(postLikeRepository, times(1)).delete(like);
    }

    @Test
    void toggleLike_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.toggleLike(postId, userDetails));
    }

    @Test
    void toggleLike_ShouldThrowNotFound_WhenPostNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.toggleLike(postId, userDetails));
    }

    // ADD COMMENT
    @Test
    void addComment_ShouldSaveComment_WhenReplyToSameAuthor() {
        PostCommentReq req = new PostCommentReq();
        req.setContent("Nice post!");

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        
        PostComment savedComment = new PostComment();
        savedComment.setId(UUID.randomUUID());
        savedComment.setPost(post);
        savedComment.setUser(user);
        savedComment.setContent("Nice post!");
        when(postCommentRepository.save(any(PostComment.class))).thenReturn(savedComment);

        PostCommentRes res = communityPostService.addComment(postId, req, userDetails);

        assertNotNull(res);
        verify(eventPublisher, never()).publishEvent(any()); // User commented on own post
    }

    @Test
    void addComment_ShouldSaveCommentAndNotify_WhenOtherUserComments() {
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
        verify(eventPublisher, times(1)).publishEvent(any(PostInteractionNotificationEvent.class));
    }

    @Test
    void addComment_ShouldSaveCommentAndNotify_WhenReplyToParent() {
        User commenter = new User();
        commenter.setUserId(UUID.randomUUID());
        commenter.setUserName("commenter");
        commenter.setFullName("Commenter Name");

        User parentUser = new User();
        parentUser.setUserId(UUID.randomUUID());
        parentUser.setUserName("parentUser");

        PostComment parent = new PostComment();
        parent.setId(UUID.randomUUID());
        parent.setUser(parentUser);

        PostCommentReq req = new PostCommentReq();
        req.setContent("Reply!");
        req.setParentCommentId(parent.getId());

        PostComment savedComment = new PostComment();
        savedComment.setId(UUID.randomUUID());
        savedComment.setPost(post);
        savedComment.setUser(commenter);
        savedComment.setContent("Reply!");
        savedComment.setParentComment(parent);

        when(userDetails.getUsername()).thenReturn("commenter");
        when(userRepository.findByUserName("commenter")).thenReturn(Optional.of(commenter));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(postCommentRepository.save(any(PostComment.class))).thenReturn(savedComment);

        communityPostService.addComment(postId, req, userDetails);

        verify(eventPublisher, times(2)).publishEvent(any(PostInteractionNotificationEvent.class)); // 1 for post author, 1 for parent comment author
    }

    @Test
    void addComment_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.addComment(postId, new PostCommentReq(), userDetails));
    }

    @Test
    void addComment_ShouldThrowNotFound_WhenPostNotFound() {
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.addComment(postId, new PostCommentReq(), userDetails));
    }

    @Test
    void addComment_ShouldThrowNotFound_WhenParentCommentNotFound() {
        PostCommentReq req = new PostCommentReq();
        req.setParentCommentId(UUID.randomUUID());
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findById(req.getParentCommentId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> communityPostService.addComment(postId, req, userDetails));
    }

    @Test
    void addComment_ShouldSaveComment_WhenReplyToSameAuthorAndSamePostAuthor() {
        PostCommentReq req = new PostCommentReq();
        req.setContent("Nice post!");
        
        PostComment parent = new PostComment();
        parent.setId(UUID.randomUUID());
        parent.setUser(user); 
        req.setParentCommentId(parent.getId());

        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        
        PostComment savedComment = new PostComment();
        savedComment.setId(UUID.randomUUID());
        savedComment.setPost(post);
        savedComment.setUser(user);
        savedComment.setContent("Nice post!");
        savedComment.setParentComment(parent);
        
        when(postCommentRepository.save(any(PostComment.class))).thenReturn(savedComment);

        communityPostService.addComment(postId, req, userDetails);

        verify(eventPublisher, never()).publishEvent(any()); 
    }

    // GET COMMENTS
    @Test
    void getComments_ShouldReturnPageOfComments() {
        PostComment comment = new PostComment();
        comment.setId(UUID.randomUUID());
        comment.setUser(user);
        Page<PostComment> page = new PageImpl<>(Collections.singletonList(comment));
        
        when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findByPostOrderByCreatedAtAsc(eq(post), any(PageRequest.class))).thenReturn(page);

        Page<PostCommentRes> result = communityPostService.getComments(postId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getComments_ShouldThrowNotFound_WhenPostNotFound() {
        when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> communityPostService.getComments(postId, 0, 10));
    }
}
