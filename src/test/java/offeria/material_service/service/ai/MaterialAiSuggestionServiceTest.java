package offeria.material_service.service.ai;

import offeria.material_service.ai.MaterialAiSuggestionProvider;
import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.dto.response.AiMaterialSuggestionResponse;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.service.matching.MaterialMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialAiSuggestionServiceTest {

    @Mock
    private MaterialMatchingService matchingService;

    @Mock
    private MaterialAiSuggestionProvider aiSuggestionProvider;

    private MaterialAiSuggestionService service;

    @BeforeEach
    void setUp() {
        service = new MaterialAiSuggestionService(
                matchingService,
                aiSuggestionProvider
        );
    }

    @Test
    void shouldNotCallAiWhenMaterialAlreadyExists() {
        MaterialMatchResponse match = new MaterialMatchResponse(
                UUID.randomUUID(),
                "Steel Pipe",
                "بايب حديد",
                MaterialMatchType.EXACT_ENGLISH_NAME,
                "Steel Pipe"
        );

        when(matchingService.findExactMatch("Steel Pipe"))
                .thenReturn(Optional.of(match));

        Optional<AiMaterialSuggestionResponse> result =
                service.suggest("Steel Pipe");

        assertTrue(result.isEmpty());
        verifyNoInteractions(aiSuggestionProvider);
    }

    @Test
    void shouldReturnUnapprovedAiSuggestionForUnknownMaterial() {
        when(matchingService.findExactMatch("Unknown Material"))
                .thenReturn(Optional.empty());

        AiMaterialSuggestionResponse providerResponse =
                AiMaterialSuggestionResponse.unapproved(
                        "Unknown Material",
                        "Suggested Material",
                        "مادة مقترحة",
                        "مادة مقترحة",
                        "PCS",
                        "General",
                        "AI generated specification"
                );

        when(aiSuggestionProvider.suggest("Unknown Material"))
                .thenReturn(Optional.of(providerResponse));

        Optional<AiMaterialSuggestionResponse> result =
                service.suggest("Unknown Material");

        assertTrue(result.isPresent());
        assertFalse(result.get().approved());
        assertEquals("Suggested Material", result.get().canonicalEnglishName());
        assertEquals("مادة مقترحة", result.get().preferredIraqiName());
    }

    @Test
    void shouldReturnEmptyWhenProviderCannotSuggest() {
        when(matchingService.findExactMatch("Unknown Material"))
                .thenReturn(Optional.empty());

        when(aiSuggestionProvider.suggest("Unknown Material"))
                .thenReturn(Optional.empty());

        Optional<AiMaterialSuggestionResponse> result =
                service.suggest("Unknown Material");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectBlankInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.suggest("   ")
        );

        verifyNoInteractions(matchingService);
        verifyNoInteractions(aiSuggestionProvider);
    }

    @Test
    void shouldRejectNullInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.suggest(null)
        );

        verifyNoInteractions(matchingService);
        verifyNoInteractions(aiSuggestionProvider);
    }

    @Test
    void shouldTrimInputBeforeCallingProvider() {
        when(matchingService.findExactMatch("Unknown Material"))
                .thenReturn(Optional.empty());

        when(aiSuggestionProvider.suggest("Unknown Material"))
                .thenReturn(Optional.empty());

        service.suggest("  Unknown Material  ");

        verify(matchingService).findExactMatch("Unknown Material");
        verify(aiSuggestionProvider).suggest("Unknown Material");
    }
}
