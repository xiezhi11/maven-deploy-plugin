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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Parent;
import org.apache.maven.api.plugin.MojoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
class DeployFileMojoUnitTest {
    MockDeployFileMojo mojo;
    Parent parent;

    @BeforeEach
    void setUp() {
        parent = Parent.newBuilder()
                .groupId("parentGroup")
                .artifactId("parentArtifact")
                .version("parentVersion")
                .build();
        Model pomModel = Model.newBuilder().packaging(null).parent(parent).build();
        mojo = new MockDeployFileMojo(pomModel);
    }

    static class MockDeployFileMojo extends DeployFileMojo {
        private Model model;

        MockDeployFileMojo(Model model) {
            this.model = model;
        }

        @Override
        protected Model readModel(Path pomFile) throws MojoException {
            return model;
        }
    }

    @Test
    void processPomFromPomFileWithParent4() {
        mojo.setPomFile(Paths.get("foo.bar"));
        setMojoModel(mojo, null, "artifact", "version", "packaging", parent);
        mojo.initProperties();
        checkMojoProperties("parentGroup", "artifact", "version", "packaging");
    }

    @Test
    void processPomFromPomFileWithParent5() {
        mojo.setPomFile(Paths.get("foo.bar"));
        setMojoModel(mojo, "group", "artifact", "version", "packaging", parent);
        mojo.initProperties();
        checkMojoProperties("group", "artifact", "version", "packaging");
    }

    @Test
    void processPomFromPomFileWithParent6() {
        mojo.setPomFile(Paths.get("foo.bar"));
        setMojoModel(mojo, "group", "artifact", "version", "packaging", null);
        mojo.initProperties();
        checkMojoProperties("group", "artifact", "version", "packaging");
    }

    @Test
    void processPomFromPomFileWithOverrides() {
        mojo.setPomFile(Paths.get("foo.bar"));
        setMojoModel(mojo, "group", "artifact", "version", "packaging", null);
        mojo.setGroupId("groupO");
        mojo.setArtifactId("artifactO");
        mojo.setVersion("versionO");
        mojo.setPackaging("packagingO");
        mojo.initProperties();
        checkMojoProperties("groupO", "artifactO", "versionO", "packagingO");
    }

    @Test
    void validateSideArtifactsFailsWhenFilesAndTypesCountDiffer() {
        mojo.setFiles("artifact-1.jar,artifact-2.jar");
        mojo.setTypes("jar");
        mojo.setClassifiers("classifier-1,classifier-2");

        MojoException e = assertThrows(MojoException.class, () -> mojo.validateSideArtifacts());
        String message = e.getMessage();
        assertTrue(message.contains("'files'"), message);
        assertTrue(message.contains("'types'"), message);
        assertTrue(message.contains("'classifiers'"), message);
        assertTrue(message.contains("'files' (2 entries)"), message);
        assertTrue(message.contains("'types' (1 entries)"), message);
        assertTrue(message.contains("'classifiers' (2 entries)"), message);
    }

    @Test
    void validateSideArtifactsFailsWhenFilesAndClassifiersCountDiffer() {
        mojo.setFiles("artifact-1.jar,artifact-2.jar");
        mojo.setTypes("jar,war");
        mojo.setClassifiers("classifier-1");

        MojoException e = assertThrows(MojoException.class, () -> mojo.validateSideArtifacts());
        String message = e.getMessage();
        assertTrue(message.contains("'files'"), message);
        assertTrue(message.contains("'types'"), message);
        assertTrue(message.contains("'classifiers'"), message);
        assertTrue(message.contains("'files' (2 entries)"), message);
        assertTrue(message.contains("'types' (2 entries)"), message);
        assertTrue(message.contains("'classifiers' (1 entries)"), message);
    }

    @Test
    void validateSideArtifactsFailsWhenTypesMissing() {
        mojo.setFiles("artifact-1.jar");
        mojo.setClassifiers("classifier-1");

        MojoException e = assertThrows(MojoException.class, () -> mojo.validateSideArtifacts());
        assertEquals("You must specify 'types' if you specify 'files'", e.getMessage());
    }

    @Test
    void validateSideArtifactsFailsWhenClassifiersMissing() {
        mojo.setFiles("artifact-1.jar");
        mojo.setTypes("jar");

        MojoException e = assertThrows(MojoException.class, () -> mojo.validateSideArtifacts());
        assertEquals("You must specify 'classifiers' if you specify 'files'", e.getMessage());
    }

    @Test
    void validateSideArtifactsFailsWhenFilesMissingForTypes() {
        mojo.setTypes("jar");

        MojoException e = assertThrows(MojoException.class, () -> mojo.validateSideArtifacts());
        assertEquals("You must specify 'files' if you specify 'types'", e.getMessage());
    }

    @Test
    void validateSideArtifactsFailsWhenFilesMissingForClassifiers() {
        mojo.setClassifiers("classifier-1");

        MojoException e = assertThrows(MojoException.class, () -> mojo.validateSideArtifacts());
        assertEquals("You must specify 'files' if you specify 'classifiers'", e.getMessage());
    }

    @Test
    void validateSideArtifactsPassesWhenCountsMatch() {
        mojo.setFiles("artifact-1.jar,artifact-2.jar");
        mojo.setTypes("jar,war");
        mojo.setClassifiers("classifier-1,classifier-2");

        mojo.validateSideArtifacts();
    }

    private void checkMojoProperties(
            final String expectedGroup,
            final String expectedArtifact,
            final String expectedVersion,
            final String expectedPackaging) {
        assertEquals(expectedGroup, mojo.getGroupId());
        assertEquals(expectedArtifact, mojo.getArtifactId());
        assertEquals(expectedVersion, mojo.getVersion());
        assertEquals(expectedPackaging, mojo.getPackaging());
    }

    private void setMojoModel(
            MockDeployFileMojo mojo, String group, String artifact, String version, String packaging, Parent parent) {
        mojo.model = Model.newBuilder()
                .groupId(group)
                .artifactId(artifact)
                .version(version)
                .packaging(packaging)
                .parent(parent)
                .build();
    }
}
