package com.akole.dividox.common.ui.resources.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Source: .stitch/DESIGN.md → Shape section (DiviDox Finance).
// Metric cards use 24 dp (designMd "xl" rule). Buttons are full-pill via
// Material3's CornerFull default — no override needed here.
internal val DividoxShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chips, badges
    small = RoundedCornerShape(8.dp),        // text fields, ghost inputs
    medium = RoundedCornerShape(12.dp),      // menus, snackbars
    large = RoundedCornerShape(24.dp),       // metric cards (signature xl)
    extraLarge = RoundedCornerShape(28.dp),  // bottom sheets, dialogs
)
