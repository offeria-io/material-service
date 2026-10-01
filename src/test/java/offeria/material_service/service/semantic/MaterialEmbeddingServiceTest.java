package offeria.material_service.service.semantic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaterialEmbeddingServiceTest {

    private EmbeddingModel embeddingModel;
    private MaterialEmbeddingService service;

    @BeforeEach
    void setUp() {
        embeddingModel = mock(EmbeddingModel.class);
        service = new MaterialEmbeddingService(embeddingModel);
    }

    @Test
    void shouldEmbedText() {
        float[] vector = {1.0f, 2.0f};

        when(embeddingModel.embed("Steel Pipe"))
                .thenReturn(vector);

        assertArrayEquals(
                vector,
                service.embed("  Steel Pipe  ")
        );
    }

    @Test
    void shouldRejectBlankText() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.embed(" ")
        );
    }

    @Test
    void shouldCalculateCosineSimilarity() {
        double similarity = service.cosineSimilarity(
                new float[]{1.0f, 0.0f},
                new float[]{1.0f, 0.0f}
        );

        assertEquals(1.0, similarity, 0.0001);
    }

    @Test
    void shouldReturnZeroForInvalidVectors() {
        assertEquals(
                0.0,
                service.cosineSimilarity(
                        new float[]{1.0f},
                        new float[]{1.0f, 2.0f}
                )
        );
    }
}
