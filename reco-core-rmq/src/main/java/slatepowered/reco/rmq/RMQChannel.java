package slatepowered.reco.rmq;

import slatepowered.reco.AbstractChannel;
import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.Message;

public class RMQChannel extends AbstractChannel {

    // the RMQ connection
    final RMQProvider connection;

    // the remote node name
    final String remote;

    // whether this channel is auxiliary
    final boolean isAux;

    public RMQChannel(RMQProvider connection, String remote, boolean isAux) {
        this.connection = connection;
        this.remote = remote;
        this.isAux = isAux;
    }

    public RMQProvider getConnection() {
        return connection;
    }

    public String getRemote() {
        return remote;
    }

    @Override
    public String remote() {
        return remote;
    }

    @Override
    public void send(Message<?> message) {
        if (!isAux) {
            connection.send(message, remote);
        } else {
            connection.sendAux(message, remote);
        }
    }

    @Override
    public void publish(Message<?> message) {
        connection.publish(message);
    }

    public boolean isAux() {
        return isAux;
    }

    @Override
    public CommunicationProvider<?> provider() {
        return connection;
    }
}
