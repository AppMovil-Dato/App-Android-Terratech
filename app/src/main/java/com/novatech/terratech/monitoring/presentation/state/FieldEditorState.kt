package com.novatech.terratech.monitoring.presentation.state

import androidx.compose.runtime.saveable.listSaver
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft
import com.novatech.terratech.monitoring.domain.valueobject.ParcelBoundary

data class FieldEditorState(
    val step: Int = 0,
    val name: String = "",
    val crop: String = "",
    val soil: String = "",
    val hectares: String = "",
    val points: List<Coordinates> = emptyList(),
    val drawing: Boolean = false,
) {
    val polygon
        get() = if (drawing) runCatching { ParcelBoundary.of(points) }.getOrNull() else null

    val validDetails
        get() = name.trim().length in 2..100 && crop.trim().length in 1..100

    val validLocation
        get() =
            if (drawing) polygon != null
            else
                points.size == 1 &&
                    hectares.replace(',', '.').toDoubleOrNull()?.let {
                        it > 0 && it * 10000 <= 9999999
                    } == true

    fun draft() =
        FieldDraft.of(
            name,
            crop,
            polygon?.areaM2?.div(10000)
                ?: (hectares.replace(',', '.').toDoubleOrNull() ?: Double.NaN),
            soil.ifBlank { "Sin especificar" },
            points.first().latitude,
            points.first().longitude,
            polygon?.points.orEmpty(),
        )

    companion object {
        val Saver =
            listSaver<FieldEditorState, Any>(
                save = {
                    listOf(it.step, it.name, it.crop, it.soil, it.hectares, it.drawing) +
                        it.points.flatMap { p -> listOf(p.latitude, p.longitude) }
                },
                restore = {
                    FieldEditorState(
                        it[0] as Int,
                        it[1] as String,
                        it[2] as String,
                        it[3] as String,
                        it[4] as String,
                        it.drop(6).chunked(2).map { p ->
                            Coordinates.of(p[0] as Double, p[1] as Double)
                        },
                        it[5] as Boolean,
                    )
                },
            )
    }
}
