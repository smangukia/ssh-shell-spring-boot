package com.github.fonimus.ssh.shell.commands.actuator;

import com.github.fonimus.ssh.shell.SshShellCommandFactory;
import com.github.fonimus.ssh.shell.SshShellProperties;
import com.github.fonimus.ssh.shell.handler.PrettyJson;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.actuate.session.SessionsEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;
import static com.github.fonimus.ssh.shell.SshShellUtils.confirm;

/**
 * Actuator shell command
 */
@ShellComponent
@ShellCommandGroup("Actuator Commands")
@ConditionalOnClass(Endpoint.class)
@ConditionalOnProperty(value = SSH_SHELL_PREFIX + ".actuator.enable", havingValue = "true", matchIfMissing = true)
public class ActuatorCommand {

    private ApplicationContext applicationContext;

    private Environment environment;

    private SshShellProperties properties;

    private AuditEventsEndpoint audit;

    private BeansEndpoint beans;

    private ConditionsReportEndpoint conditions;

    private ConfigurationPropertiesReportEndpoint configprops;

    private EnvironmentEndpoint env;

    private HealthEndpoint health;

    private HttpTraceEndpoint httptrace;

    private InfoEndpoint info;

    private LoggersEndpoint loggers;

    private MetricsEndpoint metrics;

    private MappingsEndpoint mappings;

    private SessionsEndpoint sessions;

    private ScheduledTasksEndpoint scheduledtasks;

    private ShutdownEndpoint shutdown;

    private ThreadDumpEndpoint threaddump;

