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
import de.rub.nds.anvilcore.annotation;

import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a parameter is used by a certain test template.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface UseParameterIdentifierProvider {
    /**
     * A class that is used as parameter identifier provider.
     *
     * @return a {@link ParameterIdentifierProvider} subclass
     */
    Class<? extends ParameterIdentifierProvider> value();
}
