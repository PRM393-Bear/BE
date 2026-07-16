package com.example.PRM.serviceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @InjectMocks
    private ChatServiceImpl chatService;

    @Mock
    private ChatModel chatModel;

    @Mock
    private VectorStore vectorStore;

    @Test
    void ingestDocumentData_ShouldAddDocumentsToVectorStore() {
        List<String> paragraphs = Arrays.asList("Paragraph 1", "Paragraph 2");

        chatService.ingestDocumentData(paragraphs);

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore, times(1)).add(captor.capture());

        List<Document> addedDocs = captor.getValue();
        assertEquals(2, addedDocs.size());
        assertEquals("Paragraph 1", addedDocs.get(0).getContent());
        assertEquals("Paragraph 2", addedDocs.get(1).getContent());
    }

    @Test
    void askChatbot_ShouldReturnResponseFromChatModel() {
        String userQuestion = "How to register?";
        Document doc1 = new Document("Guide to registration");
        Document doc2 = new Document("Step 1: Go to website");

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(Arrays.asList(doc1, doc2));
        when(chatModel.call(anyString())).thenReturn("This is the answer.");

        String response = chatService.askChatbot(userQuestion);

        assertEquals("This is the answer.", response);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatModel, times(1)).call(promptCaptor.capture());

        String capturedPrompt = promptCaptor.getValue();
        assertTrue(capturedPrompt.contains(userQuestion));
        assertTrue(capturedPrompt.contains("Guide to registration"));
        assertTrue(capturedPrompt.contains("Step 1: Go to website"));
    }
}
