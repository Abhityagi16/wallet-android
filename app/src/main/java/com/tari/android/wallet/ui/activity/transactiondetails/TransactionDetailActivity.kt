package com.tari.android.wallet.ui.activity.transactiondetails

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.Group
import butterknife.BindView
import butterknife.OnClick
import com.tari.android.wallet.R
import com.tari.android.wallet.model.CompletedTx
import com.tari.android.wallet.model.Contact
import com.tari.android.wallet.model.Tx
import com.tari.android.wallet.ui.activity.BaseActivity
import com.tari.android.wallet.ui.component.CustomFontEditText
import com.tari.android.wallet.ui.component.CustomFontTextView
import com.tari.android.wallet.ui.component.EmojiIdSummaryViewController
import com.tari.android.wallet.util.EmojiUtil
import java.math.BigInteger

class TransactionDetailActivity : BaseActivity() {

    override val contentViewId: Int = R.layout.activity_transaction_detail
    companion object {
        val TRANSACTION_EXTRA_KEY = "TRANSACTION_EXTRA_KEY"

        fun createIntent(context: Context, transaction: Tx): Intent{
            return Intent(context, TransactionDetailActivity::class.java)
                .apply {
                    putExtra(TRANSACTION_EXTRA_KEY, transaction)
                }
        }
    }

    @BindView(R.id.tx_detail_payment_state)
    lateinit var transactionTypeTv: CustomFontTextView
    @BindView(R.id.tx_detail_back)
    lateinit var backBtn: ImageView
    @BindView(R.id.tx_detail_date)
    lateinit var transactionDateTv: CustomFontTextView
    @BindView(R.id.transaction_detail_amount)
    lateinit var transactionAmountTv: CustomFontTextView
    @BindView(R.id.tx_detail_tx_fee)
    lateinit var transactionFeeTv: CustomFontTextView

    @BindView(R.id.tx_detail_add_contact)
    lateinit var addContactBtn: CustomFontTextView

    @BindView(R.id.transaction_detail_contact_name)
    lateinit var contactNameTv: CustomFontTextView

    @BindView(R.id.transaction_detail_create_contact)
    lateinit var createContactEt: CustomFontEditText
    @BindView(R.id.contact_label)
    lateinit var contactLabel: CustomFontTextView

    @BindView(R.id.tx_detail_edit_name)
    lateinit var editContactBtn: CustomFontTextView

    @BindView(R.id.transaction_detail_note)
    lateinit var transactionNoteTv: CustomFontTextView

    @BindView(R.id.tx_detail_tx_id)
    lateinit var transactionIdTv: CustomFontTextView

    @BindView(R.id.transaction_detail_emoji_summary)
    lateinit var emojiSummaryView: View
    @BindView(R.id.transaction_detail_separator)
    lateinit var contactSeparator: View

    @BindView(R.id.tx_detail_tx_fee_group)
    lateinit var transactionFeeGroup: Group

    private var transaction: Tx? = null
    private lateinit var emojiIdSummaryController: EmojiIdSummaryViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        emojiIdSummaryController = EmojiIdSummaryViewController(emojiSummaryView)
        transaction = intent.getParcelableExtra(TRANSACTION_EXTRA_KEY)
        if (transaction == null) finish()
        setupUI()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        when(transaction!!.direction) {
            Tx.Direction.INBOUND -> {
                transactionTypeTv.text = getString(R.string.payment_received)
            }
            Tx.Direction.OUTBOUND -> {
                transactionTypeTv.text = getString(R.string.payment_sent)
            }
        }
        transactionAmountTv.text = "%1$,.2f".format(transaction!!.amount.tariValue.toDouble())

        val emojiId = EmojiUtil.getEmojiIdForPublicKeyHexString(transaction!!.user.publicKeyHexString)
        emojiIdSummaryController.display(emojiId)
        transactionIdTv.text = "${getString(R.string.transaction_id)}:${transaction!!.id}"
        transactionNoteTv.text = transaction!!.message
        backBtn.setOnClickListener { onBackPressed() }
        val user = transaction!!.user
        if (user is Contact) {
            contactNameTv.visibility = View.VISIBLE
            contactNameTv.text = user.alias
            contactLabel.visibility = View.VISIBLE
            addContactBtn.visibility = View.INVISIBLE
            contactSeparator.visibility = View.VISIBLE
            editContactBtn.visibility = View.VISIBLE

        } else {
            addContactBtn.visibility = View.VISIBLE
        }
        if (transaction is CompletedTx) {
            transactionFeeGroup.visibility = View.VISIBLE
            transactionFeeTv.text = "${(transaction as CompletedTx).calculateFee()}"
        } else {
            transactionFeeGroup.visibility = View.GONE
        }
    }

    @OnClick(R.id.tx_detail_add_contact)
    fun addContact() {
        addContactBtn.visibility = View.INVISIBLE
        editContactBtn.visibility = View.VISIBLE
        createContactEt.visibility = View.VISIBLE
        contactLabel.visibility = View.VISIBLE
        contactSeparator.visibility = View.VISIBLE
    }
}