    public ActuatorCommand(ApplicationContext applicationContext, Environment environment,
                           SshShellProperties properties,
                           @Lazy AuditEventsEndpoint audit, @Lazy BeansEndpoint beans,
                           @Lazy ConditionsReportEndpoint conditions,
                           @Lazy ConfigurationPropertiesReportEndpoint configprops, @Lazy EnvironmentEndpoint env,
                           @Lazy HealthEndpoint health, @Lazy HttpTraceEndpoint httptrace, @Lazy InfoEndpoint info,
                           @Lazy LoggersEndpoint loggers, @Lazy MetricsEndpoint metrics,
                           @Lazy MappingsEndpoint mappings, @Lazy SessionsEndpoint sessions,
                           @Lazy ScheduledTasksEndpoint scheduledtasks,
                           @Lazy ShutdownEndpoint shutdown, @Lazy ThreadDumpEndpoint threaddump) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.properties = properties;
        this.audit = audit;
        this.beans = beans;
        this.conditions = conditions;
        this.configprops = configprops;
        this.env = env;
        this.health = health;
        this.httptrace = httptrace;
        this.info = info;
        this.loggers = loggers;
        this.metrics = metrics;
        this.mappings = mappings;
        this.sessions = sessions;
        this.scheduledtasks = scheduledtasks;
        this.shutdown = shutdown;
        this.threaddump = threaddump;
    }

    /**
     * Audit method
     *
     * @param principal principal to filter with
     * @param type      to filter with
     * @return audit
     */
    @ShellMethod(key = "audit", value = "Display audit endpoint.")
    @ShellMethodAvailability("auditAvailability")
    public PrettyJson<AuditEventsEndpoint.AuditEventsDescriptor> audit(
            @ShellOption(value = {"-p", "--principal"}, defaultValue = ShellOption.NULL, help = "Principal to filter " +
                    "on") String principal,
            @ShellOption(value = {"-t", "--type"}, defaultValue = ShellOption.NULL, help = "Type to filter on")
                    String type
    ) {
        return new PrettyJson<>(audit.events(principal, null, null));
    }

    /**
     * @return whether `audit` command is available
     */
    public Availability auditAvailability() {
        return availability("audit", AuditEventsEndpoint.class);
    }

    /**
     * Beans method
     *
     * @return beans
     */
    @ShellMethod(key = "beans", value = "Display beans endpoint.")
    @ShellMethodAvailability("beansAvailability")
    public PrettyJson<BeansEndpoint.ApplicationBeans> beans() {
        return new PrettyJson<>(beans.beans());
    }

    /**
     * @return whether `beans` command is available
     */
    public Availability beansAvailability() {
        return availability("beans", BeansEndpoint.class);
    }

    /**
     * Conditions method
     *
     * @return conditions
     */
    @ShellMethod(key = "conditions", value = "Display conditions endpoint.")
    @ShellMethodAvailability("conditionsAvailability")
    public PrettyJson<ConditionsReportEndpoint.ApplicationConditionEvaluation> conditions() {
        return new PrettyJson<>(conditions.applicationConditionEvaluation());
    }

    /**
     * @return whether `conditions` command is available
     */
    public Availability conditionsAvailability() {
        return availability("conditions", ConditionsReportEndpoint.class);
    }

    /**
     * Config props method
     *
     * @return configprops
     */
    @ShellMethod(key = "configprops", value = "Display configprops endpoint.")
    @ShellMethodAvailability("configpropsAvailability")
    public PrettyJson<ConfigurationPropertiesReportEndpoint.ApplicationConfigurationProperties> configprops() {
        return new PrettyJson<>(configprops.configurationProperties());
    }

    /**
     * @return whether `configprops` command is available
     */
    public Availability configpropsAvailability() {
        return availability("configprops", ConfigurationPropertiesReportEndpoint.class);
    }

    /**
     * Environment method
     *
     * @param pattern pattern to filter with
     * @return env
     */
    @ShellMethod(key = "env", value = "Display env endpoint.")
    @ShellMethodAvailability("envAvailability")
    public PrettyJson<EnvironmentEndpoint.EnvironmentDescriptor> env(
            @ShellOption(value = {"-p", "--pattern"}, defaultValue = ShellOption.NULL, help = "Pattern " +
                    "to filter on") String pattern) {
        return new PrettyJson<>(env.environment(pattern));
    }

    /**
     * @return whether `env` command is available
     */
    public Availability envAvailability() {
        return availability("env", EnvironmentEndpoint.class);
    }

    /**
     * Health method
     *
     * @return health
     */
    @ShellMethod(key = "health", value = "Display health endpoint.")
    @ShellMethodAvailability("healthAvailability")
    public PrettyJson<Health> health() {
        return new PrettyJson<>(health.health());
    }

    /**
     * @return whether `health` command is available
     */
    public Availability healthAvailability() {
        return availability("health", HealthEndpoint.class);
    }

    /**
     * Http traces method
     *
     * @return httptrace
     */
    @ShellMethod(key = "httptrace", value = "Display httptrace endpoint.")
    @ShellMethodAvailability("httptraceAvailability")
    public PrettyJson<HttpTraceEndpoint.HttpTraceDescriptor> httptrace() {
        return new PrettyJson<>(httptrace.traces());
    }

    /**
     * @return whether `httptrace` command is available
     */
    public Availability httptraceAvailability() {
        return availability("httptrace", HttpTraceEndpoint.class);
    }

    /**
     * Info method
     *
     * @return info
     */
    @ShellMethod(key = "info", value = "Display info endpoint.")
    @ShellMethodAvailability("infoAvailability")
    public PrettyJson<Map<String, Object>> info() {
        return new PrettyJson<>(info.info());
    }

    /**
     * @return whether `info` command is available
     */
    public Availability infoAvailability() {
        return availability("info", InfoEndpoint.class);
    }

    /**
     * Loggers method
     *
     * @param action      action to make
     * @param loggerName  logger name for get or configure
     * @param loggerLevel logger level for configure
     * @return loggers
     */
    @ShellMethod(key = "loggers", value = "Display or configure loggers.")
    @ShellMethodAvailability("loggersAvailability")
    public PrettyJson loggers(
            @ShellOption(value = {"-a", "--action"}, help = "Action to perform", defaultValue = "list") LoggerAction
                    action,
            @ShellOption(value = {"-n", "--name"}, help = "Logger name for configuration or display", defaultValue =
                    ShellOption.NULL) String loggerName,
            @ShellOption(value = {"-l", "--level"}, help = "Logger level for configuration", defaultValue =
                    ShellOption.NULL) LogLevel loggerLevel
    ) {
        if ((action == LoggerAction.get || action == LoggerAction.conf) && loggerName == null) {
            throw new IllegalArgumentException("Logger name is mandatory for '" + action + "' action");
        }
        switch (action) {
            case get:
                LoggersEndpoint.LoggerLevels levels = loggers.loggerLevels(loggerName);
                return new PrettyJson<>("Logger named [" + loggerName + "] : [configured: " + levels
                        .getConfiguredLevel() + ", effective: " + levels.getEffectiveLevel() + "]", false);
            case conf:
                if (loggerLevel == null) {
                    throw new IllegalArgumentException("Logger level is mandatory for '" + action + "' action");
                }
                loggers.configureLogLevel(loggerName, loggerLevel);
                return new PrettyJson<>("Logger named [" + loggerName + "] now configured to level [" + loggerLevel +
                                                "]", false);
            default:
                // list
                return new PrettyJson<>(loggers.loggers());
        }
    }

    /**
     * @return whether `loggers` command is available
     */
    public Availability loggersAvailability() {
        return availability("loggers", LoggersEndpoint.class);
    }

    /**
     * Metrics method
     *
     * @param name metrics name to display
     * @param tags tags to filter with
     * @return metrics
     */
    @ShellMethod(key = "metrics", value = "Display metrics endpoint.")
    @ShellMethodAvailability("metricsAvailability")
    public PrettyJson metrics(
            @ShellOption(value = {"-n", "--name"}, help = "Metric name to get", defaultValue = ShellOption.NULL)
                    String name,
            @ShellOption(value = {"-t", "--tags"}, help = "Tags (key=value, separated by coma)", defaultValue =
                    ShellOption.NULL) String tags
    ) {
        if (name != null) {
            MetricsEndpoint.MetricResponse result = metrics.metric(name, tags != null ? Arrays.asList(tags.split(",")
            ) : null);
            if (result == null) {
                String tagsStr = tags != null ? " and tags: " + tags : "";
                throw new IllegalArgumentException("No result for metrics name: " + name + tagsStr);
            }
            return new PrettyJson<>(result);
        }
        return new PrettyJson<>(metrics.listNames());
    }

    /**
     * @return whether `metrics` command is available
     */
    public Availability metricsAvailability() {
        return availability("metrics", MetricsEndpoint.class);
    }

    /**
     * Mappings method
     *
     * @return mappings
     */
    @ShellMethod(key = "mappings", value = "Display mappings endpoint.")
    @ShellMethodAvailability("mappingsAvailability")
    public PrettyJson<MappingsEndpoint.ApplicationMappings> mappings() {
        return new PrettyJson<>(mappings.mappings());
    }

    /**
     * @return whether `mappings` command is available
     */
    public Availability mappingsAvailability() {
        return availability("mappings", MappingsEndpoint.class);
    }

    /**
     * Sessions method
     *
     * @return sessions
     */
    @ShellMethod(key = "sessions", value = "Display sessions endpoint.")
    @ShellMethodAvailability("sessionsAvailability")
    public PrettyJson<SessionsEndpoint.SessionsReport> sessions() {
        return new PrettyJson<>(sessions.sessionsForUsername(null));
    }

    /**
     * @return whether `sessions` command is available
     */
    public Availability sessionsAvailability() {
        return availability("sessions", SessionsEndpoint.class);
    }

    /**
     * Scheduled tasks method
     *
     * @return scheduledtasks
     */
    @ShellMethod(key = "scheduledtasks", value = "Display scheduledtasks endpoint.")
    @ShellMethodAvailability("scheduledtasksAvailability")
    public PrettyJson<ScheduledTasksEndpoint.ScheduledTasksReport> scheduledtasks() {
        return new PrettyJson<>(scheduledtasks.scheduledTasks());
    }

    /**
     * @return whether `scheduledtasks` command is available
     */
    public Availability scheduledtasksAvailability() {
        return availability("scheduledtasks", ScheduledTasksEndpoint.class);
    }

    /**
     * Shutdown method
     *
     * @return shutdown message
     */
    @ShellMethod(key = "shutdown", value = "Shutdown application.")
    @ShellMethodAvailability("shutdownAvailability")
    public String shutdown() {
        if (confirm("Are you sure you want to shutdown application ? [y/N]")) {
            shutdown.shutdown();
            return "Shutting down application...";
        } else {
            return "Aborting shutdown";
        }
    }

    /**
     * @return whether `shutdown` command is available
     */
    public Availability shutdownAvailability() {
        return availability("shutdown", ShutdownEndpoint.class, false);
    }

    /**
     * Thread dump method
     *
     * @return threaddump
     */
    @ShellMethod(key = "threaddump", value = "Display threaddump endpoint.")
    @ShellMethodAvailability("threaddumpAvailability")
    public PrettyJson<ThreadDumpEndpoint.ThreadDumpDescriptor> threaddump() {
        return new PrettyJson<>(threaddump.threadDump());
    }

    /**
     * @return whether `threaddump` command is available
     */
    public Availability threaddumpAvailability() {
        return availability("threaddump", ThreadDumpEndpoint.class);
    }

    private static boolean checkAuthorities(List<String> authorizedRoles, List<String> authorities) {
        if (authorities == null) {
            return true;
        }
        for (String authority : authorities) {
            String check = authority;
            if (check.startsWith("ROLE_")) {
                check = check.substring(5);
            }
            if (authorizedRoles.contains(check)) {
                return true;
            }
        }

        return false;
    }

    private Availability availability(String name, Class<?> clazz) {
        return availability(name, clazz, true);
    }

    private Availability availability(String name, Class<?> clazz, boolean defaultValue) {
        if (!"info".equals(name)) {
            List<String> authorities = SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getAuthorities();
            if (!checkAuthorities(properties.getActuator().getAuthorizedRoles(), authorities)) {
                return Availability.unavailable("actuator commands are forbidden for current user");
            }
        }
        String property = "management.endpoint." + name + ".enabled";
        if (!environment.getProperty(property, Boolean.TYPE, defaultValue)) {
            return Availability.unavailable("endpoint '" + name + "' deactivated (please check property '" + property
                                                    + "')");
        } else if (properties.getActuator().getExcludes().contains(name)) {
            return Availability.unavailable("command is present in exclusion (please check property '" +
                                                    SSH_SHELL_PREFIX + ".actuator.excludes')");
        }
        try {
            applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return Availability.unavailable(clazz.getName() + " is not in application context");
        }
        return Availability.available();
    }

    public enum LoggerAction {
        list, get, conf
    }
}