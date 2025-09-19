package example.beechang.together.ui.component.util.webrtc

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import example.beechang.together.R
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.call.room.VideoScaleType
import example.beechang.together.ui.theme.LocalTogeAppColor
import example.beechang.together.ui.utils.WebRtcLifecycleHandler
import example.beechang.together.webrtc.WebRtcData
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack


@Composable
fun ParticipantCallingView(
    modifier: Modifier = Modifier,
    eglBase: EglBase?,
    webRtcData: WebRtcData,
    participant: RoomParticipantUi,
    scaleType: VideoScaleType = VideoScaleType.ASPECT_FILL,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val rtcScalingType = when (scaleType) {
        VideoScaleType.ASPECT_FIT -> RendererCommon.ScalingType.SCALE_ASPECT_FIT
        VideoScaleType.ASPECT_FILL -> RendererCommon.ScalingType.SCALE_ASPECT_FILL
        VideoScaleType.ASPECT_BALANCED -> RendererCommon.ScalingType.SCALE_ASPECT_BALANCED
    }

    val currentVideoTrack = remember { mutableStateOf<VideoTrack?>(null) }
    val textureViewRenderer = remember { mutableStateOf<TogeTextureViewRenderer?>(null) }

    val isInitialized = remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val webRtcLifecycleHandler = WebRtcLifecycleHandler(
            userId = participant.userId,
            videoTrack = currentVideoTrack,
            renderer = textureViewRenderer,
            isInitialized = isInitialized,
        )
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    webRtcLifecycleHandler.onPause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    webRtcLifecycleHandler.onResume(webRtcData.videoTrack)
                }

                Lifecycle.Event.ON_DESTROY -> {
                    webRtcLifecycleHandler.onDestroy()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webRtcLifecycleHandler.onDestroy()
        }
    }

    LaunchedEffect(webRtcData.videoTrack) {
        if (currentVideoTrack.value != webRtcData.videoTrack) {
            textureViewRenderer.value?.let { renderer ->
                try {
                    currentVideoTrack.value?.removeSink(renderer)
                    if (isInitialized.value && webRtcData.videoTrack != null) {
                        webRtcData.videoTrack.addSink(renderer)
                    }
                } catch (e: Exception) {
                    Log.e("ParticipantCallingView", "Error updating video track sink", e)
                }
            }
            currentVideoTrack.value = webRtcData.videoTrack
        }
    }

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (participant.isSpeaking) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = modifier
            .then(
                if (participant.isSpeaking) {
                    Modifier.border(
                        width = animatedBorderWidth,
                        color = LocalTogeAppColor.current.primary700,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(8.dp))
            .fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .fillMaxSize()
                .align(Alignment.Center),
            factory = { ctx ->
                TogeTextureViewRenderer(ctx).apply {
                    try {
                        eglBase?.let { egl ->
                            init(egl.eglBaseContext, null)
                        }
                        setMirror(webRtcData.isFrontLocalCamera)
                        setScalingType(rtcScalingType)
                        isInitialized.value = true
                    } catch (e: Exception) {
                        Log.e(
                            "ParticipantCallingView",
                            "Error initializing TogeTextureViewRenderer",
                            e
                        )
                    }

                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    textureViewRenderer.value = this
                    webRtcData.videoTrack?.addSink(this)
                    currentVideoTrack.value = webRtcData.videoTrack
                }
            },
            update = { view ->
                view.setScalingType(rtcScalingType)
                view.setMirror(webRtcData.isFrontLocalCamera)
            }
        )

        if (!participant.isCameraOn || participant.isContentBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (participant.isContentBlocked) { // 차단된 사용자
                    if (participant.isShowBlockIndicator) { // 차단된 사용자 - 검은 화면만 또는 차단 아이콘
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.4f)
                                .aspectRatio(1f)
                                .align(Alignment.Center)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_block_red),
                                contentDescription = "Blocked User",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    // 일반적인 카메라 꺼짐 상태 - 프로필 이미지 표시
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.4f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .align(Alignment.Center)
                            .border(2.dp, LocalTogeAppColor.current.grey999, CircleShape)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(participant.getProfileFullUrl())
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // name
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LocalTogeAppColor.current.grey500.copy(alpha = 0.7f))
                .wrapContentSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {

            if (!participant.isMicrophoneOn && !participant.isContentBlocked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mic_off),
                    contentDescription = "Mic Off",
                    tint = LocalTogeAppColor.current.white,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }

            Text(
                text = if (participant.isContentBlocked && participant.isShowBlockIndicator) {
                    stringResource(R.string.blocked_user)
                } else {
                    participant.name
                },
                color = LocalTogeAppColor.current.white,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}


