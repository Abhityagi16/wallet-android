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

import java.util.*

internal typealias FFIPrivateKeyPtr = Long

/**
 * Wrapper for native private key type.
 *
 * @author The Tari Development Team
 */
internal class FFIPrivateKey constructor(pointer: FFIPrivateKeyPtr): FFIBase() {

    // region JNI

    private external fun jniGetBytes(
        privateKeyPtr: FFIPrivateKeyPtr,
        libError: FFIError
    ): FFIByteVectorPtr

    private external fun jniDestroy(privateKeyPtr: FFIPrivateKeyPtr)
    private external fun jniCreate(byteVectorPtr: FFIByteVectorPtr, libError: FFIError): FFIPrivateKeyPtr
    private external fun jniGenerate(): FFIPrivateKeyPtr
    private external fun jniFromHex(hexStr: String, libError: FFIError): FFIPrivateKeyPtr

    // endregion

    private var ptr = nullptr

    init {
        ptr = pointer
    }

    constructor() : this(nullptr) {
        ptr = jniGenerate()
    }

    constructor(byteVector: FFIByteVector) : this(nullptr) {
        val error = FFIError()
        ptr = jniCreate(byteVector.getPointer(), error)
        if (error.code != 0) {
            throw RuntimeException()
        }
    }

    constructor(hexString: HexString) : this(nullptr) {
        if (hexString.toString().length == 64) {
            val error = FFIError()
            ptr = jniFromHex(hexString.hex, error)
            if (error.code != 0) {
                throw RuntimeException()
            }
        } else {
            throw InvalidPropertiesFormatException("HexString is not a valid PrivateKey")
        }
    }

    fun getPointer(): FFIPrivateKeyPtr {
        return ptr
    }

    fun getBytes(): FFIByteVector {
        val error = FFIError()
        val result = FFIByteVector(jniGetBytes(ptr, error))
        if (error.code != 0) {
            throw RuntimeException()
        }
        return result
    }

    override fun toString(): String {
        val error = FFIError()
        val result = FFIByteVector(jniGetBytes(ptr, error)).toString()
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
