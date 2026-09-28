package com.fieldclinic.javabackend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ImageControllerTests {

    @Mock
    private DetectRecordRepository repository;

    @InjectMocks
    private ImageController controller;

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        Map<String, Object> result = controller.detectDisease(file, "user-1");
        assertEquals(400, result.get("code"));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "text".getBytes());
        Map<String, Object> result = controller.detectDisease(file, "user-1");
        assertEquals(400, result.get("code"));
        verifyNoInteractions(repository);
    }
}
