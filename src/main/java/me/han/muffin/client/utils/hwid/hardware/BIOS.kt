package me.han.muffin.client.utils.hwid.hardware

import me.han.muffin.client.utils.hwid.hardware.interfaces.Motherboard

class BIOS: Motherboard {

    override var manufacturer: String? = null
    override var product: String? = null
    override var BIOSReleaseDate: String? = null
    override var BIOSVendor: String? = null
    override var BIOSVersion: String? = null

}