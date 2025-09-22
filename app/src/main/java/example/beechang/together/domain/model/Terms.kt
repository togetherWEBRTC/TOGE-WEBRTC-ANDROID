package example.beechang.together.domain.model

enum class ReportReason(val code: String, val displayName: String) {
    IMPERSONATION("impersonation", "사칭, 신분도용 등"),
    INAPPROPRIATE_CONTENT("inappropriate_content", "음란물, 욕설, 협박, 명예훼손 등"),
    COPYRIGHT_VIOLATION("copyright_violation", "저작권 또는 상표 침해"),
    SPAM_COMMERCIAL("spam_commercial", "홍보, 스팸, 영리목적 등"),
    PERSONAL_INFO("personal_info", "개인 정보 노출"),
    OTHER("other", "기타");

    fun toCategoryFormat(): String {
        return "$code:$displayName"
    }

    companion object {
        fun all(): List<ReportReason> = entries
        fun displayNames(): List<String> = entries.map { it.displayName }
        fun fromDisplayName(name: String): ReportReason? = entries.find { it.displayName == name }
        fun fromIndex(index: Int): ReportReason? = entries.getOrNull(index)
    }
}

enum class ReportType {
    CALL , CHAT , PROFILE
}

enum class InquiryCategory(val code: String, val displayName: String) {
    TECHNICAL("TECHNICAL", "기술문의"),
    ACCOUNT("ACCOUNT", "계정문의"),
//    PAYMENT("PAYMENT", "결제문의"),
    BUG_REPORT("BUG_REPORT", "버그신고"),
    FEATURE_REQUEST("FEATURE_REQUEST", "기능요청"),
    OTHER("OTHER", "기타");

    companion object Companion {
        fun all(): List<InquiryCategory> = entries
        fun displayNames(): List<String> = entries.map { it.displayName }
        fun fromDisplayName(name: String): InquiryCategory? = entries.find { it.displayName == name }
        fun fromCode(code: String): InquiryCategory? = entries.find { it.code == code }
    }
}