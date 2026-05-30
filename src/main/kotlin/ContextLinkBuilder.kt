/*
 * Copyright 2026 ztf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
