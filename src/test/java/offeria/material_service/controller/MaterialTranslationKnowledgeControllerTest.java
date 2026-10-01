package offeria.material_service.controller;

import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.response.MaterialTranslationKnowledgeResponse;
import offeria.material_service.service.translation.MaterialTranslationKnowledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialTranslationKnowledgeControllerTest {

    @Mock
    private MaterialTranslationKnowledgeService translationKnowledgeService;

    private MaterialTranslationKnowledgeController controller;

    @BeforeEach
    void setUp() {
        controller = new MaterialTranslationKnowledgeController(
                translationKnowledgeService
        );
    }

    @Test
    void shouldReturnTranslationKnowledgeByMaterialId() {
        UUID materialId = UUID.randomUUID();
        MaterialTranslationKnowledgeResponse knowledge = response(materialId);

        when(translationKnowledgeService.findByMaterialId(materialId))
                .thenReturn(Optional.of(knowledge));

        ResponseEntity<MaterialTranslationKnowledgeResponse> response =
                controller.findByMaterialId(materialId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(knowledge, response.getBody());
    }

    @Test
    void shouldReturnNotFoundForUnknownMaterialId() {
        UUID materialId = UUID.randomUUID();

        when(translationKnowledgeService.findByMaterialId(materialId))
                .thenReturn(Optional.empty());

        ResponseEntity<MaterialTranslationKnowledgeResponse> response =
                controller.findByMaterialId(materialId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldReturnTranslationKnowledgeByQuery() {
        UUID materialId = UUID.randomUUID();
        MaterialTranslationKnowledgeResponse knowledge = response(materialId);

        when(translationKnowledgeService.findByQuery("Steel Pipe"))
                .thenReturn(Optional.of(knowledge));

        ResponseEntity<MaterialTranslationKnowledgeResponse> response =
                controller.findByQuery("Steel Pipe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(knowledge, response.getBody());
    }

    @Test
    void shouldReturnNotFoundForUnknownQuery() {
        when(translationKnowledgeService.findByQuery("Unknown"))
                .thenReturn(Optional.empty());

        ResponseEntity<MaterialTranslationKnowledgeResponse> response =
                controller.findByQuery("Unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldRejectBlankQuery() {
        ResponseEntity<MaterialTranslationKnowledgeResponse> response =
                controller.findByQuery("   ");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(translationKnowledgeService);
    }

    private MaterialTranslationKnowledgeResponse response(UUID materialId) {
        return new MaterialTranslationKnowledgeResponse(
                materialId,
                "Steel Pipe",
                "بايب حديد",
                "أنبوب فولاذي",
                "PCS",
                "Piping",
                "Steel Pipe",
                "Test Manufacturer",
                "Test Brand",
                "SP-001",
                "2 inch steel pipe",
                MaterialStatus.APPROVED,
                MaterialSource.MANUAL
        );
    }
}
