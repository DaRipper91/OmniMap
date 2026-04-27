package com.omnimap.core.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DesktopConnectivityObserver : ConnectivityObserver {
    override fun observe(): Flow<ConnectivityObserver.Status> {
        return flowOf(ConnectivityObserver.Status.Available)
    }
}
