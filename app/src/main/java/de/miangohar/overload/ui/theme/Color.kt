package de.miangohar.overload.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette. Kept in one file so that the colour roles below stay readable.
val IronBlue = Color(0xFF2E5AAC)
val IronBlueDark = Color(0xFF9FBBF0)
val Chalk = Color(0xFFF6F7F9)
val Graphite = Color(0xFF1B1C1E)

// Semantic colours for the volume status indicators. Each has a separate dark
// theme variant because the light theme values do not clear WCAG AA 4.5:1
// contrast against the dark background on their own, see ADR-0013.
val BelowTarget = Color(0xFFA8530F)
val BelowTargetDark = Color(0xFFE8964C)
val InTarget = Color(0xFF2E7D32)
val InTargetDark = Color(0xFF66BB6A)
val AboveTarget = Color(0xFF8E24AA)
val AboveTargetDark = Color(0xFFCE93D8)
val Untargeted = Color(0xFF6B6F76)
val UntargetedDark = Color(0xFF9AA0A6)
