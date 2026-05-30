package ztf.extend

object ContextLinkBuilder {

    sealed class Result {
        data class Success(val reference: String) : Result()
        data class Failure(val messageKey: String) : Result()
    }

    fun buildContextLink(filePath: String, startLine: Int, endLine: Int, format: String): Result {
        if (filePath.isBlank()) return Result.Failure("notification.cannotGetFilePath")
        if (startLine < 0) return Result.Success("@$filePath")
        val lineRef = if (startLine == endLine) "$startLine" else "$startLine-$endLine"
        return if (format == "opencode") Result.Success("@$filePath#$lineRef")
        else Result.Success("@$filePath#L$lineRef")
    }
}
