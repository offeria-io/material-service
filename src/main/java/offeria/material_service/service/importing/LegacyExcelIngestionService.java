package offeria.material_service.service.importing;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LegacyExcelIngestionService {

    private final LegacyMaterialStagingRepository stagingRepository;

    @Transactional
    public int ingest(
            InputStream inputStream,
            String workbookName
    ) throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }

        if (workbookName == null || workbookName.isBlank()) {
            throw new IllegalArgumentException("workbookName must not be blank");
        }

        List<LegacyMaterialStaging> stagingRecords = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            DataFormatter formatter = new DataFormatter();

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {

                        String rawValue = formatter.formatCellValue(cell);

                        if (rawValue == null || rawValue.isBlank()) {
                            continue;
                        }

                        LegacyMaterialStaging stagingRecord =
                                LegacyMaterialStaging.builder()
                                        .rawValue(rawValue)
                                        .sourceWorkbook(workbookName)
                                        .sourceSheet(sheet.getSheetName())
                                        .sourceRow(row.getRowNum() + 1)
                                        .sourceColumn(
                                                CellReferenceHelper.toColumnName(
                                                        cell.getColumnIndex()
                                                )
                                        )
                                        .status(
                                                LegacyMaterialImportStatus.PENDING_REVIEW
                                        )
                                        .build();

                        stagingRecords.add(stagingRecord);
                    }
                }
            }
        }

        stagingRepository.saveAll(stagingRecords);

        return stagingRecords.size();
    }

    private static final class CellReferenceHelper {

        private CellReferenceHelper() {
        }

        private static String toColumnName(int columnIndex) {
            StringBuilder columnName = new StringBuilder();

            int current = columnIndex;

            do {
                columnName.insert(
                        0,
                        (char) ('A' + (current % 26))
                );

                current = (current / 26) - 1;

            } while (current >= 0);

            return columnName.toString();
        }
    }
}
