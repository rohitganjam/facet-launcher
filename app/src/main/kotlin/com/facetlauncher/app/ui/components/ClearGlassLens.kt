package com.facetlauncher.app.ui.components

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.unit.LayoutDirection
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.isRuntimeShaderSupported

/**
 * The rounded-rectangle refraction shader of Backdrop (Kyant, Apache-2.0, `RoundedRectRefractionShaderString`), with the depth
 * pull as a strength (`depthWeight`) instead of the library's on/off flag. Content within [height] of the edge is moved along the edge
 * normal by up to [amount] px; the depth term adds a pull away from the panel's centre. Needs API 33; below that it does nothing.
 */
internal fun BackdropEffectScope.clearGlassLens(shape: CornerBasedShape, height: Float, amount: Float, depthWeight: Float) {
    if (!isRuntimeShaderSupported() || height <= 0f || amount <= 0f) return
    if (padding > 0f) padding = (padding - height).coerceAtLeast(0f)
    val density: androidx.compose.ui.unit.Density = this
    val panelSize = size
    val maxRadius = panelSize.minDimension / 2f
    val ltr = layoutDirection == LayoutDirection.Ltr
    fun CornerSize.px() = toPx(panelSize, density).coerceAtMost(maxRadius)
    val radii = floatArrayOf(
        (if (ltr) shape.topStart else shape.topEnd).px(),
        (if (ltr) shape.topEnd else shape.topStart).px(),
        (if (ltr) shape.bottomEnd else shape.bottomStart).px(),
        (if (ltr) shape.bottomStart else shape.bottomEnd).px(),
    )
    val offset = -padding
    runtimeShaderEffect("ClearGlassLens", CLEAR_GLASS_LENS_SHADER, "content") {
        setFloatUniform("size", panelSize.width, panelSize.height)
        setFloatUniform("offset", offset, offset)
        setFloatUniform("cornerRadii", radii)
        setFloatUniform("refractionHeight", height)
        setFloatUniform("refractionAmount", -amount)
        setFloatUniform("depthWeight", depthWeight)
    }
}

private const val CLEAR_GLASS_LENS_SHADER = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthWeight;

float radiusAt(float2 coord, float4 radii) {
    if (coord.x >= 0.0) {
        if (coord.y <= 0.0) return radii.y;
        else return radii.z;
    } else {
        if (coord.y <= 0.0) return radii.x;
        else return radii.w;
    }
}

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) {
        return sign(coord) * normalize(max(cornerCoord, 0.0));
    } else {
        float gradX = step(cornerCoord.y, cornerCoord.x);
        return sign(coord) * float2(gradX, 1.0 - gradX);
    }
}

float circleMap(float x) {
    return 1.0 - sqrt(1.0 - x * x);
}

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = (coord + offset) - halfSize;
    float radius = radiusAt(coord, cornerRadii);

    float sd = sdRoundedRect(centeredCoord, halfSize, radius);
    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    sd = min(sd, 0.0);

    float d = circleMap(1.0 - -sd / refractionHeight) * refractionAmount;
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = normalize(gradSdRoundedRect(centeredCoord, halfSize, gradRadius) + depthWeight * normalize(centeredCoord));

    float2 refractedCoord = coord + d * grad;
    return content.eval(refractedCoord);
}
"""
