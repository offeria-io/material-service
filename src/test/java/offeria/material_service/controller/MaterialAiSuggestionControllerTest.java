package offeria.material_service.controller;

import offeria.material_service.dto.request.AiMaterialSuggestionRequest;
import offeria.material_service.dto.response.AiMaterialSuggestionResponse;
import offeria.material_service.service.ai.MaterialAiSuggestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialAiSuggestionControllerTest {

    @Mock
    private MaterialAiSuggestionService suggestionService;

    private MaterialAiSuggestionController controller;

    @BeforeEach
    void setUp() {
        controller = new MaterialAiSuggestionController(suggestionService);
    }

    @Test
    void shouldReturnAiSuggestion() {
        AiMaterialSuggestionResponse suggestion =
                AiMaterialSuggestionResponse.unapproved(
                        "Unknown Material",
                        "Suggested Material",
                        "مادة مقترحة",
                        "مادة مقترحة",
                        "PCS",
                        "General",
                        "AI generated specification"
                );

        when(suggestionService.suggest("Unknown Material"))
                .thenReturn(Optional.of(suggestion));

        ResponseEntity<AiMaterialSuggestionResponse> response =
                controller.suggest(
                        new AiMaterialSuggestionRequest("Unknown Material")
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(suggestion, response.getBody());
        assertFalse(response.getBody().approved());
    }

    @Test
    void shouldReturnNoContentWhenNoSuggestionExists() {
        when(suggestionService.suggest("Unknown Material"))
                .thenReturn(Optional.empty());

        ResponseEntity<AiMaterialSuggestionResponse> response =
                controller.suggest(
                        new AiMaterialSuggestionRequest("Unknown Material")
                );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldRejectBlankMaterialText() {
        ResponseEntity<AiMaterialSuggestionResponse> response =
                controller.suggest(
                        new AiMaterialSuggestionRequest("   ")
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(suggestionService);
    }

    @Test
    void shouldRejectNullMaterialText() {
        ResponseEntity<AiMaterialSuggestionResponse> response =
                controller.suggest(
                        new AiMaterialSuggestionRequest(null)
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(suggestionService);
    }

    @Test
    void shouldRejectNullRequest() {
        ResponseEntity<AiMaterialSuggestionResponse> response =
                controller.suggest(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(suggestionService);
    }
}
