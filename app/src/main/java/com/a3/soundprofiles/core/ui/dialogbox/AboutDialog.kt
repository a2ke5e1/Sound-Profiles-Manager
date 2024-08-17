package com.a3.soundprofiles.core.ui.dialogbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.a3.soundprofiles.BuildConfig
import com.a3.soundprofiles.R
import com.a3.soundprofiles.databinding.DialogAboutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.IllegalStateException
import kotlin.let
import kotlin.text.trimIndent

data class Credits(val username: String, val language: String) {
  fun getClickableCredit(): SpannableString {
    val credit = SpannableString("@$username - $language")
    val link = "https://t.me/$username"
    val urlSpan = URLSpan(link)
    credit.setSpan(urlSpan, 0, credit.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return credit
  }
}

class AboutDialog : DialogFragment() {

  private val credits = listOf<Credits>()

  @SuppressLint("SetTextI18n")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let {
      val builder = MaterialAlertDialogBuilder(it)
      val inflater = requireActivity().layoutInflater
      val binding = DialogAboutBinding.inflate(inflater)

      binding.buildVersion.text =
          ContextCompat.getString(requireContext(), R.string.version) +
              " ${BuildConfig.VERSION_NAME}"
      val creditsText = SpannableStringBuilder("")
      for (credit in credits) {
        creditsText.append(credit.getClickableCredit())
        creditsText.append("\n")
      }
      binding.translationCredit.text = creditsText
      binding.translationCredit.movementMethod = LinkMovementMethod.getInstance()

      binding.telegramLink.setOnClickListener {
        val url = "https://t.me/phycalc"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
        this.dismiss()
      }
      binding.shareLink.setOnClickListener {
        shareApp(requireContext())
        this.dismiss()
      }
      binding.dismissButton.setOnClickListener { dismiss() }

      builder.setView(binding.root)
      builder.create()
    } ?: throw IllegalStateException("Activity cannot be null")
  }

  companion object {
     fun shareApp(context: Context) {
      // Launch share if users want to share app with their friends
      val intent = Intent(Intent.ACTION_SEND)
      intent.type = "text/plain"
      intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
      val shareMessage =
          """
            Check out ${context.getString(R.string.app_name)} (https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID} )
            Create and manage sound profiles on your Android device.
            """
              .trimIndent()
      intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
      context.startActivity(Intent.createChooser(intent, "Share"))
    }
  }
}
