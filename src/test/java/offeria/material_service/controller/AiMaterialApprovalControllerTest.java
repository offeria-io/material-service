package offeria.material_service.controller;

import offeria.material_service.dto.request.AiMaterialApprovalRequest;
import offeria.material_service.dto.response.AiMaterialApprovalResponse;
import offeria.material_service.service.ai.AiMaterialApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMaterialApprovalControllerTest {

    @Mock
    private AiMaterialApprovalService approvalService;

    private AiMaterialApprovalController controller;

    @BeforeEach
    void setUp() {
        controller = new AiMaterialApprovalController(approvalService);
    }

    @Test
    void shouldApproveAiSuggestion() {
        AiMaterialApprovalRequest request = new AiMaterialApprovalRequest(
                "Steel Pipe",
                "بايب حديد",
                "أنبوب فولاذي",
                "M",
                "Piping",
                "Carbon steel"
        );

        AiMaterialApprovalResponse approved =
                new AiMaterialApprovalResponse(
                        UUID.randomUUID(),
                        "Steel Pipe",
                        "بايب حديد",
                        "أنبوب فولاذي",
                        "M",
                        "Piping",
                        "Carbon steel",
                        "APPROVED",
                        "AI_SUGGESTED"
                );

        when(approvalService.approve(request)).thenReturn(approved);

        ResponseEntity<AiMaterialApprovalResponse> response =
                controller.approve(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("APPROVED", response.getBody().status());
        assertEquals("AI_SUGGESTED", response.getBody().source());
    }
}
