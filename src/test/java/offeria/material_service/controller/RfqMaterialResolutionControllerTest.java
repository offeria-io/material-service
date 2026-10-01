package offeria.material_service.controller;

import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.dto.request.RfqMaterialResolutionRequest;
import offeria.material_service.dto.response.RfqMaterialResolutionResponse;
import offeria.material_service.service.rfq.RfqMaterialResolutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RfqMaterialResolutionControllerTest {

    @Mock
    private RfqMaterialResolutionService resolutionService;

    private RfqMaterialResolutionController controller;

    @BeforeEach
    void setUp() {
        controller = new RfqMaterialResolutionController(resolutionService);
    }

    @Test
    void shouldResolveRfqMaterial() {
        UUID materialId = UUID.randomUUID();

        RfqMaterialResolutionResponse result =
                new RfqMaterialResolutionResponse(
                        "Steel Pipe",
                        true,
                        materialId,
                        "Steel Pipe",
                        "بايب حديد",
                        "أنبوب فولاذي",
                        MaterialMatchType.EXACT_ENGLISH_NAME,
                        List.of()
                );

        when(resolutionService.resolve("Steel Pipe"))
                .thenReturn(result);

        ResponseEntity<RfqMaterialResolutionResponse> response =
                controller.resolve(
                        new RfqMaterialResolutionRequest("Steel Pipe")
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());

        verify(resolutionService).resolve("Steel Pipe");
    }

    @Test
    void shouldReturnUnresolvedResultWithSimilarCandidates() {
        RfqMaterialResolutionResponse result =
                RfqMaterialResolutionResponse.unresolved(
                        "Steel Pip",
                        List.of()
                );

        when(resolutionService.resolve("Steel Pip"))
                .thenReturn(result);

        ResponseEntity<RfqMaterialResolutionResponse> response =
                controller.resolve(
                        new RfqMaterialResolutionRequest("Steel Pip")
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().resolved());
    }

    @Test
    void shouldRejectBlankMaterialText() {
        ResponseEntity<RfqMaterialResolutionResponse> response =
                controller.resolve(
                        new RfqMaterialResolutionRequest("   ")
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(resolutionService);
    }

    @Test
    void shouldRejectNullMaterialText() {
        ResponseEntity<RfqMaterialResolutionResponse> response =
                controller.resolve(
                        new RfqMaterialResolutionRequest(null)
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(resolutionService);
    }

    @Test
    void shouldRejectNullRequest() {
        ResponseEntity<RfqMaterialResolutionResponse> response =
                controller.resolve(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(resolutionService);
    }
}
