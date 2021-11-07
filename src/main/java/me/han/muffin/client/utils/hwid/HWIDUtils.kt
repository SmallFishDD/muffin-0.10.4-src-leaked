package me.han.muffin.client.utils.hwid

import java.io.IOException
import java.security.MessageDigest
import kotlin.experimental.and

object HWIDUtils {

    val systemStuff =
        System.getenv("os") +
            System.getProperty("os.name") +
            System.getProperty("os.arch") +
            System.getProperty("os.version") +
            System.getProperty("user.language") +
            System.getenv("SystemRoot") +
            System.getenv("HOMEDRIVE") +
            System.getenv("PROCESSOR_LEVEL") +
            System.getenv("PROCESSOR_REVISION") +
            System.getenv("PROCESSOR_IDENTIFIER") +
            System.getenv("PROCESSOR_ARCHITECTURE") +
            System.getenv("PROCESSOR_ARCHITEW6432") +
            System.getenv("NUMBER_OF_PROCESSORS")

    fun getFinalHwid(): String {
        val cpu = WindowsHardwareAbstractionLayer.getProcessors()
        var str = ""
        for (processor in cpu) {
            str +=
                processor.identifier +
                    processor.name +
                    processor.vendor +
                    processor.vendorFrequency +
                    processor.CPUFamily +
                    processor.CPUModel +
                    processor.CPUStepping +
                    processor.isCpu64Bit
        }

        //val array = arrayOf(System.getenv("PROCESSOR_IDENTIFIER"), System.getenv("NUMBER_OF_PROCESSORS"), systemStuff, "notlikethis")

        val array = arrayOf(
            str,
            System.getenv("COMPUTERNAME"),
            System.getProperty("os.name").trim(),
            System.getProperty("os.arch").trim(),
            System.getProperty("os.version").trim(),
            System.getProperty("user.name").trim(),
            WindowsHardwareAbstractionLayer.getMachineGuid(),
            WindowsHardwareAbstractionLayer.getMotherboardDetails(),
            //   WindowsHardwareAbstractionLayer.getSystemBiosVersion(),
            //   WindowsHardwareAbstractionLayer.getMACStuff(),
            WindowsHardwareAbstractionLayer.getSoundCards()
        )

        val sb = StringBuilder()
        for (s in array) {
            sb.append(s)
            if (sb.toString().contains("VMware")) {
                try {
                    val shutdownMethod = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", Integer.TYPE)
                    shutdownMethod.isAccessible = true
                    shutdownMethod.invoke(null, 0)
                } catch (e: Exception) {
                    throw NullPointerException("Failed to load.").also { it.stackTrace = arrayOf() }
                }
                throw IOException().also { it.stackTrace = arrayOf() }
            }
        }
        //    return "INFO: Updated user han hwid from empty to "+ sb.toString()

        val digest = MessageDigest.getInstance("MD5")
        digest.update(sb.toString().toByteArray())
        val bytes = digest.digest()
        val sb2 = StringBuilder()
        for (b in bytes) sb2.append(String.format("%02x", b and 0xff.toByte()))
        return sb2.toString()
    }

}