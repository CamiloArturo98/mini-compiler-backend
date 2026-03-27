package com.compiler.vm;

public class VMValue {

    public enum Type {
        NUMBER, STRING, BOOLEAN, NULL
    }

    private final Type type;
    private final Object value;

    private VMValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static VMValue ofNumber(double v)  { return new VMValue(Type.NUMBER,  v); }
    public static VMValue ofString(String v)  { return new VMValue(Type.STRING,  v); }
    public static VMValue ofBoolean(boolean v){ return new VMValue(Type.BOOLEAN, v); }
    public static VMValue ofNull()            { return new VMValue(Type.NULL,    null); }

    public Type getType() { return type; }

    public double asNumber() {
        if (type == Type.NUMBER) return (double) value;
        throw new RuntimeException("Expected NUMBER but got " + type);
    }

    public String asString() {
        if (type == Type.STRING) return (String) value;
        return toString();
    }

    public boolean asBoolean() {
        if (type == Type.BOOLEAN) return (boolean) value;
        if (type == Type.NULL)    return false;
        if (type == Type.NUMBER)  return (double) value != 0;
        if (type == Type.STRING)  return !((String) value).isEmpty();
        return false;
    }

    public boolean isTruthy() { return asBoolean(); }

    @Override
    public String toString() {
        if (type == Type.NULL)   return "null";
        if (type == Type.NUMBER) {
            double d = (double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VMValue other)) return false;
        if (type != other.type) return false;
        if (type == Type.NULL) return true;
        return value.equals(other.value);
    }
}