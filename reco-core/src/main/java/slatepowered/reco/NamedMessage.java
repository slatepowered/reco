package slatepowered.reco;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A message-like which provides a name for
 * all messages instantiated to follow.
 */
public interface NamedMessage extends MessageLike<Object> {

    /**
     * The 'NAME' constant values cached by class
     * for performance reasons.
     */
    Map<Class<?>, String> nameConstantCache = new HashMap<>();

    /**
     * Get the name from the class.
     *
     * @param klass The named message class.
     * @return The name.
     */
    static String getName(Class<? extends NamedMessage> klass) {
        // index cache
        String name = nameConstantCache.get(klass);
        if (name != null)
            return name;

        try {
            // get field
            Field f = klass.getField("NAME");
            if (f.getType() != String.class)
                throw new NoSuchFieldException();

            // get value, cache and return
            name = (String) f.get(null);
            nameConstantCache.put(klass, name);
            return name;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Failed to get message name from " + klass + ": no such constant 'String NAME'");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get message name from " + klass + ": ", e);
        }
    }

    /////////////////////////////////

    /**
     * Get the name of all messages of this type.
     *
     * The default implementation looks for a constant
     * 'NAME' in the current class.
     *
     * @return The message name.
     */
    default String messageName() {
        return getName(getClass());
    }

    @Override
    default Message<Object> toMessage() {
        return new Message<>(messageName(), this);
    }

}
