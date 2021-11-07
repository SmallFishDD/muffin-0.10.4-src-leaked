package me.han.muffin.client.utils.hwid

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.Win32Exception
import com.sun.jna.platform.win32.WinReg
import me.han.muffin.client.utils.hwid.hardware.BIOS
import me.han.muffin.client.utils.hwid.hardware.CentralProcessor
import me.han.muffin.client.utils.hwid.hardware.interfaces.Processor
import java.net.NetworkInterface
import java.util.regex.Pattern

object WindowsHardwareAbstractionLayer {

    private const val REGISTRY_SOUNDCARDS = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e96c-e325-11ce-bfc1-08002be10318}\\"
    /** Constant `whitespaces`  */
    val whitespaces: Pattern = Pattern.compile("\\s+")

    fun getProcessors(): Array<Processor> {
        var cpu64bit = false
        val cpuRegistryRoot = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\"
        val processors = ArrayList<Processor>()
        val processorIds = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryRoot)

        if (processorIds.isNotEmpty()) {
            val cpuRegistryPath = "$cpuRegistryRoot${processorIds[0]}"

            val cpu = CentralProcessor()

            cpu.vendor = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "VendorIdentifier")
            cpu.name = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "ProcessorNameString")
            cpu.identifier = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "Identifier")

            try {
                cpu.vendorFrequency = (Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "~MHz") * 1000000L)
            } catch (e: Win32Exception) {
                // Leave as 0, parse the identifier as backup
            }

            if (cpu.identifier!!.isNotEmpty()) {
                cpu.CPUFamily = parseIdentifier(cpu.identifier!!, "Family")
                cpu.CPUModel = parseIdentifier(cpu.identifier!!, "Model")
                cpu.CPUStepping = parseIdentifier(cpu.identifier!!, "Stepping")
            }

            /*
            val systemInfo = WinBase.SYSTEM_INFO()
            Kernel32.INSTANCE.GetNativeSystemInfo(systemInfo)
            val processorArchitecture = systemInfo.processorArchitecture.pi.wProcessorArchitecture.toInt()
            if (processorArchitecture == 9 // PROCESSOR_ARCHITECTURE_AMD64
                || processorArchitecture == 12 // PROCESSOR_ARCHITECTURE_ARM64
                || processorArchitecture == 6) { // PROCESSOR_ARCHITECTURE_IA64
                cpu64bit = true
            }
             */

            cpu.isCpu64Bit = true
            processors.add(cpu)
        }

        return processors.toTypedArray()
    }

    fun getMachineGuid(): String {
        val machineRegistryRoot = "SOFTWARE\\Microsoft\\Cryptography\\"
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, machineRegistryRoot, "MachineGuid")
    }

    fun getMotherboardDetails(): String {
        val biosRegistryRoot = "HARDWARE\\DESCRIPTION\\System\\BIOS\\"

        val bios = BIOS().apply {
            manufacturer = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, biosRegistryRoot, "BaseBoardManufacturer")
            product = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, biosRegistryRoot, "BaseBoardProduct")
            BIOSReleaseDate = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, biosRegistryRoot, "BIOSReleaseDate")
            BIOSVendor = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, biosRegistryRoot, "BIOSVendor")
            BIOSVersion = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, biosRegistryRoot, "BIOSVersion")
        }

        return StringBuilder().run {
            append(bios.manufacturer)
            append(bios.product)
            append(bios.BIOSReleaseDate)
            append(bios.BIOSVendor)
            append(bios.BIOSVersion)
            toString()
        }
    }

    fun getSystemBiosVersion() = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "HARDWARE\\DESCRIPTION\\System\\", "SystemBiosVersion")

    fun getSoundCards(): String {
        val keys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, REGISTRY_SOUNDCARDS)
        var soundcard = ""

        for (key in keys) {
            val fullKey = REGISTRY_SOUNDCARDS + key

            try {
                if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, "Driver")) {
                    soundcard = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "Driver") + " " +
                        Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "DriverVersion") + " " +
                        Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "ProviderName") + " " +
                        Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "DriverDesc")
                }
            } catch (e: Win32Exception) {
            }
        }

        return soundcard
    }

    fun getMACStuff(): String {
        var mac = ""
        val nis = NetworkInterface.getNetworkInterfaces()
        while (nis.hasMoreElements()) {
            val ni = nis.nextElement()
            mac += ni.name + " " + ni.displayName
        }
        return mac
    }

    private fun parseIdentifier(identifier: String, key: String): String {
        val idSplit = whitespaces.split(identifier)
        var found = false
        for (s in idSplit) {
            // If key string found, return next value
            if (found) {
                return s
            }
            found = s == key
        }
        // If key string not found, return empty string
        return ""
    }

}