package com.synopsys.integration.blackduck.api.recipe;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class CreateDetailedProjectRecipeTest extends BasicRecipe {
    private ProjectView projectView;

    @AfterEach
    public void cleanup() {
        deleteProject(projectView);
    }

    @Test
    public void testCreatingAProject() throws IntegrationException {
        /*
         * let's post the project/version in Black Duck
         */
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        ProjectSyncModel projectSyncModel = createProjectSyncModel(uniqueProjectName, PROJECT_VERSION_NAME);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectSyncModel.createProjectRequest());
        HttpUrl projectUrl = projectVersionWrapper.getProjectView().getHref();

        /*
         * using the url of the created project, we can now verify that the
         * fields are set correctly with the BlackDuckService, a general purpose API
         * wrapper to handle common GET requests and their response payloads
         */
        BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        projectView = blackDuckService.getResponse(projectUrl, ProjectView.class);
        ProjectVersionView projectVersionView = blackDuckService.getResponse(projectView, ProjectView.CANONICALVERSION_LINK_RESPONSE).get();

        Assertions.assertEquals(uniqueProjectName, projectView.getName());
        Assertions.assertEquals("A sample testing project to demonstrate blackduck-common capabilities.", projectView.getDescription());

        Assertions.assertEquals(PROJECT_VERSION_NAME, projectVersionView.getVersionName());
        Assertions.assertEquals(ProjectVersionPhaseType.DEVELOPMENT, projectVersionView.getPhase());
        Assertions.assertEquals(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.OPENSOURCE, projectVersionView.getDistribution());
    }

}
