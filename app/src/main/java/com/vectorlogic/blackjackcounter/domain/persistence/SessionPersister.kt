package com.vectorlogic.blackjackcounter.domain.persistence

import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState

interface SessionPersister {
    fun load(): CounterSessionState?
    fun save(state: CounterSessionState)
}
