package offeria.material_service.controller;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import offeria.material_service.service.importing.LegacyMaterialReviewService;
import offeria.material_service.service.importing.LegacyMaterialPromotionService;
import offeria.material_service.service.importing.LegacyMaterialBatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.dto.response.LegacyMaterialBatchItemResponse;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(LegacyMaterialStagingController.class)
class LegacyMaterialStagingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LegacyMaterialStagingRepository stagingRepository;

    @MockitoBean
    private LegacyMaterialReviewService reviewService;

    @MockitoBean
    private LegacyMaterialPromotionService promotionService;

    @MockitoBean
    private LegacyMaterialBatchService batchService;

    @Test
    void shouldListPendingRecords() throws Exception {
        LegacyMaterialStaging staging = pendingRecord();

        when(stagingRepository.findByStatus(
                LegacyMaterialImportStatus.PENDING_REVIEW
        )).thenReturn(List.of(staging));

        mockMvc.perform(
                        get("/api/v1/legacy-material-staging")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(staging.getId().toString()))
                .andExpect(jsonPath("$[0].rawValue")
                        .value("Oil Filter"))
                .andExpect(jsonPath("$[0].status")
                        .value("PENDING_REVIEW"));
    }

    @Test
    void shouldGetRecordById() throws Exception {
        LegacyMaterialStaging staging = pendingRecord();

        when(stagingRepository.findById(staging.getId()))
                .thenReturn(Optional.of(staging));

        mockMvc.perform(
                        get("/api/v1/legacy-material-staging/{id}",
                                staging.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(staging.getId().toString()))
                .andExpect(jsonPath("$.rawValue")
                        .value("Oil Filter"));
    }

    @Test
    void shouldApprovePendingRecord() throws Exception {
        LegacyMaterialStaging staging = pendingRecord();
        staging.setStatus(
                LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
        );
        staging.setReviewNotes("Verified");

        when(reviewService.approve(
                staging.getId(),
                "Verified"
        )).thenReturn(staging);

        mockMvc.perform(
                        post("/api/v1/legacy-material-staging/{id}/approve",
                                staging.getId())
                                .contentType("application/json")
                                .content("""
                                        {
                                          "reviewNotes": "Verified"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("APPROVED_FOR_IMPORT"))
                .andExpect(jsonPath("$.reviewNotes")
                        .value("Verified"));
    }

    @Test
    void shouldRejectPendingRecord() throws Exception {
        LegacyMaterialStaging staging = pendingRecord();
        staging.setStatus(
                LegacyMaterialImportStatus.REJECTED
        );
        staging.setReviewNotes("Invalid legacy value");

        when(reviewService.reject(
                staging.getId(),
                "Invalid legacy value"
        )).thenReturn(staging);

        mockMvc.perform(
                        post("/api/v1/legacy-material-staging/{id}/reject",
                                staging.getId())
                                .contentType("application/json")
                                .content("""
                                        {
                                          "reviewNotes": "Invalid legacy value"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("REJECTED"))
                .andExpect(jsonPath("$.reviewNotes")
                        .value("Invalid legacy value"));
    }

    @Test
    void shouldReturnConflictForInvalidTransition()
            throws Exception {

        UUID id = UUID.randomUUID();

        when(reviewService.approve(id, "Again"))
                .thenThrow(new IllegalStateException(
                        "Only PENDING_REVIEW staging records can be reviewed"
                ));

        mockMvc.perform(
                        post("/api/v1/legacy-material-staging/{id}/approve",
                                id)
                                .contentType("application/json")
                                .content("""
                                        {
                                          "reviewNotes": "Again"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Only PENDING_REVIEW staging records can be reviewed"));
    }

    private LegacyMaterialStaging pendingRecord() {
        return LegacyMaterialStaging.builder()
                .id(UUID.randomUUID())
                .rawValue("Oil Filter")
                .candidateEnglishName("Oil Filter")
                .normalizedEnglishName("oil filter")
                .sourceWorkbook("legacy.xlsx")
                .sourceSheet("Materials")
                .sourceRow(1)
                .sourceColumn("A")
                .status(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                )
                .build();
    }

    @Test
    void shouldPromoteApprovedStagingRecord() throws Exception {
        UUID stagingId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        Material material = Material.builder()
                .id(materialId)
                .canonicalEnglishName("Oil Filter")
                .preferredIraqiName("فلتر دهن")
                .unit("UNKNOWN")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.LEGACY_EXCEL)
                .build();

        when(promotionService.promote(stagingId))
                .thenReturn(material);

        mockMvc.perform(
                        post("/api/v1/legacy-material-staging/{id}/promote", stagingId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId").value(materialId.toString()))
                .andExpect(jsonPath("$.canonicalEnglishName").value("Oil Filter"))
                .andExpect(jsonPath("$.preferredIraqiName").value("فلتر دهن"))
                .andExpect(jsonPath("$.unit").value("UNKNOWN"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.source").value("LEGACY_EXCEL"));

        verify(promotionService).promote(stagingId);
    }


    @Test
    void shouldBatchApproveStagingRecords() throws Exception {
        UUID id = UUID.randomUUID();

        when(batchService.approve(
                anyList(),
                eq("Reviewed")
        )).thenReturn(List.of(
                LegacyMaterialBatchItemResponse.success(id)
        ));

        mockMvc.perform(
                        post("/api/v1/legacy-material-staging/batch/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "stagingIds": ["%s"],
                                          "reviewNotes": "Reviewed"
                                        }
                                        """.formatted(id))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stagingId").value(id.toString()))
                .andExpect(jsonPath("$[0].success").value(true));
    }

    @Test
    void shouldBatchRejectStagingRecords() throws Exception {
        UUID id = UUID.randomUUID();

        when(batchService.reject(
                anyList(),
                eq("Invalid material")
        )).thenReturn(List.of(
                LegacyMaterialBatchItemResponse.success(id)
        ));

        mockMvc.perform(
                        post("/api/v1/legacy-material-staging/batch/reject")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "stagingIds": ["%s"],
                                          "reviewNotes": "Invalid material"
                                        }
                                        """.formatted(id))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true));
    }

    @Test
    void shouldBatchPromoteWithMixedResults() throws Exception {
        UUID successId = UUID.randomUUID();
        UUID failedId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        when(batchService.promote(anyList()))
                .thenReturn(List.of(
                        LegacyMaterialBatchItemResponse.promoted(
                                successId,
                                materialId
                        ),
                        LegacyMaterialBatchItemResponse.failure(
                                failedId,
                                "Only APPROVED_FOR_IMPORT staging records can be promoted"
                        )
                ));

        mockMvc.perform(
                        post("/api/v1/legacy-material-staging/batch/promote")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "stagingIds": ["%s", "%s"]
                                        }
                                        """.formatted(successId, failedId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true))
                .andExpect(jsonPath("$[0].materialId").value(materialId.toString()))
                .andExpect(jsonPath("$[1].success").value(false))
                .andExpect(jsonPath("$[1].message")
                        .value("Only APPROVED_FOR_IMPORT staging records can be promoted"));
    }

}
