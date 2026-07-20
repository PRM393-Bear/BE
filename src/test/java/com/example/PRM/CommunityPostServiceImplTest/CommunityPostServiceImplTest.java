package com.example.PRM.CommunityPostServiceImplTest;

import com.example.PRM.dto.request.community.CommunityPostReq;
import com.example.PRM.dto.request.community.PostCommentReq;
import com.example.PRM.dto.response.community.CommunityPostRes;
import com.example.PRM.dto.response.community.PostCommentRes;
import com.example.PRM.entity.CommunityPost;
import com.example.PRM.entity.DonationEvent;
import com.example.PRM.entity.PostComment;
import com.example.PRM.entity.PostLike;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.event.PostInteractionNotificationEvent;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.CommunityPostRepository;
import com.example.PRM.repository.DonationEventRepository;
import com.example.PRM.repository.PostCommentRepository;
import com.example.PRM.repository.PostLikeRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.CommunityPostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommunityPostServiceImpl.
 * Covers the full flow: create/update/delete/hide/unhide posts, list posts,
 * toggle like (with notification branches), add comment (with notification
 * branches for post author and parent-comment author), and list comments.
 *
 * NOTE: Field/method names on entities & DTOs are inferred from usage in
 * CommunityPostServiceImpl. Adjust getters/setters/builders to match your
 * actual classes if they differ.
 */
@ExtendWith(MockitoExtension.class)
class CommunityPostServiceImplTest {

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
    private DonationEventRepository donationEventRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CommunityPostServiceImpl communityPostService;

    private User orgUser;
    private User memberUser;
    private User adminUser;
    private CommunityPost post;
    private UUID postId;
    private UUID donationEventId;
    private DonationEvent donationEvent;
    private CommunityPostReq postReq;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        donationEventId = UUID.randomUUID();

        Role orgRole = new Role();
        orgRole.setRoleName("ORGANIZATION");

        Role memberRole = new Role();
        memberRole.setRoleName("MEMBER");

        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        orgUser = new User();
        orgUser.setUserId(UUID.randomUUID());
        orgUser.setUserName("org_user");
        orgUser.setFullName("Org User");
        orgUser.setLogoUrl("https://example.com/org.png");
        orgUser.setRole(orgRole);

        memberUser = new User();
        memberUser.setUserId(UUID.randomUUID());
        memberUser.setUserName("member_user");
        memberUser.setFullName("Member User");
        memberUser.setLogoUrl("https://example.com/member.png");
        memberUser.setRole(memberRole);

        adminUser = new User();
        adminUser.setUserId(UUID.randomUUID());
        adminUser.setUserName("admin_user");
        adminUser.setFullName("Admin User");
        adminUser.setRole(adminRole);

        post = new CommunityPost();
        post.setId(UUID.randomUUID());
        post.setUser(orgUser);
        post.setContent("Hello community");
        post.setHidden(false);

        donationEvent = new DonationEvent();
        donationEvent.setId(donationEventId);
        donationEvent.setTitle("Winter Donation Drive");

