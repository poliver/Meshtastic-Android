package com.geeksville.mesh.service

import com.geeksville.mesh.android.BuildUtils.debug
import com.geeksville.mesh.android.prefs.MeshPrefs
import com.geeksville.mesh.concurrent.handledLaunch
import com.geeksville.mesh.model.NO_DEVICE_SELECTED
import com.geeksville.mesh.repository.datastore.RadioConfigRepository
import com.geeksville.mesh.util.anonymize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LastAddressUpdater @Inject constructor(
    private val meshPrefs: MeshPrefs,
    private val radioConfigRepository: RadioConfigRepository,
    private val serviceNotifications: MeshServiceNotifications
) {

    private val lastAddress: MutableStateFlow<String?> =
        MutableStateFlow(meshPrefs.deviceAddress ?: NO_DEVICE_SELECTED)


    fun updateLastAddress(deviceAddr: String?, serviceScope: CoroutineScope) {
        debug("setDeviceAddress: Passing through device change to radio service: ${deviceAddr.anonymize}")
        when (deviceAddr) {
            null,
            "",
                -> {
                debug("SetDeviceAddress: No previous device address, setting new one")
                lastAddress.value = deviceAddr
                meshPrefs.deviceAddress = deviceAddr
            }

            lastAddress.value,
            NO_DEVICE_SELECTED,
                -> {
                debug("SetDeviceAddress: Device address is the none or same, ignoring")
            }

            else -> {
                debug("SetDeviceAddress: Device address changed from $lastAddress to $deviceAddr")
                lastAddress.value = deviceAddr
                meshPrefs.deviceAddress = deviceAddr
                serviceScope.handledLaunch {
                    debug("Clearing nodeDB")
                    radioConfigRepository.clearNodeDB()
                }
                serviceNotifications.clearNotifications()
            }
        }
    }
}