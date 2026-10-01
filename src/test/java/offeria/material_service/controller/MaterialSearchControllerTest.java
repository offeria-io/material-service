package offeria.material_service.controller;

import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.service.matching.MaterialMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialSearchControllerTest {

    @Mock
    private MaterialMatchingService matchingService;

    private MaterialSearchController controller;

    @BeforeEach
    void setUp() {
        controller = new MaterialSearchController(matchingService);
    }

    @Test
    void shouldReturnExactMatch() {
        MaterialMatchResponse match = new MaterialMatchResponse(
                UUID.randomUUID(),
                "Steel Pipe",
                "بايب حديد",
                MaterialMatchType.EXACT_ENGLISH_NAME,
                "Steel Pipe"
        );

        when(matchingService.findExactMatch("Steel Pipe"))
                .thenReturn(Optional.of(match));

        ResponseEntity<MaterialMatchResponse> response =
                controller.findExactMatch("Steel Pipe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(match, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenExactMatchDoesNotExist() {
        when(matchingService.findExactMatch("Unknown Material"))
                .thenReturn(Optional.empty());

        ResponseEntity<MaterialMatchResponse> response =
                controller.findExactMatch("Unknown Material");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldRejectBlankExactQuery() {
        ResponseEntity<MaterialMatchResponse> response =
                controller.findExactMatch("   ");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(matchingService);
    }

    @Test
    void shouldReturnSimilarMaterials() {
        MaterialMatchResponse first = new MaterialMatchResponse(
                UUID.randomUUID(),
                "Steel Pipe 2 Inch",
                "بايب حديد 2 انج",
                MaterialMatchType.SIMILAR_NAME,
                "Steel Pipe"
        );

        MaterialMatchResponse second = new MaterialMatchResponse(
                UUID.randomUUID(),
                "Steel Pipe 4 Inch",
                "بايب حديد 4 انج",
                MaterialMatchType.SIMILAR_NAME,
                "Steel Pipe"
        );

        when(matchingService.findSimilar("Steel Pipe"))
                .thenReturn(List.of(first, second));

        ResponseEntity<List<MaterialMatchResponse>> response =
                controller.findSimilar("Steel Pipe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void shouldReturnEmptyListWhenNoSimilarMaterialsExist() {
        when(matchingService.findSimilar("Unknown Material"))
                .thenReturn(List.of());

        ResponseEntity<List<MaterialMatchResponse>> response =
                controller.findSimilar("Unknown Material");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldRejectBlankSimilarQuery() {
        ResponseEntity<List<MaterialMatchResponse>> response =
                controller.findSimilar(" ");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(matchingService);
    }
}
