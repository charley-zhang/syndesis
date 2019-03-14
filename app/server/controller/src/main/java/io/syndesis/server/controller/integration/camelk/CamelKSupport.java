/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.server.controller.integration.camelk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionStatusBuilder;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.controller.integration.camelk.crd.DoneableIntegration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationList;
import io.syndesis.server.openshift.OpenShiftService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class CamelKSupport {
    //    // IntegrationPhaseInitial --
    //    IntegrationPhaseInitial IntegrationPhase = ""
    //    // IntegrationPhaseWaitingForPlatform --
    //    IntegrationPhaseWaitingForPlatform IntegrationPhase = "Waiting For Platform"
    //    // IntegrationPhaseBuildingContext --
    //    IntegrationPhaseBuildingContext IntegrationPhase = "Building Context"
    //    // IntegrationPhaseBuildImageSubmitted --
    //    IntegrationPhaseBuildImageSubmitted IntegrationPhase = "Build Image Submitted"
    //    // IntegrationPhaseBuildImageRunning --
    //    IntegrationPhaseBuildImageRunning IntegrationPhase = "Build Image Running"
    //    // IntegrationPhaseDeploying --
    //    IntegrationPhaseDeploying IntegrationPhase = "Deploying"
    //    // IntegrationPhaseRunning --
    //    IntegrationPhaseRunning IntegrationPhase = "Running"
    //    // IntegrationPhaseError --
    //    IntegrationPhaseError IntegrationPhase = "Error"
    //    // IntegrationPhaseBuildFailureRecovery --
    //    IntegrationPhaseBuildFailureRecovery IntegrationPhase = "Building Failure Recovery"

    public static final ImmutableSet<String> CAMEL_K_STARTED_STATES = ImmutableSet.of(
                "Waiting For Platform",
                "Building Context",
                "Build Image Submitted",
                "Build Image Running",
                "Deploying");
    public static final ImmutableSet<String> CAMEL_K_FAILED_STATES = ImmutableSet.of(
                "Error",
                "Building Failure Recovery");
    public static final ImmutableSet<String> CAMEL_K_RUNNING_STATES = ImmutableSet.of(
                "Running" );

    public static final String CAMEL_K_INTEGRATION_CRD_NAME = "integrations.camel.apache.org";
    public static final String CAMEL_K_INTEGRATION_CRD_GROUP = "camel.apache.org";
    public static final String CAMEL_K_INTEGRATION_CRD_APIVERSION = "camel.apache.org/v1alpha1";
    public static final String CAMEL_K_INTEGRATION_CRD_VERSION = "v1alpha1";

    public static final CustomResourceDefinition CAMEL_K_INTEGRATION_CRD = new CustomResourceDefinitionBuilder()
            .withApiVersion(CAMEL_K_INTEGRATION_CRD_APIVERSION)
            .withKind("Integration")
            .withNewMetadata()
                .withName(CamelKSupport.CAMEL_K_INTEGRATION_CRD_NAME)
            .endMetadata()
            .withNewSpec()
                .withGroup(CamelKSupport.CAMEL_K_INTEGRATION_CRD_GROUP)
                .withScope("Namespaced")
                .withVersion(CAMEL_K_INTEGRATION_CRD_VERSION)
                .withNewNames()
                    .withKind("Integration")
                    .withListKind("IntegrationList")
                    .withPlural("integrations")
                    .withShortNames(ImmutableList.of("it"))
                    .withSingular("integration")
                .endNames()
            .endSpec()
            .withStatus(new CustomResourceDefinitionStatusBuilder().build())
        .build();

    private CamelKSupport() {
    }

    public static String compress(String data) throws IOException {
        if (data == null) {
            return null;
        }

        return compress(data.getBytes(UTF_8));
    }

    public static String compress(byte[] data) throws IOException {
        if (data == null) {
            return null;
        }

        try(
            ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
            GZIPOutputStream gzip = new GZIPOutputStream(os)
        ) {
            gzip.write(data);
            return os.toString(UTF_8.name());
        }
    }

    public static String propsToString(Properties data) {
        if (data == null) {
            return "";
        }

        try {
            StringWriter w = new StringWriter();
            data.store(w, "");
            return w.toString();
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    public static Properties secretToProperties(Secret secret) {
        Properties properties = new Properties();
        if (secret == null) {
            return properties;
        }

        String data = secret.getStringData().get("application.properties");
        if (data == null) {
            return properties;
        }

        try(Reader reader = new StringReader(data)) {
            properties.load(reader);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }

        return properties;
    }

    @SuppressWarnings("unchecked")
    public static io.syndesis.server.controller.integration.camelk.crd.Integration getIntegrationCR(
            OpenShiftService openShiftService,
            CustomResourceDefinition integrationCRD,
            IntegrationDeployment integrationDeployment) {

        return openShiftService.getCR(
            integrationCRD,
            io.syndesis.server.controller.integration.camelk.crd.Integration.class,
            IntegrationList.class,
            DoneableIntegration.class,
            Names.sanitize(integrationDeployment.getIntegrationId().get())
        );
    }

    @SuppressWarnings("unchecked")
    public static List<io.syndesis.server.controller.integration.camelk.crd.Integration> getIntegrationCRbyLabels(
        OpenShiftService openShiftService,
        CustomResourceDefinition integrationCRD,
        Map<String,String> labels) {

        return openShiftService.getCRBylabel(
            integrationCRD,
            io.syndesis.server.controller.integration.camelk.crd.Integration.class,
            IntegrationList.class,
            DoneableIntegration.class,
            labels
        );
    }

    public static boolean isBuildStarted(io.syndesis.server.controller.integration.camelk.crd.Integration integration) {
        return isInPhase(integration, CamelKSupport.CAMEL_K_STARTED_STATES);

    }

    public static boolean isBuildFailed(io.syndesis.server.controller.integration.camelk.crd.Integration integration) {
        return isInPhase(integration, CamelKSupport.CAMEL_K_FAILED_STATES);
    }

    public static boolean isRunning(io.syndesis.server.controller.integration.camelk.crd.Integration integration) {
        return isInPhase(integration, CamelKSupport.CAMEL_K_RUNNING_STATES);
    }

    public static boolean isInPhase(io.syndesis.server.controller.integration.camelk.crd.Integration integration, Collection<String> phases) {
        return integration != null && integration.getStatus() != null && phases.contains(integration.getStatus().getPhase());
    }

    public static IntegrationDeploymentState getState(io.syndesis.server.controller.integration.camelk.crd.Integration integration) {
        if (integration != null) {
            if (CamelKSupport.isBuildFailed(integration)) {
                return IntegrationDeploymentState.Error;
            }
            if (CamelKSupport.isBuildStarted(integration)) {
                return IntegrationDeploymentState.Pending;
            }
            if (CamelKSupport.isRunning(integration)) {
                return IntegrationDeploymentState.Published;
            }
        }

        return IntegrationDeploymentState.Unpublished;
    }
}
