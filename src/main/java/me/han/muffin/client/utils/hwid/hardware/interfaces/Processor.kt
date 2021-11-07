package me.han.muffin.client.utils.hwid.hardware.interfaces

interface Processor {

    var vendor: String?
    var name: String?
    var identifier: String?
    var CPUFamily: String?
    var CPUModel: String?
    var CPUStepping: String?
    var vendorFrequency: Long?
    var isCpu64Bit: Boolean?
    val load: Float

}