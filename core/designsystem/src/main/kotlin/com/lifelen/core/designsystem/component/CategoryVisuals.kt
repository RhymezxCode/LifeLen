package com.lifelen.core.designsystem.component

import androidx.compose.ui.graphics.Color
import com.lifelen.core.designsystem.theme.CatBook
import com.lifelen.core.designsystem.theme.CatBookTint
import com.lifelen.core.designsystem.theme.CatElectronics
import com.lifelen.core.designsystem.theme.CatElectronicsTint
import com.lifelen.core.designsystem.theme.CatFood
import com.lifelen.core.designsystem.theme.CatFoodTint
import com.lifelen.core.designsystem.theme.CatPlant
import com.lifelen.core.designsystem.theme.CatPlantTint
import com.lifelen.core.designsystem.theme.NeutralAccent
import com.lifelen.core.model.ScanCategory

/** Chip color + label for a category — single source of truth (Design Spec §2.1 category tokens). */
data class CategoryVisual(val label: String, val color: Color, val tint: Color)

fun ScanCategory.visual(): CategoryVisual = when (this) {
    ScanCategory.ELECTRONICS -> CategoryVisual("Electronics", CatElectronics, CatElectronicsTint)
    ScanCategory.FOOD -> CategoryVisual("Food", CatFood, CatFoodTint)
    ScanCategory.PLANT -> CategoryVisual("Plant", CatPlant, CatPlantTint)
    ScanCategory.BOOK -> CategoryVisual("Book", CatBook, CatBookTint)
    ScanCategory.CLOTHING -> CategoryVisual("Clothing", CatBook, CatBookTint)
    ScanCategory.ANIMAL -> CategoryVisual("Animal", CatPlant, CatPlantTint)
    ScanCategory.LANDMARK -> CategoryVisual("Landmark", CatElectronics, CatElectronicsTint)
    ScanCategory.DOCUMENT -> CategoryVisual("Document", NeutralAccent, CatBookTint)
    ScanCategory.GENERIC -> CategoryVisual("Object", NeutralAccent, CatElectronicsTint)
}
