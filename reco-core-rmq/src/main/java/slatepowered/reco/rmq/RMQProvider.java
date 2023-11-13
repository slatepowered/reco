package slatepowered.reco.rmq;

import com.rabbitmq.client.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import slatepowered.reco.BinaryCommunicationProvider;
import slatepowered.reco.Message;
import slatepowered.reco.ReceivedMessage;
import slatepowered.reco.Serializer;
import slatepowered.veru.misc.Throwables;

import java.io.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@SuppressWarnings({ "rawtypes" })
public class RMQProvider extends BinaryCommunicationProvider<RMQChannel> {

    // logger
    static final Logger LOGGER = Logger.getLogger("RMQProvider");

    public static Builder builder(String localName) {
        return new Builder(localName);
    }

    ////////////////////////////////////////

    // the RabbitMQ connection and channel
    Connection rmqConnection;
    Channel rmqChannel;

    String exchangeName;
    String publishExchangeName;

    public RMQProvider(String localName, Serializer serializer) {
        super(localName, serializer);
    }

    public String getExchangeName() {
        return exchangeName;
    }

    /**
     * Connect this communication provider to the given
     * RabbitMQ channel.
     *
     * @param rmqChannel The RabbitMQ channel.
     * @return This.
     */
    public RMQProvider connect(Channel rmqChannel) {
        this.rmqConnection = rmqChannel.getConnection();
        this.rmqChannel = rmqChannel;
        return this;
    }

    public static Channel makeConnection(String host, int port, String username, String password,
                                         String virtualHost) throws IOException, TimeoutException {
        // create connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        if (port >= 0) factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        return factory.newConnection().createChannel();
    }

    /**
     * Connect to the RabbitMQ instance at the
     * provided host and port. The credentials and
     * virtual host name are hard-coded.
     * @param host The hostname.
     * @param port The port (if -1 default is used).
     * @param username The username.
     * @param password The password.
     * @param virtualHost The virtual host name.
     */
    public RMQProvider connect(String host, int port, String username, String password,
                               String virtualHost) {
        try {
            connect(makeConnection(host, port, username, password, virtualHost));
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
        }

        return this;
    }

    /**
     * Binds this connection and starts
     * listening for messages on both the local
     * queue and the publishing queue.
     *
     * @param exchangeName The exchange base name to bind to.
     */
    public RMQProvider bind(String exchangeName) {
        try {
            // declare communication exchanges
            this.exchangeName = exchangeName;
            this.publishExchangeName = exchangeName + "pub";
            rmqChannel.exchangeDeclare(exchangeName, "topic");
            rmqChannel.exchangeDeclare(publishExchangeName, "fanout");

            // declare and bind local queue
            rmqChannel.queueDeclare(localName, false, false, true, null);
            rmqChannel.queueBind(localName, exchangeName, localName);

            // create listener on local queue
            rmqChannel.basicConsume(localName, true, (consumerTag, message) -> receiveRMQ(message, localName), consumerTag -> { });

            // declare and bind pub queue
            rmqChannel.queueDeclare(localName, false, false, true, null);
            rmqChannel.queueBind(localName, publishExchangeName, localName);

            // create listener on pub queue
            rmqChannel.basicConsume(localName, true, (consumerTag, message) -> receiveRMQ(message, publishExchangeName), consumerTag -> { });
        } catch (Exception e) {
            LOGGER.warning("Failed to bind to local and pub queue");
            e.printStackTrace();
        }

        return this;
    }

    // handles received RMQ deliveries
    private void receiveRMQ(Delivery delivery, String queueName) {
        try {
            // decode message body and properties
            DecodeResult decodeResult = decodeReceivedMessage(delivery.getBody());
            assert decodeResult != null;
            ReceivedMessage<?> message = decodeResult.message;

            // call communication provider
            received(
                    message,
                    decodeResult.domain,
                    queueName,
                    decodeResult.sourceName
            );
        } catch (Exception e) {
            logger.warning("Failed to handle received msg cause(UncaughtError)");
            e.printStackTrace();
        }
    }

    // result record
    @Data
    static class DecodeResult {
        final Domain domain;
        final String sourceName;
        final ReceivedMessage<?> message;
    }

