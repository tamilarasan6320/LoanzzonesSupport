package com.loanzzone.support.loanzzone_support_v2.data

import com.google.gson.annotations.SerializedName

data class BankSupportDto(
    @SerializedName("id") val id: Number? = null,
    @SerializedName("bankname") val bankname: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
)

data class LoanPayoutDto(
    @SerializedName("id") val id: Number? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("bank_nbfc") val bankNbfc: String? = null,
    @SerializedName("company_name") val companyName: String? = null,
    @SerializedName("code") val code: String? = null,
)

/** Loan payout row: shows three labeled lines in the card (Banks Support uses [title]/[subtitle] only). */
data class LoanPayoutCardFields(
    val bankNbfc: String,
    val companyName: String,
    val code: String,
)

data class ContactRow(
    val stableId: String,
    val title: String,
    val subtitle: String,
    /** Digits suitable for ACTION_DIAL, or null when no phone */
    val dialDigits: String?,
    val loanFields: LoanPayoutCardFields? = null,
)

enum class SupportTab(
    val label: String,
    /** `null` = banks support API; otherwise loan-payout category */
    val loanCategory: String?,
) {
    BanksSupport("Banks Support", null),
    PersonalLoan("Personal Loan", "personal_loan"),
    BusinessLoan("Business loan", "business_loan"),
    ClientHomeLoan("Home Loan", "client_home_loan"),
    ClientLap("LAP", "client_lap"),
}
