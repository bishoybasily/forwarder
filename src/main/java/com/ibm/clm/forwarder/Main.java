package com.ibm.clm.forwarder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author bishoybasily
 * @since 3/8/20
 */
@Log4j2
public class Main {

    private final static String
            HOST = "localhost",
            PARAM_PASSWORD = "p",
            PARAM_RULE = "r",
            PARAM_VERBOSE = "v";

    static {
        JSch.setConfig("StrictHostKeyChecking", "no");
    }

    public static void main(String[] args) {

        Options options = new Options()
                .addOption(PARAM_VERBOSE, "verbose", false, "enable jsch logging")
                .addRequiredOption(PARAM_PASSWORD, "password", true, "the password of the current user")
                .addRequiredOption(PARAM_RULE, "rule", true, "mapping rule in the form of binding_address:binding_port:remote_address:remote_port (accepts multiple values)");

        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);

            String password = commandLine.getOptionValue(PARAM_PASSWORD);
            String[] rules = commandLine.getOptionValues(PARAM_RULE);
            boolean verbose = commandLine.hasOption(PARAM_VERBOSE);

            if (verbose)
                JSch.setLogger(new JSchLogger(log));

            Map<Rule, Session> mappings = initMappings(password, rules);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                destroyMappings(mappings);
            }));

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(String.format("java -jar %s", jarName()), options);
        }

    }

    private static Map<Rule, Session> initMappings(String password, String[] rules) {

        JSch jSch = new JSch();

        return Stream.of(rules)
                .map(String::trim)
                .map(Rule::from)
                .map(new Function<Rule, Map.Entry<Rule, Session>>() {

                    @SneakyThrows
                    @Override
                    public Map.Entry<Rule, Session> apply(Rule rule) {

                        Session session = jSch.getSession(HOST);
                        session.setPassword(password.trim());

                        session.setPortForwardingL(
                                rule.getLocalAddress(),
                                rule.getLocalPort(),
                                rule.getRemoteAddress(),
                                rule.getRemotePort()
                        );
                        session.connect();
                        log.info(String.format("Rule: %s connected", rule));

                        return new AbstractMap.SimpleEntry<>(rule, session);
                    }

                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static void destroyMappings(Map<Rule, Session> mappings) {
        mappings.forEach(new BiConsumer<Rule, Session>() {

            @SneakyThrows
            @Override
            public void accept(Rule rule, Session session) {

                session.delPortForwardingL(
                        rule.getRemoteAddress(),
                        rule.getLocalPort()
                );
                session.disconnect();
                log.info(String.format("Rule: %s disconnected", rule));

            }
        });
    }

    private static String jarName() {
        return new File(Main.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }

}
