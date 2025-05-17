package org.bukkit.metadata;

import java.lang.ref.WeakReference;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

/**
 * Optional base class for facilitating MetadataValue implementations.
 * <p>
 * This provides all the conversion functions for MetadataValue so that
 * writing an implementation of MetadataValue is as simple as implementing
 * value() and invalidate().
 */
public abstract class MetadataValueAdapter implements MetadataValue {
    protected final WeakReference<Plugin> owningPlugin;

    protected MetadataValueAdapter(Plugin owningPlugin) {
        Validate.notNull(owningPlugin, "owningPlugin cannot be null");
        this.owningPlugin = new WeakReference<Plugin>(owningPlugin);
    }

    public Plugin getOwningPlugin() {
        return owningPlugin.get();
    }

    public int asInt() {
    	Object value = value();
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException|NullPointerException e) {
            }
        }
        return 0; 
    }

    public float asFloat() {
    	Object value = value();
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException|NullPointerException e) {
            }
        }
        return 0; 
    }

    public double asDouble() {
    	Object value = value();
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException|NullPointerException e) {
            }
        }
        return 0; 
    }

    public long asLong() {
    	Object value = value();
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException|NullPointerException e) {
            }
        }
        return 0; 
    }

    public short asShort() {
    	Object value = value();
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        if (value instanceof String) {
            try {
                return Short.parseShort((String) value);
            } catch (NumberFormatException|NullPointerException e) {
            }
        }
        return 0; 
    }

    public byte asByte() {
    	Object value = value();
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        if (value instanceof String) {
            try {
                return Byte.parseByte((String) value);
            } catch (NumberFormatException|NullPointerException e) {
            }
        }
        return 0; 
    }

    public boolean asBoolean() {
        Object value = value();
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }

        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        return value != null;
    }

    public String asString() {
        Object value = value();

        if (value == null) {
            return "";
        }
        return value.toString();
    }

}
