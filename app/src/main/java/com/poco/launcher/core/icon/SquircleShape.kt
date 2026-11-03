package com.poco.launcher.core.icon

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * An iOS-inspired Squircle (superellipse) shape with continuous curvature.
 * Provides the signature rounded rectangular curve with smooth transitions.
 */
class SquircleShape(private val cornerRadiusRatio: Float = 0.225f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = createSquirclePath(size.width, size.height, cornerRadiusRatio)
        return Outline.Generic(path)
    }

    companion object {
        fun createSquirclePath(width: Float, height: Float, ratio: Float = 0.225f): Path {
            val path = Path()
            val minSide = minOf(width, height)
            val r = minSide * ratio

            val w = width
            val h = height

            // iOS-like superellipse cubic Bézier control points
            val c = r * 0.551915024494f

            path.moveTo(r, 0f)
            path.lineTo(w - r, 0f)
            path.cubicTo(w - r + c, 0f, w, r - c, w, r)
            path.lineTo(w, h - r)
            path.cubicTo(w, h - r + c, w - r + c, h, w - r, h)
            path.lineTo(r, h)
            path.cubicTo(r - c, h, 0f, h - r + c, 0f, h - r)
            path.lineTo(0f, r)
            path.cubicTo(0f, r - c, r - c, 0f, r, 0f)
            path.close()

            return path
        }
    }
}
