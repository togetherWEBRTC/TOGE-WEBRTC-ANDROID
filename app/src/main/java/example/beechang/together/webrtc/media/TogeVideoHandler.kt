package example.beechang.together.webrtc.media

import android.content.Context
import android.util.Log
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Capturer
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CameraVideoCapturer.CameraSwitchHandler
import org.webrtc.CapturerObserver
import org.webrtc.EglBase
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer

class TogeVideoHandler(
    private val context: Context,
    private val eglBase: EglBase,
) {

    /**
     * 사용이유 : 전면 & 후면 스위칭하는 과정에서 스위칭 요청시 전면(1) -> 전면(2) -> 후면 으로 변경이되서 버튼을 두번눌러야
     *          전 후면이 바뀌는 현상이 발생함. getTargetCameraName에서 명시적으로 카메라 이름찾아서 사용하기 위함
     */
    private var isUsingFrontCamera = false

    private val cameraEnumerator: CameraEnumerator by lazy {
        if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator(true)
        }
    }

    private val surfaceTextureHelper = SurfaceTextureHelper.create(
        SURFACE_TEXTURE_THREAD_NAME,
        eglBase.eglBaseContext,
    )

    private val videoCapturer: VideoCapturer by lazy {
        createVideoCapturer(isUsingFrontCamera)
    }

    fun initializeVideoCapturer(capturerObserver: CapturerObserver) {
        videoCapturer.initialize(surfaceTextureHelper, context, capturerObserver)
    }

    fun startVideoCapture(isFrontCamera: Boolean = false) {
        val optimalResolution = getOptimalResolution(isFrontCamera)
        videoCapturer.startCapture(
            optimalResolution.width,
            optimalResolution.height,
            DEFAULT_FRAME_RATE
        )
    }

    fun getIsUsingFrontCamera(): Boolean = isUsingFrontCamera

    fun switchCamera() {
        if (cameraEnumerator.deviceNames.size < 2) {
            return
        }

        isUsingFrontCamera = !isUsingFrontCamera
        val targetCameraName = getTargetCameraName(isUsingFrontCamera)

        targetCameraName?.let { cameraName ->
            when (videoCapturer) {
                is Camera2Capturer -> (videoCapturer as Camera2Capturer).switchCamera(
                    cameraSwitchHandler,
                    cameraName
                )

                is CameraVideoCapturer -> (videoCapturer as CameraVideoCapturer).switchCamera(
                    cameraSwitchHandler,
                    cameraName
                )

                else -> Log.e(
                    "TogeVideoHandler",
                    "Unsupported video capturer type: ${videoCapturer.javaClass.simpleName}"
                )
            }
        } ?: run {
            Log.e("TogeVideoHandler", "Could not find suitable camera")
        }
    }

    fun release() {
        try {
            videoCapturer.dispose()
            surfaceTextureHelper.dispose()
        } catch (e: Exception) {
            Log.e("TogeVideoHandler", "Error releasing resources: ${e.message}")
        }
    }

    private fun getTargetCameraName(wantFrontFacing: Boolean): String? {
        return cameraEnumerator.deviceNames.firstOrNull { deviceName ->
            if (wantFrontFacing) {
                cameraEnumerator.isFrontFacing(deviceName)
            } else {
                cameraEnumerator.isBackFacing(deviceName)
            }
        }
    }

    private fun createVideoCapturer(isFrontCamera: Boolean): VideoCapturer {
        val deviceName = getTargetCameraName(isFrontCamera)
            ?: throw IllegalStateException("No ${if (isFrontCamera) "front" else "back"} camera available")

        return cameraEnumerator.createCapturer(deviceName, null)
            ?: throw IllegalStateException("Camera capturer creation failed")
    }

    private fun getOptimalResolution(isFrontCamera: Boolean): CameraEnumerationAndroid.CaptureFormat {
        val cameraId = getTargetCameraName(isFrontCamera)
            ?: throw IllegalStateException("Cannot find ${if (isFrontCamera) "front" else "back"} camera")

        val supportedFormats = cameraEnumerator.getSupportedFormats(cameraId)
            ?: emptyList()

        if (supportedFormats.isEmpty()) {
            throw IllegalStateException("No supported formats for camera: $cameraId")
        }

        for (resolution in PREFERRED_RESOLUTIONS) {
            supportedFormats.firstOrNull { format ->
                format.width == resolution || format.height == resolution
            }?.let { return it }
        }

        return supportedFormats.maxByOrNull { it.width * it.height }
            ?: supportedFormats.first() // 맞는거 없을 시 큰걸로 찾음
    }

    private val cameraSwitchHandler = object : CameraSwitchHandler {
        override fun onCameraSwitchDone(isFrontCamera: Boolean) {
            isUsingFrontCamera = isFrontCamera
        }

        override fun onCameraSwitchError(errorDescription: String) {
            Log.e("TogeVideoHandler", "Camera switch error: $errorDescription")
        }
    }

    companion object {
        private const val SURFACE_TEXTURE_THREAD_NAME = "WebRtcVideoHandlerSurfaceThread"
        private const val DEFAULT_FRAME_RATE = 30
        private val PREFERRED_RESOLUTIONS = listOf(1080, 720, 480, 360)
    }
}