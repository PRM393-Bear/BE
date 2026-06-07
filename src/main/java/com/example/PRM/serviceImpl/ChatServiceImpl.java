package com.example.PRM.serviceImpl;

import com.example.PRM.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @Override
    public void ingestDocumentData(List<String> textParagraphs) {
        List<Document> documents = textParagraphs.stream()
                .map(Document::new)
                .toList();
        vectorStore.add(documents);
    }

    @Override
    public String askChatbot(String userQuestion) {
        // Retrieve relevant context from database
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(userQuestion).withTopK(4)
        );

        String contextInfo = similarDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));

        // Build RAG prompt for local LLM using standard string concatenation
        String promptTemplate = "Bạn là một trợ lý ảo tư vấn hỗ trợ người dùng của hệ thống Lifecycle Marketplace.\n" +
                "Hãy dựa vào thông tin hướng dẫn chính xác sau đây để trả lời câu hỏi của khách hàng:\n" +
                "---------------------\n" +
                contextInfo + "\n" +
                "---------------------\n" +
                "Câu hỏi: " + userQuestion + "\n\n" +
                "Lưu ý quan trọng:\n" +
                "- Trả lời bằng tiếng Việt một cách tự nhiên, lịch sự.\n" +
                "- Chỉ trả lời dựa trên thông tin đã cung cấp ở trên. Nếu thông tin trên không chứa câu trả lời, hãy nói khéo léo rằng bạn chưa nắm được thông tin này và cần liên hệ hỗ trợ viên sau. Không được bịa ra thông tin ngoài lề.";

        return chatModel.call(promptTemplate);
    }
}
