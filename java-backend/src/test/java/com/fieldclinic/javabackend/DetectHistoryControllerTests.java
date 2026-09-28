package com.fieldclinic.javabackend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DetectHistoryControllerTests {

    @Mock
    private DetectRecordRepository repository;

    @InjectMocks
    private DetectHistoryController controller;

    @Test
    void rejectsBlankUserId() {
        Map<String, Object> result = controller.history(" ", null, null, null);
        assertEquals(400, result.get("code"));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsUnknownType() {
        Map<String, Object> result = controller.history("user-1", "unknown", null, null);
        assertEquals(400, result.get("code"));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsOversizedPage() {
        Map<String, Object> result = controller.history("user-1", null, 0, 101);
        assertEquals(400, result.get("code"));
        verifyNoInteractions(repository);
    }
}
