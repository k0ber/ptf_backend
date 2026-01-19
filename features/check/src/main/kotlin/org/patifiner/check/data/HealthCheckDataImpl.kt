package org.patifiner.check.data

class CheckDataImpl(
    private val versionName: String,
    private val versionCode: String
) : CheckData {
    override fun getCheckStatus() = CheckResponse(
        response = "ptf",
        versionName = versionName,
        versionCode = versionCode
    )
}
