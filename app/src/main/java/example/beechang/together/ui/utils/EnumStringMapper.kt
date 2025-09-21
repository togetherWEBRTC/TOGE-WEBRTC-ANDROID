package example.beechang.together.ui.utils

import android.content.Context
import example.beechang.together.R
import example.beechang.together.domain.model.ReportReason
import example.beechang.together.domain.model.ReportReasonCategory

fun ReportReason.getUiString(context: Context): String {
    return when (this) {
        ReportReason.IMPERSONATION -> context.getString(R.string.report_reason_impersonation)
        ReportReason.INAPPROPRIATE_CONTENT -> context.getString(R.string.report_reason_inappropriate_content)
        ReportReason.COPYRIGHT_VIOLATION -> context.getString(R.string.report_reason_copyright_violation)
        ReportReason.SPAM_COMMERCIAL -> context.getString(R.string.report_reason_spam_commercial)
        ReportReason.PERSONAL_INFO -> context.getString(R.string.report_reason_personal_info)
        ReportReason.OTHER -> context.getString(R.string.report_reason_other)
    }
}
