package com.loanzzone.support.loanzzone_support_v2.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SupportRepository(
    private val baseUrl: String,
    private val gson: Gson = Gson(),
    private val client: OkHttpClient = defaultClient(),
) {

    fun fetchForTab(tab: SupportTab): Result<List<ContactRow>> = runCatching {
        when (tab.loanCategory) {
            null -> fetchBankSupportJson().map { it.toContactRow() }
            else -> fetchLoanPayoutJson(tab.loanCategory).map { it.toContactRow() }
        }
    }

    private fun fetchBankSupportJson(): List<BankSupportDto> {
        val url = "${trimBase()}/api/banksupport.php"
        val (code, body) = httpGet(url)
        requireHttpOk(code, body, "Banks support")
        val json = extractLeadingJson(body)
        return parseJsonArray(json, BankSupportDto::class.java)
    }

    private fun fetchLoanPayoutJson(category: String): List<LoanPayoutDto> {
        val enc = URLEncoder.encode(category, Charsets.UTF_8.name())
        // Prefer /api/ (same folder as banksupport.php). api-firebase often not deployed publicly.
        val paths = listOf(
            "/api/loan-payout-app.php?category=$enc",
            "/api/loan-payout.php?category=$enc",
            "/api/loan_payout.php?category=$enc",
            "/api-firebase/loan-payout-app.php?category=$enc",
        )
        val notFound = mutableListOf<String>()
        var lastDetail: String? = null
        for (path in paths) {
            val url = "${trimBase()}$path"
            val (code, body) = httpGet(url)
            when {
                code == 404 -> {
                    notFound.add(path)
                    lastDetail = "HTTP 404 for $path"
                    continue
                }
                code != 200 -> {
                    lastDetail = "HTTP $code for $path"
                    val err = parseErrorJsonMessage(body)
                    throw IllegalStateException(
                        err ?: "Loan API failed ($code). Deploy loan-payout API to server.",
                    )
                }
                else -> {
                    try {
                        return parseLoanPayoutBody(body)
                    } catch (e: Exception) {
                        lastDetail = e.message ?: e.javaClass.simpleName
                        continue
                    }
                }
            }
        }
        val base = trimBase()
        val hint = "Upload api/loan-payout-app.php (or loan-payout.php) to your server next to banksupport.php, then open in a browser: $base/api/loan-payout-app.php?category=personal_loan"
        throw IllegalStateException(
            if (notFound.size == paths.size) {
                "Loan API missing (404 on all URLs). $hint"
            } else {
                lastDetail ?: "Loan list could not be loaded. $hint"
            },
        )
    }

    /** Skips BOM, PHP notices, or HTML before the first `{` or `[`. */
    private fun extractLeadingJson(body: String): String {
        val t = body.trim()
        if (t.isEmpty()) return t
        val a = t.indexOf('[')
        val o = t.indexOf('{')
        val start = when {
            a < 0 && o < 0 -> 0
            a < 0 -> o
            o < 0 -> a
            else -> minOf(a, o)
        }
        return if (start > 0) t.substring(start) else t
    }

    /** Accepts raw JSON array or `{ "rows": [ ... ] }` (bootstrap-style). */
    private fun parseLoanPayoutBody(body: String): List<LoanPayoutDto> {
        val trimmed = extractLeadingJson(body).trim()
        if (trimmed.isEmpty() || trimmed == "null") return emptyList()
        if (trimmed.startsWith("<")) {
            throw IllegalStateException("Server returned HTML, not JSON (check API URL).")
        }
        if (trimmed.startsWith("{")) {
            val obj = gson.fromJson(trimmed, JsonObject::class.java)
            if (obj.has("success") && obj.get("success")?.asBoolean == false) {
                val msg = obj.get("message")?.asString ?: "Server error"
                throw IllegalStateException(msg)
            }
            if (obj.has("rows") && obj.get("rows").isJsonArray) {
                val arr = obj.getAsJsonArray("rows")
                val type = object : TypeToken<List<LoanPayoutDto>>() {}.type
                return gson.fromJson(arr, type) ?: emptyList()
            }
            throw IllegalStateException("Unexpected JSON from loan API (expected array or rows).")
        }
        return parseJsonArray(trimmed, LoanPayoutDto::class.java)
    }

    private fun <T> parseJsonArray(json: String, elementClass: Class<T>): List<T> {
        val listType = TypeToken.getParameterized(List::class.java, elementClass).type
        return gson.fromJson(json, listType) ?: emptyList()
    }

    private fun parseErrorJsonMessage(body: String): String? {
        val trimmed = body.trim()
        if (!trimmed.startsWith("{")) return null
        return try {
            val obj = gson.fromJson(trimmed, JsonObject::class.java)
            obj.get("message")?.asString
        } catch (_: Exception) {
            null
        }
    }

    private fun requireHttpOk(code: Int, body: String, label: String) {
        if (code == 200) return
        val msg = parseErrorJsonMessage(body)
        throw IllegalStateException(msg ?: "$label HTTP $code")
    }

    private fun httpGet(url: String): Pair<Int, String> {
        val req = Request.Builder()
            .url(url)
            .header("Accept", "application/json, text/plain, */*")
            .header("User-Agent", "LoanzzonesSupport/1.0 (Android)")
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            return resp.code to body
        }
    }

    private fun trimBase() = baseUrl.trimEnd('/')

    private fun BankSupportDto.toContactRow(): ContactRow {
        val name = bankname?.trim().orEmpty().ifEmpty { "—" }
        val mob = mobile?.trim().orEmpty()
        val digits = digitsForDial(mob)
        return ContactRow(
            stableId = "b-${id?.toLong() ?: name.hashCode()}",
            title = name,
            subtitle = mob,
            dialDigits = digits,
        )
    }

    private fun LoanPayoutDto.toContactRow(): ContactRow {
        val company = companyName?.trim().orEmpty()
        val bank = bankNbfc?.trim().orEmpty()
        val codeRaw = code?.trim().orEmpty()
        val dial = digitsForDial(codeRaw)
        val em = "—"
        val loanFields = LoanPayoutCardFields(
            bankNbfc = bank.ifEmpty { em },
            companyName = company.ifEmpty { em },
            code = codeRaw.ifEmpty { em },
        )
        return ContactRow(
            stableId = "l-${id?.toLong() ?: "${bank}|$company|$codeRaw".hashCode()}",
            title = bank.ifEmpty { company }.ifEmpty { codeRaw }.ifEmpty { em },
            subtitle = "",
            dialDigits = dial,
            loanFields = loanFields,
        )
    }

    private fun digitsForDial(raw: String): String? {
        if (raw.isEmpty()) return null
        val digits = buildString {
            for (c in raw) {
                if (c.isDigit()) append(c)
                if (c == '+' && isEmpty()) append(c)
            }
        }
        val onlyDigits = digits.replace("+", "")
        return if (onlyDigits.length >= 10) digits else null
    }

    companion object {
        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }
}
