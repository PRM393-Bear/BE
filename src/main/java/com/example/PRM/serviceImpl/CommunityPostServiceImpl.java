package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.community.CommunityPostReq;
import com.example.PRM.dto.request.community.PostCommentReq;
import com.example.PRM.dto.response.community.CommunityPostRes;
import com.example.PRM.dto.response.community.PostCommentRes;
import com.example.PRM.entity.CommunityPost;
import com.example.PRM.entity.PostComment;
import com.example.PRM.entity.PostLike;
import com.example.PRM.entity.User;
import com.example.PRM.event.PostInteractionNotificationEvent;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.CommunityPostRepository;
import com.example.PRM.repository.PostCommentRepository;
import com.example.PRM.repository.PostLikeRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommunityPostRes createPost(CommunityPostReq req, UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getRole().getRoleName().equals("ORGANIZATION")) {
            throw new ForbiddenException("Only users with ORGANIZATION role can create community posts.");
        }

        CommunityPost post = new CommunityPost();
        post.setUser(user);
        post.setContent(req.getContent());
        post.setImages(req.getImages());

        CommunityPost savedPost = communityPostRepository.save(post);
        return mapToPostRes(savedPost, user);
    }

    @Override
    @Transactional
    public CommunityPostRes updatePost(UUID postId, CommunityPostReq req, UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Community post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You can only edit your own posts.");
        }

        post.setContent(req.getContent());
        post.setImages(req.getImages());

        CommunityPost savedPost = communityPostRepository.save(post);
        return mapToPostRes(savedPost, user);
    }

    @Override
    @Transactional
    public void deletePost(UUID postId, UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Community post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId()) && !user.getRole().getRoleName().equals("ADMIN")) {
            throw new ForbiddenException("You can only delete your own posts.");
        }

        postLikeRepository.deleteAllByPost(post);
        postCommentRepository.deleteAllByPost(post);
        communityPostRepository.delete(post);
    }

    @Override
    @Transactional
    public void hidePost(UUID postId, UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Community post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId()) && !user.getRole().getRoleName().equals("ADMIN")) {
            throw new ForbiddenException("You can only hide your own posts.");
        }

        post.setHidden(true);
        communityPostRepository.save(post);
    }

    @Override
    @Transactional
    public void unhidePost(UUID postId, UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Community post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId()) && !user.getRole().getRoleName().equals("ADMIN")) {
            throw new ForbiddenException("You can only unhide your own posts.");
        }

        post.setHidden(false);
        communityPostRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommunityPostRes> getAllPosts(int page, int size, UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUserName(userDetails.getUsername()).orElse(null);
        }
        
        User finalCurrentUser = currentUser;
        return communityPostRepository.findByIsHiddenFalseOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(post -> mapToPostRes(post, finalCurrentUser));
    }

    @Override
    @Transactional
    public void toggleLike(UUID postId, UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Community post not found"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);
        
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
        } else {
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            postLikeRepository.save(newLike);

            // Notify post author if someone else liked it
            if (!post.getUser().getUserId().equals(user.getUserId())) {
                eventPublisher.publishEvent(new PostInteractionNotificationEvent(
                        this,
                        post.getUser().getUserId(),
                        "Lượt thích mới",
                        user.getFullName() + " đã thích bài viết của bạn."
                ));
            }
        }
    }

    @Override
    @Transactional
    public PostCommentRes addComment(UUID postId, PostCommentReq req, UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Community post not found"));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(req.getContent());

        if (req.getParentCommentId() != null) {
            PostComment parent = postCommentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        PostComment savedComment = postCommentRepository.save(comment);

        // Notify post author if someone else commented
        if (!post.getUser().getUserId().equals(user.getUserId())) {
            eventPublisher.publishEvent(new PostInteractionNotificationEvent(
                    this,
                    post.getUser().getUserId(),
                    "Bình luận mới",
                    user.getFullName() + " đã bình luận về bài viết của bạn: " + req.getContent()
            ));
        }

        // Notify parent comment author if this is a reply and the author is not the same
        if (comment.getParentComment() != null && !comment.getParentComment().getUser().getUserId().equals(user.getUserId())) {
            eventPublisher.publishEvent(new PostInteractionNotificationEvent(
                    this,
                    comment.getParentComment().getUser().getUserId(),
                    "Phản hồi mới",
                    user.getFullName() + " đã trả lời bình luận của bạn: " + req.getContent()
            ));
        }

        return mapToCommentRes(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostCommentRes> getComments(UUID postId, int page, int size) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Community post not found"));
                
        return postCommentRepository.findByPostOrderByCreatedAtAsc(post, PageRequest.of(page, size))
                .map(this::mapToCommentRes);
    }

    private CommunityPostRes mapToPostRes(CommunityPost post, User currentUser) {
        long likeCount = postLikeRepository.countByPost(post);
        long commentCount = postCommentRepository.countByPost(post);
        boolean isLikedByMe = currentUser != null && postLikeRepository.existsByPostAndUser(post, currentUser);

        return CommunityPostRes.builder()
                .id(post.getId())
                .content(post.getContent())
                .images(post.getImages())
                .createdAt(post.getCreatedAt())
                .authorId(post.getUser().getUserId())
                .authorName(post.getUser().getFullName())
                .authorAvatar(post.getUser().getLogoUrl())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLikedByMe(isLikedByMe)
                .isHidden(post.isHidden())
                .build();
    }
    
    private PostCommentRes mapToCommentRes(PostComment comment) {
        return PostCommentRes.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .authorId(comment.getUser().getUserId())
                .authorName(comment.getUser().getFullName())
                .authorAvatar(comment.getUser().getLogoUrl())
                .build();
    }
}
