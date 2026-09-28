package com.fieldclinic.javabackend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTests {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskController controller;

    @Test
    void rejectsInvalidDate() {
        Map<String, Object> result = controller.list("user-1", "2026/09/28");
        assertEquals(400, result.get("code"));
        verifyNoInteractions(repository);
    }

    @Test
    void cannotCompleteAnotherUsersTask() {
        when(repository.findByIdAndUserId(7L, "user-1")).thenReturn(Optional.empty());
        Map<String, Object> result = controller.complete(7L, "user-1");
        assertEquals(404, result.get("code"));
    }
}
