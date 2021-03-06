package pure.fsm.core.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pure.fsm.core.Transition;
import pure.fsm.core.context.ContextMessage;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ContextHistoryFormatter implements OnCleanupListener {

    private final static Logger LOG = LoggerFactory.getLogger(ContextHistoryFormatter.class);

    public static final ContextHistoryFormatter HISTORY_FORMATTER = new ContextHistoryFormatter();

    public String toTransitionString(Transition transition) {

        return format("\n\n+++++ State Machine Transition history stateMachineId [%s] +++++ =>",
                transition.getContext().stateMachineId()) +
                toCustomTransitionLog(transition) +
                "\n" + toContextString(transition, calcNumTransitions(transition, 1));
    }

    protected String toCustomTransitionLog(Transition transition) {
        return "";
    }

    private int calcNumTransitions(Transition transition, int count) {

        return transition.previous().isPresent() ? calcNumTransitions(transition.previous().get(), count + 1) : count;
    }

    private String toContextString(Transition lastTransition, int indent) {
        StringBuilder sb = new StringBuilder();

        lastTransition.previous().ifPresent(prev -> sb.append(toContextString(prev, indent - 1)));

        final List<ContextMessage> msg = lastTransition.getContext().getContextsOfType(ContextMessage.class);

        sb.append(format("%" + indent + "s", " ")).append(format("event[%s] ==> State[%s], Transitioned[%s], msg[%s]",
                lastTransition.getEvent(),
                lastTransition.getState().getClass().getName(),
                lastTransition.getTransitioned(),
                msg.stream().map(m -> m.message).collect(Collectors.joining(":"))));

        return sb.append("\n").toString();
    }

    @Override
    public void onCleanup(Transition transition) {
        LOG.info(toTransitionString(transition));
    }
}
