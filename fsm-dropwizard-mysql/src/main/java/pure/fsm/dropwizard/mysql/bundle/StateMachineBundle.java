package pure.fsm.dropwizard.mysql.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import pure.fsm.core.StateFactoryRegistration;
import pure.fsm.core.cleanup.CleanUpFinalisedStateMachines;
import pure.fsm.core.cleanup.OnCleanupListener;
import pure.fsm.core.StateMachineRepository;
import pure.fsm.core.state.StateFactory;
import pure.fsm.core.WithinLock;
import pure.fsm.core.EventTicker;
import pure.fsm.core.transition.TransitionOccuredListener;
import pure.fsm.repository.mysql.MysqlStateMachineRepository;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;

public abstract class StateMachineBundle implements ConfiguredBundle<PureFsmMysqlConfig> {

    private StateMachineRepository repository;
    private WithinLock template;
    private DBI dbi;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(PureFsmMysqlConfig configuration, Environment environment) {

        DBIFactory dbiFactory = new DBIFactory();

        dbi = dbiFactory.build(environment, configuration.getPureFsmDatabase(), "purefsm-mysql");
        repository = new MysqlStateMachineRepository(dbi);
        template = new WithinLock(repository, createTransitionOccuredListeners());

        createStateFactories().stream().forEach(StateFactoryRegistration::registerStateFactory);
    }

    protected List<TransitionOccuredListener> createTransitionOccuredListeners() {
        return newArrayList();
    }

    protected abstract List<StateFactory> createStateFactories();

    public DBI getDbi() {
        return dbi;
    }

    public StateMachineRepository getStateMachineRepository() {
        return repository;
    }

    public WithinLock getTemplate() {
        return template;
    }

    public EventTicker getTimeoutTicker(long howOften, TimeUnit timeUnit) {
        return new EventTicker(getStateMachineRepository(), getTemplate(), handleEvent, howOften, timeUnit);
    }

    public CleanUpFinalisedStateMachines getCleaner(Collection<OnCleanupListener> cleanupListeners,
                                                    long scheduleFrequency, TimeUnit scheduleTimeUnit,
                                                    long keepFinalised, ChronoUnit keepFinalisedTimeUnit) {

        return new CleanUpFinalisedStateMachines(getStateMachineRepository(), cleanupListeners, scheduleFrequency, scheduleTimeUnit, keepFinalised, keepFinalisedTimeUnit);
    }
}
