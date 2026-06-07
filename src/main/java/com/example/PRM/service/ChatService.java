package com.example.PRM.service;

import java.util.List;

public interface ChatService {
    void ingestDocumentData(List<String> textParagraphs);
    String askChatbot(String userQuestion);
}
