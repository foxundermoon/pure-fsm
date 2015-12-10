package pure.fsm.example.kotlin.domain

import pure.fsm.core.StateMachine
import pure.fsm.core.Transition
import pure.fsm.example.kotlin.domain.TelcoEvent.*
import pure.fsm.example.kotlin.domain.TelcoState.*
import java.time.LocalDateTime

public val TIMEOUT_SECS = 1L

class TelcoStateMachine : StateMachine<TelcoEvent>() {

    init {

        `when`(InitialState, { last, event ->
            when (event) {
                is RechargeEvent -> go(RechargeRequestedState, event, last.context)
                is TimeoutTick -> checkTimeout(last)
                else -> defaultHandle(last, event)
            }
        })

        `when`(RechargeRequestedState, { last, event ->
            when (event) {
                is RechargeConfirmEvent -> go(RechargeConfirmedFinalState, event, last.context)
                is TimeoutTick -> checkTimeout(last)
                else -> defaultHandle(last, event)
            }
        })
    }

    private fun checkTimeout(last: Transition) = if (isTimeout(last)) go(TimeoutFinalState, TimeoutTick, last.context) else stay(last.state, TimeoutTick, last.context)

    private fun isTimeout(last: Transition) = LocalDateTime.now().isAfter(last.transitioned.plusSeconds(TIMEOUT_SECS))

    private fun defaultHandle(last: Transition, event: TelcoEvent) = go(ErrorFinalState, event, last.context)
}