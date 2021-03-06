/**
 * Copyright 2020 The Tari Project
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of
 * its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tari.android.wallet.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import butterknife.BindView
import com.airbnb.lottie.LottieAnimationView
import com.daasuu.ei.Ease
import com.daasuu.ei.EasingInterpolator
import com.orhanobut.logger.Logger
import com.tari.android.wallet.R
import com.tari.android.wallet.auth.AuthUtil
import com.tari.android.wallet.ui.activity.home.HomeActivity
import com.tari.android.wallet.util.Constants
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

/**
 * Initial activity class - authenticates the user.
 *
 * @author The Tari Development Team
 */
class AuthActivity : BaseActivity(), Animator.AnimatorListener {

    private lateinit var biometricPrompt: BiometricPrompt
    @BindView(R.id.main_img_big_gem)
    lateinit var bigGemImageView: ImageView
    @BindView(R.id.main_anim_tari)
    lateinit var tariAnimationView: LottieAnimationView
    @BindView(R.id.main_txt_testnet)
    lateinit var testnetTextView: TextView
    @BindView(R.id.main_img_small_gem)
    lateinit var smallGemImageView: ImageView

    override val contentViewId = R.layout.activity_auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        // call the animations
        val wr = WeakReference<AuthActivity>(this)
        bigGemImageView.post { wr.get()?.showTariText() }
    }

    /**
     * Hides the gem and displays Tari logo.
     */
    private fun showTariText() {
        // hide features to be shown after animation
        tariAnimationView.alpha = 0f
        testnetTextView.alpha = 0f
        smallGemImageView.alpha = 0f

        // define animations
        val hideGemAnim = ValueAnimator.ofFloat(1f, 0f)
        val showTariTextAnim = ValueAnimator.ofFloat(0f, 1f)
        val weakReference: WeakReference<AuthActivity> = WeakReference(this)
        hideGemAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            weakReference.get()?.bigGemImageView?.alpha = alpha
        }
        showTariTextAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            weakReference.get()?.tariAnimationView?.alpha = alpha
            weakReference.get()?.testnetTextView?.alpha = alpha
            weakReference.get()?.smallGemImageView?.alpha = alpha
        }

        // chain animations
        val animSet = AnimatorSet()
        animSet.startDelay = Constants.UI.shortAnimDurationMs
        animSet.play(showTariTextAnim).after(hideGemAnim)
        animSet.duration = Constants.UI.shortAnimDurationMs
        // define interpolator
        animSet.interpolator = EasingInterpolator(Ease.QUART_IN)

        // authenticate at the end of the animation set
        val wr = WeakReference<AuthActivity>(this)
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                wr.get()?.doAuth()
            }
        })

        // start the animation set
        animSet.start()
    }

    /**
     * Calls Android authentication helper - does biometric if exists, falls back
     * on passcode if not.
     */
    private fun doAuth() {
        val wr = WeakReference<AuthActivity>(this)

        // check whether there's at least screen lock
        if (!AuthUtil.isDeviceSecured(this)) {
            // local authentication not available
            tariAnimationView.post {
                wr.get()?.displayAuthNotAvailableDialog()
            }
            return
        }

        // display authentication dialog
        val executor = Executors.newSingleThreadExecutor()
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED -> wr.get()?.authHasFailed()
                        BiometricPrompt.ERROR_CANCELED -> wr.get()?.authHasFailed()
                        else -> {
                            Logger.e("Other biometric error. Code: %d", errorCode)
                            wr.get()?.authHasFailed()
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    wr.get()?.authSuccessful()
                }

            })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_title))
            .setSubtitle(getString(R.string.auth_subtitle))
            .setDescription(getString(R.string.auth_description))
            .setDeviceCredentialAllowed(true) // enable passcode (i.e. screenlock)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Auth was successful.
     */
    private fun authSuccessful() {
        val wr = WeakReference<AuthActivity>(this)
        wr.get()?.tariAnimationView?.post {
            wr.get()?.playTariWalletAnim()
        }
    }

    /**
     * Auth has failed.
     */
    private fun authHasFailed() {
        Logger.e("Authentication other error.")
        val wr = WeakReference<AuthActivity>(this)
        runOnUiThread { wr.get()?.displayAuthFailedDialog() }
    }

    /**
     * Auth not available on device, i.e. lock screen is disabled
     */
    private fun displayAuthNotAvailableDialog() {
        val wr = WeakReference<AuthActivity>(this)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(getString(R.string.auth_not_available_or_canceled_desc))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.proceed)) { dialog, _ ->
                dialog.cancel()
                wr.get()?.tariAnimationView?.post {
                    wr.get()?.playTariWalletAnim()
                }
            }
            // negative button text and action
            .setNegativeButton(getString(R.string.exit)) { _, _ ->
                finish()
            }

        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.auth_not_available_or_canceled_title))
        alert.show()
    }

    private fun displayAuthFailedDialog() {
        biometricPrompt.cancelAuthentication()

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(getString(R.string.auth_failed_desc))
            .setCancelable(false)
            // negative button text and action
            .setNegativeButton(getString(R.string.exit)) { _, _ ->
                finish()
            }

        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.auth_failed_title))
        alert.show()
    }

    /**
     * Plays Tari Wallet text anim.
     */
    private fun playTariWalletAnim() {
        tariAnimationView.addAnimatorListener(this)
        tariAnimationView.playAnimation()
    }

    //region Animator Listener
    override fun onAnimationStart(animation: Animator?) {
        // no-op
    }

    override fun onAnimationRepeat(animation: Animator?) {
        // no-op
    }

    override fun onAnimationCancel(animation: Animator?) {
        // no-op
    }

    override fun onAnimationEnd(animation: Animator?) {
        // go to home activity
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        // finish this activity
        finish()
    }
    //endregion Animator Listener

}