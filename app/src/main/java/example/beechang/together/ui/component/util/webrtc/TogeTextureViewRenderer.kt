package example.beechang.together.ui.component.util.webrtc

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.TextureView
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.RendererCommon
import org.webrtc.RendererCommon.RendererEvents
import org.webrtc.RendererCommon.ScalingType
import org.webrtc.ThreadUtils
import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import java.util.concurrent.CountDownLatch

class TogeTextureViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextureView(context, attrs), VideoSink, TextureView.SurfaceTextureListener {

    private val eglRenderer: EglRenderer = EglRenderer(getResourceName())

    private val videoLayoutMeasure = RendererCommon.VideoLayoutMeasure()

    private val uiThreadHandler = Handler(Looper.getMainLooper())

    private var rendererEvents: RendererEvents? = null

    private var rotatedFrameWidth = 0
    private var rotatedFrameHeight = 0
    private var frameRotation = 0
    private var isInitialized = false
    private var isFirstFrameRendered = false

    init {
        surfaceTextureListener = this
    }

    fun init(sharedContext: EglBase.Context, rendererEvents: RendererEvents?) {
        ThreadUtils.checkIsOnMainThread()
        if (isInitialized) return

        this.rendererEvents = rendererEvents

        eglRenderer.init(sharedContext, EglBase.CONFIG_PLAIN, GlRectDrawer())
        isInitialized = true
    }

    fun release() {
        eglRenderer.release()
    }

    fun setMirror(mirror: Boolean) {
        eglRenderer.setMirror(mirror)
    }

    fun setScalingType(scalingType: ScalingType) {
        ThreadUtils.checkIsOnMainThread()
        videoLayoutMeasure.setScalingType(scalingType)
        requestLayout()
    }

    fun pauseVideo() {
        eglRenderer.pauseVideo()
    }

    fun resumeVideo() {
        eglRenderer.disableFpsReduction()
    }

    override fun onFrame(frame: VideoFrame) {
        eglRenderer.onFrame(frame)

        if (!isFirstFrameRendered) {
            isFirstFrameRendered = true
            rendererEvents?.onFirstFrameRendered()
        }

        if (frame.rotatedWidth != rotatedFrameWidth ||
            frame.rotatedHeight != rotatedFrameHeight ||
            frame.rotation != frameRotation
        ) {
            rotatedFrameWidth = frame.rotatedWidth
            rotatedFrameHeight = frame.rotatedHeight
            frameRotation = frame.rotation

            uiThreadHandler.post { requestLayout() }

            rendererEvents?.onFrameResolutionChanged(
                frame.buffer.width,
                frame.buffer.height,
                frameRotation
            )
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        ThreadUtils.checkIsOnMainThread()
        if (isInitialized) {
            eglRenderer.createEglSurface(surface)
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        ThreadUtils.checkIsOnMainThread()
        val completionLatch = CountDownLatch(1)
        eglRenderer.releaseEglSurface(completionLatch::countDown)
        ThreadUtils.awaitUninterruptibly(completionLatch)
        return true
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        ThreadUtils.checkIsOnMainThread()
        val size = videoLayoutMeasure.measure(widthMeasureSpec, heightMeasureSpec, rotatedFrameWidth, rotatedFrameHeight)
        setMeasuredDimension(size.x, size.y)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        ThreadUtils.checkIsOnMainThread()
        eglRenderer.setLayoutAspectRatio((right - left).toFloat() / (bottom - top).toFloat())
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

    private fun getResourceName(): String {
        return try {
            resources.getResourceEntryName(id)
        } catch (e: Exception) {
            ""
        }
    }
}