        postReq = new CommunityPostReq();
        postReq.setContent("Hello community");
        postReq.setImages(List.of("img1.png"));
    }

    // ------------------------------------------------------------------
    // createPost
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Test
        @DisplayName("Tạo bài viết thành công khi không gắn donation event")
        void createSuccessWithoutDonationEvent() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(inv -> inv.getArgument(0));
            when(postLikeRepository.countByPost(any())).thenReturn(0L);
            when(postCommentRepository.countByPost(any())).thenReturn(0L);

            CommunityPostRes result = communityPostService.createPost(postReq, userDetails);

            assertNotNull(result);
            assertEquals("Hello community", result.getContent());
            assertNull(result.getDonationEventId());
            verify(donationEventRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Tạo bài viết thành công khi có gắn donation event hợp lệ")
        void createSuccessWithDonationEvent() {
            postReq.setDonationEventId(donationEventId);
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(inv -> inv.getArgument(0));
            when(postLikeRepository.countByPost(any())).thenReturn(0L);
            when(postCommentRepository.countByPost(any())).thenReturn(0L);

            CommunityPostRes result = communityPostService.createPost(postReq, userDetails);

            assertNotNull(result);
            assertEquals(donationEventId, result.getDonationEventId());
            assertEquals("Winter Donation Drive", result.getDonationEventTitle());
        }

        @Test
        @DisplayName("Ném lỗi khi donationEventId không tồn tại")
        void createThrowsWhenDonationEventNotFound() {
            postReq.setDonationEventId(donationEventId);
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.createPost(postReq, userDetails));
            verify(communityPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi user không có role ORGANIZATION")
        void createThrowsWhenNotOrganizationRole() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));

            assertThrows(ForbiddenException.class,
                    () -> communityPostService.createPost(postReq, userDetails));
            verify(communityPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void createThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.createPost(postReq, userDetails));
        }
    }

    // ------------------------------------------------------------------
    // updatePost
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("updatePost")
    class UpdatePost {

        @Test
        @DisplayName("Cập nhật thành công khi là chủ bài viết, gắn donation event mới")
        void updateSuccessWithDonationEvent() {
            CommunityPostReq updateReq = new CommunityPostReq();
            updateReq.setContent("Updated content");
            updateReq.setImages(List.of("new.png"));
            updateReq.setDonationEventId(donationEventId);

            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(communityPostRepository.save(post)).thenReturn(post);
            when(postLikeRepository.countByPost(post)).thenReturn(0L);
            when(postCommentRepository.countByPost(post)).thenReturn(0L);

            CommunityPostRes result = communityPostService.updatePost(postId, updateReq, userDetails);

            assertNotNull(result);
            assertEquals("Updated content", post.getContent());
            assertEquals(donationEvent, post.getDonationEvent());
        }

        @Test
        @DisplayName("Gỡ donation event khi donationEventId là null")
        void updateRemovesDonationEventWhenNull() {
            post.setDonationEvent(donationEvent);
            CommunityPostReq updateReq = new CommunityPostReq();
            updateReq.setContent("Updated content");

            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(communityPostRepository.save(post)).thenReturn(post);
            when(postLikeRepository.countByPost(post)).thenReturn(0L);
            when(postCommentRepository.countByPost(post)).thenReturn(0L);

            communityPostService.updatePost(postId, updateReq, userDetails);

            assertNull(post.getDonationEvent());
        }

        @Test
        @DisplayName("Ném lỗi khi donationEventId mới không tồn tại")
        void updateThrowsWhenDonationEventNotFound() {
            CommunityPostReq updateReq = new CommunityPostReq();
            updateReq.setDonationEventId(donationEventId);

            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.updatePost(postId, updateReq, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy bài viết")
        void updateThrowsWhenPostNotFound() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.updatePost(postId, postReq, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi user không phải chủ bài viết")
        void updateThrowsWhenNotOwner() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            assertThrows(ForbiddenException.class,
                    () -> communityPostService.updatePost(postId, postReq, userDetails));
            verify(communityPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void updateThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.updatePost(postId, postReq, userDetails));
        }
    }

    // ------------------------------------------------------------------
    // deletePost
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Test
        @DisplayName("Xóa thành công khi là chủ bài viết")
        void deleteSuccessAsOwner() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            communityPostService.deletePost(postId, userDetails);

            verify(postLikeRepository).deleteAllByPost(post);
            verify(postCommentRepository).deleteAllByPost(post);
            verify(communityPostRepository).delete(post);
        }

        @Test
        @DisplayName("Xóa thành công khi là ADMIN dù không phải chủ bài viết")
        void deleteSuccessAsAdmin() {
            when(userDetails.getUsername()).thenReturn("admin_user");
            when(userRepository.findByUserName("admin_user")).thenReturn(Optional.of(adminUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            communityPostService.deletePost(postId, userDetails);

            verify(communityPostRepository).delete(post);
        }

        @Test
        @DisplayName("Ném lỗi khi không phải chủ bài viết và không phải ADMIN")
        void deleteThrowsWhenNotOwnerNorAdmin() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            assertThrows(ForbiddenException.class,
                    () -> communityPostService.deletePost(postId, userDetails));
            verify(communityPostRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy bài viết")
        void deleteThrowsWhenPostNotFound() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.deletePost(postId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void deleteThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.deletePost(postId, userDetails));
        }
    }

    // ------------------------------------------------------------------
    // hidePost
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("hidePost")
    class HidePost {

        @Test
        @DisplayName("Ẩn thành công khi là chủ bài viết")
        void hideSuccessAsOwner() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            communityPostService.hidePost(postId, userDetails);

            assertTrue(post.isHidden());
            verify(communityPostRepository).save(post);
        }

        @Test
        @DisplayName("Ẩn thành công khi là ADMIN")
        void hideSuccessAsAdmin() {
            when(userDetails.getUsername()).thenReturn("admin_user");
            when(userRepository.findByUserName("admin_user")).thenReturn(Optional.of(adminUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            communityPostService.hidePost(postId, userDetails);

            assertTrue(post.isHidden());
        }

        @Test
        @DisplayName("Ném lỗi khi không phải chủ bài viết và không phải ADMIN")
        void hideThrowsWhenNotOwnerNorAdmin() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            assertThrows(ForbiddenException.class,
                    () -> communityPostService.hidePost(postId, userDetails));
            verify(communityPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy bài viết")
        void hideThrowsWhenPostNotFound() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.hidePost(postId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void hideThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.hidePost(postId, userDetails));
            verify(communityPostRepository, never()).findById(any());
        }
    }

    // ------------------------------------------------------------------
    // unhidePost
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("unhidePost")
    class UnhidePost {

        @Test
        @DisplayName("Bỏ ẩn thành công khi là chủ bài viết")
        void unhideSuccessAsOwner() {
            post.setHidden(true);
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            communityPostService.unhidePost(postId, userDetails);

            assertFalse(post.isHidden());
            verify(communityPostRepository).save(post);
        }

        @Test
        @DisplayName("Bỏ ẩn thành công khi là ADMIN")
        void unhideSuccessAsAdmin() {
            post.setHidden(true);
            when(userDetails.getUsername()).thenReturn("admin_user");
            when(userRepository.findByUserName("admin_user")).thenReturn(Optional.of(adminUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            communityPostService.unhidePost(postId, userDetails);

            assertFalse(post.isHidden());
        }

        @Test
        @DisplayName("Ném lỗi khi không phải chủ bài viết và không phải ADMIN")
        void unhideThrowsWhenNotOwnerNorAdmin() {
            post.setHidden(true);
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));

            assertThrows(ForbiddenException.class,
                    () -> communityPostService.unhidePost(postId, userDetails));
            verify(communityPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy bài viết")
        void unhideThrowsWhenPostNotFound() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.unhidePost(postId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void unhideThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.unhidePost(postId, userDetails));
            verify(communityPostRepository, never()).findById(any());
        }
    }

    // ------------------------------------------------------------------
    // getAllPosts
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getAllPosts")
    class GetAllPosts {

        @Test
        @DisplayName("Trả về danh sách bài viết khi userDetails null (khách chưa đăng nhập)")
        void getAllPostsWhenUserDetailsNull() {
            when(communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(PageRequest.of(0, 10)))
                    .thenReturn(new PageImpl<>(List.of(post)));
            when(postLikeRepository.countByPost(post)).thenReturn(3L);
            when(postCommentRepository.countByPost(post)).thenReturn(2L);

            Page<CommunityPostRes> result = communityPostService.getAllPosts(0, 10, null);

            assertEquals(1, result.getTotalElements());
            assertFalse(result.getContent().get(0).isLikedByMe());
            verifyNoInteractions(userRepository);
            verify(postLikeRepository, never()).existsByPostAndUser(any(), any());
        }

        @Test
        @DisplayName("Trả về danh sách bài viết với isLikedByMe=true khi user đã like")
        void getAllPostsWhenUserLikedPost() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(PageRequest.of(0, 10)))
                    .thenReturn(new PageImpl<>(List.of(post)));
            when(postLikeRepository.countByPost(post)).thenReturn(3L);
            when(postCommentRepository.countByPost(post)).thenReturn(2L);
            when(postLikeRepository.existsByPostAndUser(post, memberUser)).thenReturn(true);

            Page<CommunityPostRes> result = communityPostService.getAllPosts(0, 10, userDetails);

            assertTrue(result.getContent().get(0).isLikedByMe());
        }

        @Test
        @DisplayName("currentUser là null khi userDetails có nhưng user không tồn tại")
        void getAllPostsWhenUserDetailsPresentButUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());
            when(communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(PageRequest.of(0, 10)))
                    .thenReturn(new PageImpl<>(List.of(post)));
            when(postLikeRepository.countByPost(post)).thenReturn(0L);
            when(postCommentRepository.countByPost(post)).thenReturn(0L);

            Page<CommunityPostRes> result = communityPostService.getAllPosts(0, 10, userDetails);

            assertFalse(result.getContent().get(0).isLikedByMe());
            verify(postLikeRepository, never()).existsByPostAndUser(any(), any());
        }
    }

    // ------------------------------------------------------------------
    // toggleLike
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("toggleLike")
    class ToggleLike {

        @Test
        @DisplayName("Bỏ like khi đã like trước đó")
        void toggleLikeRemovesExistingLike() {
            PostLike existing = new PostLike();
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postLikeRepository.findByPostAndUser(post, memberUser)).thenReturn(Optional.of(existing));

            communityPostService.toggleLike(postId, userDetails);

            verify(postLikeRepository).delete(existing);
            verify(postLikeRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Thêm like mới và gửi thông báo khi người like khác tác giả")
        void toggleLikeAddsNewLikeAndNotifiesDifferentAuthor() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postLikeRepository.findByPostAndUser(post, memberUser)).thenReturn(Optional.empty());

            communityPostService.toggleLike(postId, userDetails);

            verify(postLikeRepository).save(any(PostLike.class));
            verify(eventPublisher).publishEvent(any(PostInteractionNotificationEvent.class));
        }

        @Test
        @DisplayName("Thêm like mới nhưng không gửi thông báo khi tự like bài của mình")
        void toggleLikeAddsNewLikeButNoNotificationWhenSelfLike() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postLikeRepository.findByPostAndUser(post, orgUser)).thenReturn(Optional.empty());

            communityPostService.toggleLike(postId, userDetails);

            verify(postLikeRepository).save(any(PostLike.class));
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy bài viết")
        void toggleLikeThrowsWhenPostNotFound() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.toggleLike(postId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void toggleLikeThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.toggleLike(postId, userDetails));
        }
    }

    // ------------------------------------------------------------------
    // addComment
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("addComment")
    class AddComment {

        private PostCommentReq commentReq;

        @BeforeEach
        void setUpComment() {
            commentReq = new PostCommentReq();
            commentReq.setContent("Nice post!");
        }

        @Test
        @DisplayName("Bình luận cấp cao nhất, tác giả khác -> gửi 1 thông báo cho tác giả bài viết")
        void addTopLevelCommentNotifiesPostAuthor() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.save(any(PostComment.class))).thenAnswer(inv -> inv.getArgument(0));

            PostCommentRes result = communityPostService.addComment(postId, commentReq, userDetails);

            assertNotNull(result);
            verify(eventPublisher, times(1)).publishEvent(any(PostInteractionNotificationEvent.class));
        }

        @Test
        @DisplayName("Bình luận cấp cao nhất, tự bình luận bài của mình -> không gửi thông báo")
        void addTopLevelCommentNoNotificationWhenSelfComment() {
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.save(any(PostComment.class))).thenAnswer(inv -> inv.getArgument(0));

            communityPostService.addComment(postId, commentReq, userDetails);

            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Trả lời bình luận, tác giả bài viết và tác giả bình luận cha đều khác -> gửi 2 thông báo")
        void addReplyNotifiesBothPostAuthorAndParentCommentAuthor() {
            UUID parentCommentId = UUID.randomUUID();
            User parentCommentAuthor = new User();
            parentCommentAuthor.setUserId(UUID.randomUUID());
            parentCommentAuthor.setFullName("Parent Author");

            PostComment parentComment = new PostComment();
            parentComment.setId(parentCommentId);
            parentComment.setUser(parentCommentAuthor);

            commentReq.setParentCommentId(parentCommentId);

            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
            when(postCommentRepository.save(any(PostComment.class))).thenAnswer(inv -> inv.getArgument(0));

            communityPostService.addComment(postId, commentReq, userDetails);

            verify(eventPublisher, times(2)).publishEvent(any(PostInteractionNotificationEvent.class));
        }

        @Test
        @DisplayName("Trả lời bình luận của chính mình trên bài người khác -> chỉ gửi 1 thông báo cho tác giả bài viết")
        void addReplyOnlyNotifiesPostAuthorWhenReplyingToOwnComment() {
            UUID parentCommentId = UUID.randomUUID();
            PostComment parentComment = new PostComment();
            parentComment.setId(parentCommentId);
            parentComment.setUser(memberUser); // tác giả comment cha == người đang bình luận

            commentReq.setParentCommentId(parentCommentId);

            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
            when(postCommentRepository.save(any(PostComment.class))).thenAnswer(inv -> inv.getArgument(0));

            communityPostService.addComment(postId, commentReq, userDetails);

            verify(eventPublisher, times(1)).publishEvent(any(PostInteractionNotificationEvent.class));
        }

        @Test
        @DisplayName("Trả lời bình luận trên bài viết của chính mình -> chỉ gửi 1 thông báo cho tác giả bình luận cha")
        void addReplyOnlyNotifiesParentAuthorWhenPostIsOwnedBySelf() {
            UUID parentCommentId = UUID.randomUUID();
            User parentCommentAuthor = new User();
            parentCommentAuthor.setUserId(UUID.randomUUID());
            parentCommentAuthor.setFullName("Parent Author");

            PostComment parentComment = new PostComment();
            parentComment.setId(parentCommentId);
            parentComment.setUser(parentCommentAuthor);

            commentReq.setParentCommentId(parentCommentId);

            // org_user là tác giả của post, và cũng là người bình luận (trả lời)
            when(userDetails.getUsername()).thenReturn("org_user");
            when(userRepository.findByUserName("org_user")).thenReturn(Optional.of(orgUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
            when(postCommentRepository.save(any(PostComment.class))).thenAnswer(inv -> inv.getArgument(0));

            communityPostService.addComment(postId, commentReq, userDetails);

            verify(eventPublisher, times(1)).publishEvent(any(PostInteractionNotificationEvent.class));
        }

        @Test
        @DisplayName("Ném lỗi khi parentCommentId không tồn tại")
        void addCommentThrowsWhenParentCommentNotFound() {
            UUID parentCommentId = UUID.randomUUID();
            commentReq.setParentCommentId(parentCommentId);

            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.findById(parentCommentId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.addComment(postId, commentReq, userDetails));
            verify(postCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy bài viết")
        void addCommentThrowsWhenPostNotFound() {
            when(userDetails.getUsername()).thenReturn("member_user");
            when(userRepository.findByUserName("member_user")).thenReturn(Optional.of(memberUser));
            when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.addComment(postId, commentReq, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void addCommentThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.addComment(postId, commentReq, userDetails));
        }
    }

    // ------------------------------------------------------------------
    // getComments
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getComments")
    class GetComments {

        @Test
        @DisplayName("Trả về danh sách bình luận thành công")
        void getCommentsSuccess() {
            PostComment comment = new PostComment();
            comment.setId(UUID.randomUUID());
            comment.setContent("A comment");
            comment.setUser(memberUser);

            when(communityPostRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.findByPostOrderByCreatedAtAsc(post, PageRequest.of(0, 10)))
                    .thenReturn(new PageImpl<>(List.of(comment)));

            Page<PostCommentRes> result = communityPostService.getComments(postId, 0, 10);

            assertEquals(1, result.getTotalElements());
            assertEquals("A comment", result.getContent().get(0).getContent());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy bài viết")
        void getCommentsThrowsWhenPostNotFound() {
            when(communityPostRepository.findById(postId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> communityPostService.getComments(postId, 0, 10));
        }
    }
}