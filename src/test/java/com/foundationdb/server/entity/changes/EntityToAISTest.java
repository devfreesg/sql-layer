/**
 * Copyright (C) 2009-2013 FoundationDB, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.foundationdb.server.entity.changes;

import com.foundationdb.junit.NamedParameterizedRunner;
import com.foundationdb.junit.Parameterization;
import com.foundationdb.server.entity.model.Space;
import com.foundationdb.util.JUnitUtils;
import com.foundationdb.util.Strings;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(NamedParameterizedRunner.class)
public final class EntityToAISTest {
    private static final String SCHEMA = "test";
    private static final String ORIG_SUFFIX = ".json";
    private static final String EXPECTED_SUFFIX = ".ais";

    private static String getShortName(String testName) {
        return testName.substring(0, testName.length() - ORIG_SUFFIX.length());
    }

    @NamedParameterizedRunner.TestParameters
    public static Collection<Parameterization> params() throws IOException {
        String[] testNames = JUnitUtils.getContainingFile(EntityToAISTest.class).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(ORIG_SUFFIX) &&
                       new File(dir, getShortName(name) + EXPECTED_SUFFIX).exists();
            }
        });
        return Collections2.transform(Arrays.asList(testNames), new Function<String, Parameterization>() {
            @Override
            public Parameterization apply(String testName) {
                String shortName = getShortName(testName);
                return new Parameterization(shortName, true, shortName);
            }
        });
    }

    @Test
    public void test() throws IOException {
        Space spaceDef = Space.readSpace(testName + ORIG_SUFFIX, EntityToAISTest.class, null);
        EntityToAIS eToAIS = new EntityToAIS(SCHEMA);
        spaceDef.visit(eToAIS);

        String expected = Strings.dumpFileToString(new File(dir, testName + EXPECTED_SUFFIX));
        String actual = AISDumper.dumpDeterministicAIS(eToAIS.getAIS(), SCHEMA);
        assertEquals("Generated AIS", expected.trim(), actual.trim());
    }

    public EntityToAISTest(String testName) {
        this.testName = testName;
    }

    private final String testName;
    private static final File dir = JUnitUtils.getContainingFile(EntityToAISTest.class);
}
