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
package com.tari.android.wallet.ffi

import java.math.BigInteger

/**
 * Pending inbound transaction wrapper.
 *
 * @author The Tari Development Team
 */

internal typealias FFIPendingInboundTxPtr = Long

internal class FFIPendingInboundTx constructor(pointer: FFIPendingInboundTxPtr): FFIBase() {

    // region JNI

    private external fun jniGetId(ptr: FFIPendingInboundTxPtr, libError: FFIError): ByteArray
    private external fun jniGetSourcePublicKey(
        ptr: FFIPendingInboundTxPtr,
        libError: FFIError
    ): FFIPublicKeyPtr

    private external fun jniGetAmount(
        ptr: FFIPendingInboundTxPtr,
        libError: FFIError
    ): ByteArray

    private external fun jniGetTimestamp(
        ptr: FFIPendingInboundTxPtr,
        libError: FFIError
    ): ByteArray

    private external fun jniGetMessage(
        ptr: FFIPendingInboundTxPtr,
        libError: FFIError
    ): String

    private external fun jniDestroy(ptr: FFIPendingInboundTxPtr)

    // endregion

    private var ptr = nullptr

    init {
        ptr = pointer
    }

    fun getPointer(): FFIPendingInboundTxPtr {
        return ptr
    }

    fun getId(): BigInteger {
        val error = FFIError()
        val bytes = jniGetId(ptr, error)
        if (error.code != 0) {
            throw RuntimeException()
        }
        return BigInteger(1, bytes)
    }

    fun getSourcePublicKey(): FFIPublicKey {
        val error = FFIError()
        val result = FFIPublicKey(jniGetSourcePublicKey(ptr, error))
        if (error.code != 0) {
            throw RuntimeException()
        }
        return result
    }

    fun getAmount(): BigInteger {
        val error = FFIError()
        val bytes = jniGetAmount(ptr, error)
        if (error.code != 0) {
            throw RuntimeException()
        }
        return BigInteger(1, bytes)
    }

    fun getTimestamp(): BigInteger {
        val error = FFIError()
        val bytes = jniGetTimestamp(ptr, error)
        if (error.code != 0) {
            throw RuntimeException()
        }
        return BigInteger(1, bytes)
    }

    fun getMessage(): String {
        val error = FFIError()
        val result = jniGetMessage(ptr, error)
        if (error.code != 0) {
            throw RuntimeException()
        }
        return result
    }

    override fun destroy() {
        jniDestroy(ptr)
        ptr = nullptr
    }

}