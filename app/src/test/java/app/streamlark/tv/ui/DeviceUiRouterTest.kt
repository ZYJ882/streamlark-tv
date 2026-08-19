package app.streamlark.tv.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceUiRouterTest {

    @Test
    fun `leanback devices use television shell`() {
        assertEquals(DeviceUiMode.TELEVISION, DeviceUiRouter.resolve(hasLeanback = true, hasTouchscreen = true))
    }

    @Test
    fun `devices without touchscreen use television shell`() {
        assertEquals(DeviceUiMode.TELEVISION, DeviceUiRouter.resolve(hasLeanback = false, hasTouchscreen = false))
    }

    @Test
    fun `ordinary touchscreen devices use mobile shell`() {
        assertEquals(DeviceUiMode.MOBILE, DeviceUiRouter.resolve(hasLeanback = false, hasTouchscreen = true))
    }

    @Test
    fun `user override takes precedence over capabilities`() {
        assertEquals(
            DeviceUiMode.MOBILE,
            DeviceUiRouter.resolve(hasLeanback = true, hasTouchscreen = true, override = DeviceUiMode.MOBILE.name)
        )
    }
}
