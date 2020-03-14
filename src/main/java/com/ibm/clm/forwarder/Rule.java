package com.ibm.clm.forwarder;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author bishoybasily
 * @since 3/12/20
 */
@Getter
@EqualsAndHashCode(of = {"localAddress", "localPort"})
public class Rule {

    private String localAddress, remoteAddress;
    private Integer localPort, remotePort;

    public Rule(String localAddress, Integer localPort, String remoteAddress, Integer remotePort) {
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    public static Rule from(String rule) {
        String[] strings = rule.split(":");
        return new Rule(strings[0], Integer.parseInt(strings[1]), strings[2], Integer.parseInt(strings[3]));
    }

    @Override
    public String toString() {
        return String.format("%s:%d:%s:%d", localAddress, localPort, remoteAddress, remotePort);
    }
}
