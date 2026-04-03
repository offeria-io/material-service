package offeria.material_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import offeria.material_service.service.MaterialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialController.class)
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaterialService materialService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMaterial_ShouldReturnCreated() throws Exception {
        MaterialRequestDTO request = MaterialRequestDTO.builder()
                .nameEn("Wood")
                .nameAr("خشب")
                .unit("meter")
                .build();

        MaterialResponseDTO response = MaterialResponseDTO.builder()
                .id(UUID.randomUUID())
                .nameEn("Wood")
                .nameAr("خشب")
                .unit("meter")
                .build();

        when(materialService.createMaterial(any(MaterialRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nameEn").value("Wood"));
    }
}
