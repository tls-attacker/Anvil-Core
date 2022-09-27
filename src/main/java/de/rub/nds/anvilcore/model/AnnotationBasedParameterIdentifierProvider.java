/*
 * SSH-Anvil - Combinatoric Testing for SSH Server Implementations
 *
 * Copyright (c) 2022 Jan Holthuis <jan.holthuis@ruhr-uni-bochum.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.annotation.UseParameterIdentifierProvider;
import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * Collects ParameterIdentifiers by checking the {link Parameter} annotations
 * of the test method and class.
 */
public class AnnotationBasedParameterIdentifierProvider extends ParameterIdentifierProvider {

    /** Instantiated parameter identifier providers (cached to avoid superfluous constructor invocations). */
    private final Map<Class<? extends ParameterIdentifierProvider>, ParameterIdentifierProvider> providers = new ConcurrentHashMap<>();

    @Override
    protected List<ParameterIdentifier> getAllParameterIdentifiers(
        final DerivationScope derivationScope
    ) {
        throw new UnsupportedOperationException("calling this method is not supported");
    }

    @Override
    public List<ParameterIdentifier> getModelParameterIdentifiers(
        final DerivationScope derivationScope
    ) {
        final ExtensionContext extensionContext = derivationScope.getExtensionContext();
        return AnnotationUtils
            .findAnnotation(
                extensionContext.getRequiredTestMethod(),
                UseParameterIdentifierProvider.class
            )
            .or(() ->
                AnnotationUtils.findAnnotation(
                    extensionContext.getRequiredTestClass(),
                    UseParameterIdentifierProvider.class
                )
            )
            .map(annotation -> annotation.value())
            .map(cls ->
                this.providers.computeIfAbsent(
                        cls,
                        key -> {
                            try {
                                return cls.getDeclaredConstructor().newInstance();
                            } catch (ReflectiveOperationException e) {
                                throw new RuntimeException(
                                    "Failed to instantiate parameter identifier provider",
                                    e
                                );
                            }
                        }
                    )
            )
            .map(provider -> provider.getModelParameterIdentifiers(derivationScope))
            .orElseThrow(() ->
                new RuntimeException(
                    "Encountered test class without parameter identifier provider",
                    null
                )
            );
    }
}
