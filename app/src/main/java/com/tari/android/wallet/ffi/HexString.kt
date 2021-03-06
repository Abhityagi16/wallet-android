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
import java.util.*

/**
 * @author The Tari Development Team
 */
internal class HexString constructor(bytes: FFIByteVector) {

    private val pattern = "\\p{XDigit}+".toRegex()

    var hex: String = String()

    init {
        if (bytes.getPointer() != nullptr) {
            val byteArray = ByteArray(bytes.getLength())
            if (bytes.getLength() > 0) {
                for (i in 0 until bytes.getLength()) {
                    val m = bytes.getAt(i)
                    byteArray[i] = m.toByte()
                }
                hex = BigInteger(1, byteArray) //
                    .toString(16) //a-f,0-9
                    .toUpperCase(Locale.getDefault()) //A-F are in lowercase in the final string before this call
            } else {
                hex = String()
            }
        } else {
            hex = String()
        }
    }

    constructor(string: String) : this(FFIByteVector(nullptr)) {
        if (pattern.matches(string) && string.length % 2 == 0) {
            hex = string
        } else {
            throw InvalidPropertiesFormatException("String is not valid Hex of even length")
        }
    }

    override fun toString(): String {
        return hex
    }


}