    // packs a message and its content
    // into a byte array with the appropriate
    // headers and identifier/name
    private byte[] packSendingMessage(Message message,
                                      Domain domain) {
        try {
            // create stream
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            // write message name
            dataStream.writeUTF(message.getName());

            // write type and headers
            dataStream.writeByte(domain.getEncoded());
            dataStream.writeUTF(localName); // write source node

            // serialize message content
            serializer.write(dataStream, message.payload());

            byte[] bytes = byteStream.toByteArray();

            // return bytes
            return bytes;
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    // decodes a message from
    // the packed data
    private DecodeResult decodeReceivedMessage(byte[] bytes) {
        try {
            // create stream
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            DataInputStream dataStream = new DataInputStream(byteStream);

            // read message name
            String name = dataStream.readUTF();

            // read type and headers
            byte type = dataStream.readByte();
            String sourceName = dataStream.readUTF();

            // deserialize message content
            ReceivedMessage<Object> message = new ReceivedMessage<>(name);
            message.payload(serializer.read(dataStream));

            // return result
            return new DecodeResult(Domain.getByEncoded(type), sourceName, message);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    // constant basic properties
    static final AMQP.BasicProperties C_BASIC_PROPERTIES = new AMQP.BasicProperties();

    /**
     * Sends the given message to a specific
     * target.
     *
     * @param message The message.
     * @param target The target.
     */
    public void send(Message message, String target) {
        try {
            // pack bytes
            byte[] bytes = packSendingMessage(message, Domain.DIRECT);

            // publish with routing key
            rmqChannel.basicPublish(exchangeName, target, C_BASIC_PROPERTIES, bytes);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
        }
    }

    /**
     * Sends the given message to a specific
     * auxiliary target.
     *
     * @param message The message.
     * @param target The aux target.
     */
    public void sendAux(Message message, String target) {
        try {
            // pack bytes
            byte[] bytes = packSendingMessage(message, Domain.AUX);

            // publish with routing key
            rmqChannel.basicPublish(exchangeName + "." + target, localName, C_BASIC_PROPERTIES, bytes);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
        }
    }

    /**
     * Publish a message to all nodes
     * listening to the publishing queue.
     *
     * @param message The message to publish.
     */
    @Override
    public void publish(Message<?> message) {
        try {
            // pack bytes
            byte[] bytes = packSendingMessage(message, Domain.PUBLISH);

            // publish with routing key
            rmqChannel.basicPublish(publishExchangeName, localName, C_BASIC_PROPERTIES, bytes);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
        }
    }

    @Override
    public RMQChannel newChannel(String remote, boolean aux) {
        RMQChannel channel = new RMQChannel(this, remote, aux);
        withChannel(channel);

        if (aux) {
            try {
                // if auxiliary, create and listen on that exchange
                rmqChannel.exchangeDeclare(exchangeName + "." + remote, "fanout");

                rmqChannel.queueDeclare(localName, false, false, true, null);
                rmqChannel.queueBind(localName, exchangeName + "." + remote, localName);

                rmqChannel.basicConsume(localName, true, (consumerTag, message) -> receiveRMQ(message, remote), consumerTag -> { });
            } catch (Exception e) {
                // rethrow error
                Throwables.sneakyThrow(e);
            }
        }

        return channel;
    }

    @Override
    public void send(Message<?> message, RMQChannel channel) {
        send(message, channel.remote);
    }

    @Override
    public String remote() {
        return null;
    }

    @Override
    public void send(Message<?> message) {
        publish(message);
    }

    @Override
    public void close() {
        try {
            rmqChannel.close();
            rmqConnection.close();
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
    }

    /**
     * Builds an RMQ provider instance.
     */
    @RequiredArgsConstructor
    public static class Builder {
        /** The name of the local node. */
        protected final String localName;
        /** The serializer to use for message content. */
        protected Serializer serializer;

        protected String host;
        protected int port = 5672;
        protected String username;
        protected String password;
        protected String virtualHost = "/";

        protected String exchangeName;

        public Builder serializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder virtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
            return this;
        }

        public Builder exchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }

        public RMQProvider build() {
            return new RMQProvider(localName, serializer)
                    .connect(host, port, username, password, virtualHost)
                    .bind(exchangeName);
        }
    }

}
