package slatepowered.reco;

import java.io.*;

/**
 * Proxy for serializer implementations.
 */
public interface Serializer {

    /**
     * Deserializes an object from the given input stream.
     *
     * @param stream The stream.
     * @return The object.
     */
    Object read(InputStream stream) throws IOException;

    /**
     * Writes the the given object to the output stream.
     *
     * @param stream The output stream.
     * @param object The object.
     */
    void write(OutputStream stream, Object object) throws IOException;

}
