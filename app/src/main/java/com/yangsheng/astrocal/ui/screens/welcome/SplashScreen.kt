package com.yangsheng.astrocal.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angle: Float,
    val speed: Float,
    val radius: Float,
    val jitter: Float,
    val tail: Float
)

@Composable
fun SplashScreen(
    onDone: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    LaunchedEffect(Unit) {
        delay(3950)
        onDone()
    }

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    // ✅ logo 缩放：从 0.35 -> 1.15（overshoot）-> 1.0
    val scaleAnim = remember { Animatable(0.35f) }
    LaunchedEffect(Unit) {
        // 第一段：放大到 1.15（更大幅度）
        scaleAnim.animateTo(
            targetValue = 4.0f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
        // 第二段：回弹到 1.0（慢一点）
        scaleAnim.animateTo(
            targetValue = 2.0f,
            animationSpec = spring(
                dampingRatio = 0.55f,
                stiffness = 120f
            )
        )
    }

    // ✅ 漂浮幅度更大、周期更慢
    val floatTrans = rememberInfiniteTransition(label = "float")
    val dy by floatTrans.animateFloat(
        initialValue = 12f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dy"
    )

    // ✅ 粒子进度更慢
    val particleT by animateFloatAsState(
        targetValue = 6f,
        animationSpec = tween(durationMillis = 2500, easing = CubicBezierEasing(0.10f, 0.90f, 0.18f, 1f)),
        label = "particleT"
    )

    // 固定粒子
    val particles = remember {
        val rnd = Random(20260228)
        List(140) {
            val a = rnd.nextFloat() * (Math.PI.toFloat() * 2f)
            Particle(
                angle = a,
                speed = 130f + rnd.nextFloat() * 360f,
                radius = 1.0f + rnd.nextFloat() * 3.0f,
                jitter = (rnd.nextFloat() - 0.5f) * 0.65f,
                tail = 0.6f + rnd.nextFloat() * 1.6f
            )
        }
    }

    val density = LocalDensity.current
    val logoSizeDp = 160.dp // ✅ logo 大一点
    val logoSizePx = with(density) { logoSizeDp.toPx() }

    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 粒子+拖尾层（在 logo 下）
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f + dy.dp.toPx())

                val p = particleT.coerceIn(0f, 1f)
                val fly = (1f - (1f - p) * (1f - p)) // easeOutQuad
                val fade = (1f - p).coerceIn(0f, 1f)

                val startRing = logoSizePx * 0.42f

                particles.forEachIndexed { idx, part ->
                    val ang = part.angle + part.jitter * p
                    val dir = Offset(cos(ang), sin(ang))

                    val dist = startRing + part.speed * fly
                    val pos = center + dir * dist

                    val tailLen = (22f + part.speed * 0.09f) * part.tail * (0.35f + 0.65f * fade)

                    val base = if (idx % 6 == 0) secondary else primary
                    val dotAlpha = (0.85f * fade).coerceIn(0f, 1f)

                    // 主粒子
                    drawCircle(
                        color = base.copy(alpha = dotAlpha),
                        radius = part.radius * (0.85f + 0.35f * (1f - fade)),
                        center = pos
                    )

                    // 拖尾（点串）
                    val segments = 9
                    for (s in 1..segments) {
                        val k = s / segments.toFloat()
                        val tailPos = pos - dir * (tailLen * k)
                        val a = (dotAlpha * (1f - k) * 0.65f).coerceIn(0f, 1f)
                        val r = (part.radius * (0.95f - 0.55f * k)).coerceAtLeast(0.55f)
                        drawCircle(
                            color = base.copy(alpha = a),
                            radius = r,
                            center = tailPos
                        )
                    }
                }
            }

            // ✅ 只显示 Logo（无文字）
            Image(
                painter = painterResource(id = R.drawable.astrocal_logo),
                contentDescription = "AstroCal Logo",
                modifier = Modifier
                    .offset(y = dy.dp)
                    .alpha(alpha)
                    .scale(scaleAnim.value)
                    .size(logoSizeDp)
            )
        }
    }
}

// ---- small vector helpers ----
private operator fun Offset.plus(other: Offset) = Offset(x + other.x, y + other.y)
private operator fun Offset.minus(other: Offset) = Offset(x - other.x, y - other.y)
private operator fun Offset.times(s: Float) = Offset(x * s, y * s)