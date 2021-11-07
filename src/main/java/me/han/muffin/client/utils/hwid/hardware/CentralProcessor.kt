package me.han.muffin.client.utils.hwid.hardware

import me.han.muffin.client.utils.hwid.hardware.interfaces.Processor

class CentralProcessor: Processor {

    override var vendor: String? = null
    override var name: String? = null
    override var identifier: String? = null
    override var vendorFrequency: Long? = null

    override var isCpu64Bit: Boolean? = null

    override var CPUFamily: String? = null
    override var CPUModel: String? = null
    override var CPUStepping: String? = null

    override val load: Float = throw UnsupportedOperationException()

    override fun toString(): String {
        return name ?: super.toString()
    }

}