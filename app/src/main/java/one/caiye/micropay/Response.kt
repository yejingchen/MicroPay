package one.caiye.micropay

data class MPResponse(
        val status: String,
        val payload: Boolean
)

data class ErrorResponse(
        val status: String,
        val message: String
)

data class AccountInfo(
        val balance: Double
)

data class AccountResponse(
        val status: String,
        val payload: AccountInfo
)

data class Transaction(
        val balance: Double
)

data class TransactionResp(
        val status: String,
        val payload: Transaction
)

data class Record(
        val username_from: String,
        val username_to: String,
        val amount: Double,
        val time: String
)

data class RecordsResponse(
        val status: String,
        val payload: ArrayList<Record>
)