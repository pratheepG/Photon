package com.photon.identity.authentication.utils;

public final class WipableChars implements CharSequence, AutoCloseable {
    private char[] data;
    public WipableChars(char[] data) { this.data = data; }
    @Override public int length() { return data.length; }
    @Override public char charAt(int index) { return data[index]; }
    @Override public CharSequence subSequence(int start, int end) {
        char[] slice = java.util.Arrays.copyOfRange(data, start, end);
        return new WipableChars(slice);
    }
    /** ONLY for debugging-avoid; you don't want this called. */
    @Override public String toString() {
        throw new UnsupportedOperationException("toString() disabled for security");
    }
    public char[] borrow() { return data; }
    @Override public void close() {
        if (data != null) java.util.Arrays.fill(data, '\0');
        data = new char[0];
    }
}