/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.anvilcore.junit.simpletest;

import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import org.junit.jupiter.params.aggregator.ArgumentAccessException;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class SimpleArgumentsAccessor implements ArgumentsAccessor {
    
    private final List<DerivationParameter> testInput;

    public SimpleArgumentsAccessor(List<DerivationParameter> testInput) {
        this.testInput = testInput;
    }

    @Override
    public Object get(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public <T> T get(int i, Class<T> type) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Character getCharacter(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Boolean getBoolean(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Byte getByte(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Short getShort(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Integer getInteger(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Long getLong(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Float getFloat(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public Double getDouble(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public String getString(int i) throws ArgumentAccessException {
        throw new ArgumentAccessException("Simple accessor only supports toList()");
    }

    @Override
    public int size() {
        return testInput.size();
    }

    @Override
    public Object[] toArray() {
        return testInput.toArray();
    }

    @Override
    public List<Object> toList() {
        return new LinkedList<Object>(testInput);
    }

}
