package com.futurae.demoapp.settings.integrity

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.utils.ILCEState
import com.futurae.demoapp.R
import com.futurae.demoapp.ui.shared.elements.error.ErrorScreen
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.OnSecondaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@Composable
fun IntegritySettingsScreen() {
    val integritySettingsViewModel: IntegritySettingsViewModel = viewModel(
        factory = IntegritySettingsViewModel.provideFactory()
    )

    val uiState by integritySettingsViewModel.integrityVerdict.collectAsState(ILCEState.Loading)

    IntegritySettings(uiState)
}

@Composable
private fun IntegritySettings(uiState: ILCEState<List<IntegrityResultUIItem>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (uiState) {
            is ILCEState.Content -> {
                uiState.data.forEach {
                    IntegrityRow(it)
                }
            }

            is ILCEState.Error -> ErrorScreen(
                titleResId = R.string.sdk_generic_error_title,
                message = uiState.throwable.message ?: "Unknown Error"
            )

            else -> Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun IntegrityRow(
    item: IntegrityResultUIItem
) {
    Column(
        modifier = Modifier.padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = item.title.value(LocalContext.current),
            style = FuturaeTypography.titleH2,
            color = PrimaryColor
        )

        when (item.element) {
            PresentationElement.GRAPHIC -> {
                IntegrityLevelGraphicIndicator(item = item)
            }

            PresentationElement.BAR -> {
                IntegrityLevelBarIndicator(
                    modifier = Modifier.padding(top = 12.dp),
                    item = item
                )
            }
        }
    }
}

@Composable
private fun IntegrityLevelGraphicIndicator(item: IntegrityResultUIItem) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        Image(
            painter = painterResource(id = item.level.toGraphicRes()),
            contentDescription = item.title.value(LocalContext.current),
            modifier = Modifier.size(104.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item.explanationText?.let {
                ExplanationTooltip(it)
            }
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = item.informativeText.value(LocalContext.current),
                style = FuturaeTypography.bodySmallRegular,
                color = PrimaryColor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplanationTooltip(text: TextWrapper) {
    val tooltipState = rememberBasicTooltipState()
    val scope = rememberCoroutineScope()
    val topOffset = with(LocalDensity.current) { 4.dp.toPx() }
    val startOffset = with(LocalDensity.current) { 10.dp.toPx() }

    BasicTooltipBox(
        positionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val x = anchorBounds.left - startOffset.toInt()
                val y = anchorBounds.bottom + topOffset.toInt()
                return IntOffset(x, y)
            }
        },
        tooltip = {
            Box(
                modifier = Modifier
                    .background(OnPrimaryColor)
                    .fillMaxWidth(0.9f)
                    .padding(8.dp)
            ) {
                Text(
                    text = text.value(LocalContext.current),
                    modifier = Modifier.padding(8.dp),
                    color = OnSecondaryColor
                )
            }
        },
        state = tooltipState,
    ) {
        Icon(
            modifier = Modifier
                .size(20.dp)
                .clickable { scope.launch { tooltipState.show() } },
            painter = painterResource(R.drawable.ic_info),
            contentDescription = "Integrity Verdict",
            tint = PrimaryColor
        )
    }
}

@Composable
private fun IntegrityLevelBarIndicator(
    modifier: Modifier,
    item: IntegrityResultUIItem
) {
    val indicatorPosition = when (item.level) {
        IntegrityLevel.NONE -> 0.8f
        IntegrityLevel.WEAK,
        IntegrityLevel.BASIC -> 0.5f

        IntegrityLevel.STRONG -> 0.2f
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Green, Color.Yellow, Color.Red)
                        )
                    )
            )
            TriangleIndicator(indicatorPosition)
        }

        Row(
            modifier = Modifier.padding(start = 4.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(item.level.toDrawableRes()),
                contentDescription = "Integrity Verdict",
                tint = PrimaryColor
            )
            Text(
                text = item.informativeText.value(LocalContext.current),
                style = FuturaeTypography.bodySmallRegular,
                color = PrimaryColor
            )
        }
    }
}

@Composable
fun TriangleIndicator(positionFraction: Float) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val triangleWidth = with(LocalDensity.current) { 26.dp.toPx() }
        val adjustedOffset = (positionFraction * totalWidth) - (triangleWidth / 2)

        Image(
            painter = painterResource(id = R.drawable.graphic_indicator),
            contentDescription = null,
            modifier = Modifier.offset(x = with(LocalDensity.current) { adjustedOffset.toDp() })
        )
    }
}


class IntegrityResultsProvider :
    androidx.compose.ui.tooling.preview.PreviewParameterProvider<ILCEState<List<IntegrityResultUIItem>>> {
    override val values: Sequence<ILCEState<List<IntegrityResultUIItem>>> = sequenceOf(
        ILCEState.Loading,
        ILCEState.Content(
            listOf(
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_device),
                    level = IntegrityLevel.STRONG,
                    element = PresentationElement.BAR,
                    informativeText = TextWrapper.Resource(R.string.strong)
                ),
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_app),
                    level = IntegrityLevel.STRONG,
                    element = PresentationElement.GRAPHIC,
                    informativeText = TextWrapper.Resource(R.string.app_verdict_strong_info),
                    explanationText = TextWrapper.Resource(R.string.app_verdict_strong_explanation)
                ),
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_license),
                    level = IntegrityLevel.STRONG,
                    element = PresentationElement.GRAPHIC,
                    informativeText = TextWrapper.Resource(R.string.license_verdict_strong_info),
                    explanationText = TextWrapper.Resource(R.string.license_verdict_strong_explanation)
                )
            )
        ),
        ILCEState.Content(
            listOf(
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_device),
                    level = IntegrityLevel.BASIC,
                    element = PresentationElement.BAR,
                    informativeText = TextWrapper.Resource(R.string.basic)
                ),
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_app),
                    level = IntegrityLevel.WEAK,
                    element = PresentationElement.GRAPHIC,
                    informativeText = TextWrapper.Resource(R.string.app_verdict_weak_info),
                    explanationText = TextWrapper.Resource(R.string.app_verdict_weak_explanation)
                ),
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_license),
                    level = IntegrityLevel.WEAK,
                    element = PresentationElement.GRAPHIC,
                    informativeText = TextWrapper.Resource(R.string.license_verdict_weak_info),
                    explanationText = TextWrapper.Resource(R.string.license_verdict_weak_explanation)
                )
            )
        ),
        ILCEState.Content(
            listOf(
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_device),
                    level = IntegrityLevel.NONE,
                    element = PresentationElement.BAR,
                    informativeText = TextWrapper.Resource(R.string.none)
                ),
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_app),
                    level = IntegrityLevel.NONE,
                    element = PresentationElement.GRAPHIC,
                    informativeText = TextWrapper.Resource(R.string.app_verdict_none_info),
                    explanationText = TextWrapper.Resource(R.string.app_verdict_none_explanation)
                ),
                IntegrityResultUIItem(
                    title = TextWrapper.Resource(R.string.integrity_license),
                    level = IntegrityLevel.NONE,
                    element = PresentationElement.GRAPHIC,
                    informativeText = TextWrapper.Resource(R.string.license_verdict_none_info),
                    explanationText = TextWrapper.Resource(R.string.license_verdict_none_explanation)
                )
            )
        ),
        ILCEState.Error(Throwable("Something went wrong"))
    )
}

@Preview(showBackground = true)
@Composable
fun IntegrityResultsScreenPreview(
    @PreviewParameter(provider = IntegrityResultsProvider::class)
    state: ILCEState<List<IntegrityResultUIItem>>
) {
    IntegritySettings(state)
}
