package com.allergytrack.app.platform

import android.content.Context
import android.os.Build
import com.allergytrack.app.ui.util.AppLocaleHelper
import com.allergytrack.shared.platform.DeviceInfo

/**
 * Přepínání jazyka aplikace. Na API 33+ přes LocaleManager (ten si restart
 * řeší sám), níž přes SharedPreferences + createConfigurationContext,
 * kde restart musí spustit volající.
 */
class AndroidDeviceInfo(
    private val context: Context
) : DeviceInfo {

    override fun currentLanguage(): String = AppLocaleHelper.getLanguage(context)

    override fun setLanguage(code: String) = AppLocaleHelper.setLanguage(context, code)

    override fun requiresRestartOnLanguageChange(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
}
