/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugins.deploy;

import org.apache.maven.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the shared deploy skip strategy {@link AbstractDeployMojo#isSkip(String, String)}. The same method
 * backs both {@code deploy} (project version) and {@code deploy-file} (deployed file version), so the matrix below
 * pins down the {@code releases}/{@code snapshots} semantics for both usages.
 */
class AbstractDeployMojoTest {

    private static final String RELEASE_VERSION = "1.0";

    private static final String SNAPSHOT_VERSION = "1.0-SNAPSHOT";

    private AbstractDeployMojo mojo;

    @BeforeEach
    void setUp() {
        Session session = mock(Session.class);
        when(session.isVersionSnapshot(SNAPSHOT_VERSION)).thenReturn(true);
        when(session.isVersionSnapshot(RELEASE_VERSION)).thenReturn(false);
        mojo = new AbstractDeployMojo() {
            @Override
            public void execute() {
                // no-op: only the shared skip strategy is under test
            }
        };
        mojo.session = session;
    }

    @Test
    void skipTrueAlwaysSkips() {
        assertTrue(mojo.isSkip("true", RELEASE_VERSION));
        assertTrue(mojo.isSkip("true", SNAPSHOT_VERSION));
    }

    @Test
    void skipFalseNeverSkips() {
        assertFalse(mojo.isSkip("false", RELEASE_VERSION));
        assertFalse(mojo.isSkip("false", SNAPSHOT_VERSION));
    }

    @Test
    void skipReleasesSkipsReleaseButNotSnapshot() {
        assertTrue(mojo.isSkip("releases", RELEASE_VERSION));
        assertFalse(mojo.isSkip("releases", SNAPSHOT_VERSION));
    }

    @Test
    void skipSnapshotsSkipsSnapshotButNotRelease() {
        assertTrue(mojo.isSkip("snapshots", SNAPSHOT_VERSION));
        assertFalse(mojo.isSkip("snapshots", RELEASE_VERSION));
    }

    @Test
    void unknownSkipValueIsTreatedAsFalse() {
        assertFalse(mojo.isSkip("garbage", RELEASE_VERSION));
        assertFalse(mojo.isSkip("garbage", SNAPSHOT_VERSION));
    }
}
