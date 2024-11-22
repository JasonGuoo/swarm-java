package org.icespace.swarm.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe context management for agents.
 */
public class SwarmContext {
    private final ConcurrentMap<String, Object> variables;
    private final ReadWriteLock lock;
    private final ObjectMapper objectMapper;

    public SwarmContext() {
        this.variables = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.objectMapper = new ObjectMapper();
    }

    public <T> T get(String key, Class<T> type) {
        lock.readLock().lock();
        try {
            Object value = variables.get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.convertValue(value, type);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void set(String key, Object value) {
        lock.writeLock().lock();
        try {
            variables.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(String key) {
        lock.writeLock().lock();
        try {
            variables.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            variables.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Map<String, Object> snapshot() {
        lock.readLock().lock();
        try {
            return new HashMap<>(variables);
        } finally {
            lock.readLock().unlock();
        }
    }
}