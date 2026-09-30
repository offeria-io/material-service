package offeria.material_service.service.importing;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LegacyExcelIngestionServiceTest {

    @Mock
    private LegacyMaterialStagingRepository stagingRepository;

    @InjectMocks
    private LegacyExcelIngestionService ingestionService;

    @Test
    void shouldIngestNonBlankExcelCellsIntoStaging() throws Exception {

        byte[] workbookBytes = createWorkbook();

        int imported = ingestionService.ingest(
                new ByteArrayInputStream(workbookBytes),
                "legacy-materials.xlsx"
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LegacyMaterialStaging>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(stagingRepository).saveAll(captor.capture());

        List<LegacyMaterialStaging> records = captor.getValue();

        assertThat(imported).isEqualTo(3);
        assertThat(records).hasSize(3);

        LegacyMaterialStaging first = records.getFirst();

        assertThat(first.getRawValue())
                .isEqualTo("Oil Filter");

        assertThat(first.getSourceWorkbook())
                .isEqualTo("legacy-materials.xlsx");

        assertThat(first.getSourceSheet())
                .isEqualTo("Materials");

        assertThat(first.getSourceRow())
                .isEqualTo(1);

        assertThat(first.getSourceColumn())
                .isEqualTo("A");

        assertThat(first.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                );
    }

    @Test
    void shouldPreserveRawFormattedValues() throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            var sheet = workbook.createSheet("Legacy");
            var row = sheet.createRow(0);

            row.createCell(0)
                    .setCellValue("8-راس روط سعر 200$");

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            workbook.write(output);

            ingestionService.ingest(
                    new ByteArrayInputStream(output.toByteArray()),
                    "legacy.xlsx"
            );
        }

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LegacyMaterialStaging>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(stagingRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .singleElement()
                .extracting(LegacyMaterialStaging::getRawValue)
                .isEqualTo("8-راس روط سعر 200$");
    }

    @Test
    void shouldSkipBlankCells() throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            var sheet = workbook.createSheet("Materials");
            var row = sheet.createRow(0);

            row.createCell(0).setCellValue("Oil Filter");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("   ");

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            workbook.write(output);

            int imported = ingestionService.ingest(
                    new ByteArrayInputStream(output.toByteArray()),
                    "legacy.xlsx"
            );

            assertThat(imported).isEqualTo(1);
        }

        verify(stagingRepository).saveAll(anyList());
    }

    @Test
    void shouldRejectBlankWorkbookName() {

        assertThatThrownBy(
                () -> ingestionService.ingest(
                        new ByteArrayInputStream(new byte[0]),
                        " "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("workbookName must not be blank");
    }

    private byte[] createWorkbook() throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            var sheet = workbook.createSheet("Materials");

            var firstRow = sheet.createRow(0);
            firstRow.createCell(0).setCellValue("Oil Filter");
            firstRow.createCell(1).setCellValue("فلتر دهن");

            var secondRow = sheet.createRow(1);
            secondRow.createCell(0).setCellValue("");

            var thirdRow = sheet.createRow(2);
            thirdRow.createCell(2).setCellValue("Fuel Filter");

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            workbook.write(output);

            return output.toByteArray();
        }
    }
}
