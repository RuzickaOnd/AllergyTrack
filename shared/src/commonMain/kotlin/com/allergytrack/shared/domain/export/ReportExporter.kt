package com.allergytrack.shared.domain.export

import com.allergytrack.shared.domain.model.Correlation
import com.allergytrack.shared.domain.model.FoodTrigger

/**
 * Generuje lékařský report. Implementace je platformní —
 * na Androidu Canvas + PdfDocument + MediaStore.
 *
 * Vrací absolutní cestu k souboru, ne java.io.File, který v commonMain
 * neexistuje. Platformní vrstva si z cesty soubor vyrobí sama.
 */
interface ReportExporter {
    suspend fun generatePdfReport(
        correlations: List<Correlation>,
        foodTriggers: List<FoodTrigger>,
        aiSummary: String
    ): String?
}
