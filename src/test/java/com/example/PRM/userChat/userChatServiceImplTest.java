package com.example.PRM.userChat;

import com.example.PRM.dto.response.ChatMessageRes;
import com.example.PRM.dto.response.ChatRoomRes;
import com.example.PRM.entity.ChatMessage;
import com.example.PRM.entity.ChatRoom;
import com.example.PRM.entity.User;
import com.example.PRM.repository.ChatMessageRepository;
import com.example.PRM.repository.ChatRoomRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.UserChatServiceImpl;
import com.example.PRM.status_enum.MessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserChatServiceImpl}.
 *
 * NOTE: msg.getStatus() is assumed to return a {@code com.example.PRM.status_enum.MessageStatus}
 * enum (mirroring the pattern used by WardrobeStatus / OtpPurpose / AddedVia in this codebase),
 * with a constant named SENT. If your actual enum type or constant name differs, adjust the
 * import and the {@code MessageStatus.SENT} references below accordingly — the rest of the
 * test logic is unaffected.
 */
@ExtendWith(MockitoExtension.class)
class UserChatServiceImplTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    private UserChatServiceImpl userChatService;

    private UUID currentUserId;
    private UUID otherUserId;
    private User currentUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        userChatService = new UserChatServiceImpl(chatRoomRepository, chatMessageRepository, userRepository);

        currentUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        currentUser = new User();
        currentUser.setUserId(currentUserId);
        currentUser.setUserName("current.user");

        otherUser = new User();
        otherUser.setUserId(otherUserId);
        otherUser.setUserName("other.user");
        otherUser.setLogoUrl("http://logo.url/other.png");
    }

    // ---------------------------------------------------------------
    // getChatHistoryWithUser
    // ---------------------------------------------------------------

    @Test
    void getChatHistoryWithUser_currentUserNotFound_throwsNoSuchElementException() {
        when(userRepository.findByUserId(currentUserId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userChatService.getChatHistoryWithUser(currentUserId, otherUserId));

        verify(userRepository, never()).findByUserId(otherUserId);
        verifyNoInteractions(chatRoomRepository);
    }

    @Test
    void getChatHistoryWithUser_otherUserNotFound_throwsNoSuchElementException() {
        when(userRepository.findByUserId(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUserId(otherUserId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userChatService.getChatHistoryWithUser(currentUserId, otherUserId));

        verifyNoInteractions(chatRoomRepository);
    }

    @Test
    void getChatHistoryWithUser_noRoomExists_returnsEmptyList() {
        when(userRepository.findByUserId(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUserId(otherUserId)).thenReturn(Optional.of(otherUser));
        when(chatRoomRepository.findRoomByUsers(currentUser, otherUser)).thenReturn(Optional.empty());

        List<ChatMessageRes> result = userChatService.getChatHistoryWithUser(currentUserId, otherUserId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    void getChatHistoryWithUser_roomExists_returnsMappedMessages() {
        when(userRepository.findByUserId(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUserId(otherUserId)).thenReturn(Optional.of(otherUser));

        UUID roomId = UUID.randomUUID();
        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        when(chatRoomRepository.findRoomByUsers(currentUser, otherUser)).thenReturn(Optional.of(room));

        UUID msgId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();

        ChatMessage message = new ChatMessage();
        message.setId(msgId);
        message.setRoom(room);
        message.setSender(currentUser);
        message.setContent("Hello there");
        message.setImageUrl(null);
        message.setCreatedAt(createdAt);
        message.setStatus(MessageStatus.SENT);

        when(chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)).thenReturn(List.of(message));

        List<ChatMessageRes> result = userChatService.getChatHistoryWithUser(currentUserId, otherUserId);

        assertEquals(1, result.size());
        ChatMessageRes res = result.get(0);
        assertEquals(msgId, res.getId());
        assertEquals(roomId, res.getRoomId());
        assertEquals(currentUserId, res.getSenderId());
        assertEquals("Hello there", res.getContent());
        assertNull(res.getImageUrl());
        assertEquals(createdAt, res.getCreatedAt());
        assertEquals(MessageStatus.SENT.name(), res.getStatus());
    }

    // ---------------------------------------------------------------
    // getMyRooms
    // ---------------------------------------------------------------

    @Test
    void getMyRooms_noRooms_returnsEmptyList() {
        when(chatRoomRepository.findAllByUserId(currentUserId)).thenReturn(List.of());

        List<ChatRoomRes> result = userChatService.getMyRooms(currentUserId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    void getMyRooms_currentUserIsUser1_otherUserIsUser2_noMessages_lastMessageNull() {
        UUID roomId = UUID.randomUUID();
        OffsetDateTime updatedAt = OffsetDateTime.now();

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setUpdatedAt(updatedAt);
        room.setUser1(currentUser);
        room.setUser2(otherUser);

        when(chatRoomRepository.findAllByUserId(currentUserId)).thenReturn(List.of(room));
        when(chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)).thenReturn(List.of());

        List<ChatRoomRes> result = userChatService.getMyRooms(currentUserId);

        assertEquals(1, result.size());
        ChatRoomRes res = result.get(0);
        assertEquals(roomId, res.getRoomId());
        assertEquals(updatedAt, res.getUpdatedAt());
        assertEquals(otherUserId, res.getOtherUserId());
        assertEquals("other.user", res.getOtherUserName());
        assertEquals("http://logo.url/other.png", res.getOtherUserLogo());
        assertNull(res.getLastMessage());
    }

    @Test
    void getMyRooms_currentUserIsUser2_otherUserIsUser1_withTextMessage_lastMessageIsContent() {
        UUID roomId = UUID.randomUUID();

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setUpdatedAt(OffsetDateTime.now());
        room.setUser1(otherUser);
        room.setUser2(currentUser);

        when(chatRoomRepository.findAllByUserId(currentUserId)).thenReturn(List.of(room));

        ChatMessage msg = new ChatMessage();
        msg.setContent("Hi!");
        msg.setImageUrl(null);
        msg.setStatus(MessageStatus.SENT);
        when(chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)).thenReturn(List.of(msg));

        List<ChatRoomRes> result = userChatService.getMyRooms(currentUserId);

        ChatRoomRes res = result.get(0);
        assertEquals(otherUserId, res.getOtherUserId());
        assertEquals("Hi!", res.getLastMessage());
    }

    @Test
    void getMyRooms_lastMessageBlankContentWithImage_lastMessageIsImagePlaceholder() {
        UUID roomId = UUID.randomUUID();

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setUpdatedAt(OffsetDateTime.now());
        room.setUser1(currentUser);
        room.setUser2(otherUser);

        when(chatRoomRepository.findAllByUserId(currentUserId)).thenReturn(List.of(room));

        ChatMessage msg = new ChatMessage();
        msg.setContent("");
        msg.setImageUrl("http://image.url/pic.png");
        msg.setStatus(MessageStatus.SENT);
        when(chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)).thenReturn(List.of(msg));

        List<ChatRoomRes> result = userChatService.getMyRooms(currentUserId);

        assertEquals("[Hình ảnh]", result.get(0).getLastMessage());
    }

    @Test
    void getMyRooms_lastMessageBlankContentNoImage_lastMessageIsEmptyString() {
        UUID roomId = UUID.randomUUID();

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setUpdatedAt(OffsetDateTime.now());
        room.setUser1(currentUser);
        room.setUser2(otherUser);

        when(chatRoomRepository.findAllByUserId(currentUserId)).thenReturn(List.of(room));

        ChatMessage msg = new ChatMessage();
        msg.setContent(null);
        msg.setImageUrl(null);
        msg.setStatus(MessageStatus.SENT);
        when(chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)).thenReturn(List.of(msg));

        List<ChatRoomRes> result = userChatService.getMyRooms(currentUserId);

        assertEquals("", result.get(0).getLastMessage());
    }

    @Test
    void getMyRooms_multipleMessages_usesLastMessageInList() {
        UUID roomId = UUID.randomUUID();

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setUpdatedAt(OffsetDateTime.now());
        room.setUser1(currentUser);
        room.setUser2(otherUser);

        when(chatRoomRepository.findAllByUserId(currentUserId)).thenReturn(List.of(room));

        ChatMessage firstMsg = new ChatMessage();
        firstMsg.setContent("First message");
        firstMsg.setStatus(MessageStatus.SENT);

        ChatMessage lastMsg = new ChatMessage();
        lastMsg.setContent("Most recent message");
        lastMsg.setStatus(MessageStatus.SENT);

        when(chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId))
                .thenReturn(List.of(firstMsg, lastMsg));

        List<ChatRoomRes> result = userChatService.getMyRooms(currentUserId);

        assertEquals("Most recent message", result.get(0).getLastMessage());
    }
}
