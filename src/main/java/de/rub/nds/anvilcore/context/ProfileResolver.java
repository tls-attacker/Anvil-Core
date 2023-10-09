/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileResolver {
    String path = "./";
    ObjectMapper objectMapper = new ObjectMapper();

    public ProfileResolver(String path) {
        this.path = path;
    }

    public List<String> resolve(String profile) {
        ArrayList<String> l = new ArrayList<String>();
        l.add(profile);
        return this.resolve(l);
    }

    public List<String> resolve(List<String> profiles) {
        Profile profile;
        Set<String> resolvedIds = new HashSet<String>();

        for (String p : profiles) {
            try {
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                profile = objectMapper.readValue(new File(this.path, p + ".json"), Profile.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            resolvedIds.addAll(profile.getTestIds());
            resolvedIds.addAll(this.resolve(profile.getProfiles()));
        }
        return new ArrayList<String>(resolvedIds);
    }
}
