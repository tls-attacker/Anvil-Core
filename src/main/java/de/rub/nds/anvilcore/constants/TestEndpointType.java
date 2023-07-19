/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.anvilcore.constants;

public enum TestEndpointType {
    CLIENT("client"),
    SERVER("server"),
    BOTH("both");

    private final String mode;

    TestEndpointType(final String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return this.mode;
    }
}
