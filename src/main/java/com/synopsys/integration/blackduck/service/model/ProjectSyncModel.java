/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.util.NameVersion;

public class ProjectSyncModel {
    // these fields are currently not supported - if you need to create a
    // project/version with these fields, or update these fields, please use
    // the create/update methods in ProjectService
    private static final Set<String> IGNORED_REQUEST_FIELDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("versionRequest", "license")));

    private static final Set<Field> ALL_FIELDS = new HashSet<>();

    // project fields
    public static final Field CLONE_CATEGORIES_FIELD = ProjectSyncModel.getFieldSafely("cloneCategories");
    public static final Field CUSTOM_SIGNATURE_ENABLED_FIELD = ProjectSyncModel.getFieldSafely("customSignatureEnabled");
    public static final Field DESCRIPTION_FIELD = ProjectSyncModel.getFieldSafely("description");
    public static final Field NAME_FIELD = ProjectSyncModel.getFieldSafely("name");
    public static final Field PROJECT_LEVEL_ADJUSTMENTS_FIELD = ProjectSyncModel.getFieldSafely("projectLevelAdjustments");
    public static final Field PROJECT_OWNER_FIELD = ProjectSyncModel.getFieldSafely("projectOwner");
    public static final Field PROJECT_TIER_FIELD = ProjectSyncModel.getFieldSafely("projectTier");

    // version fields
    public static final Field CLONE_FROM_RELEASE_URL_FIELD = ProjectSyncModel.getFieldSafely("cloneFromReleaseUrl");
    public static final Field DISTRIBUTION_FIELD = ProjectSyncModel.getFieldSafely("distribution");
    public static final Field NICKNAME_FIELD = ProjectSyncModel.getFieldSafely("nickname");
    public static final Field PHASE_FIELD = ProjectSyncModel.getFieldSafely("phase");
    public static final Field RELEASE_COMMENTS_FIELD = ProjectSyncModel.getFieldSafely("releaseComments");
    public static final Field RELEASED_ON_FIELD = ProjectSyncModel.getFieldSafely("releasedOn");
    public static final Field VERSION_NAME_FIELD = ProjectSyncModel.getFieldSafely("versionName");

    // project fields
    private List<ProjectCloneCategoriesType> cloneCategories;
    private Boolean customSignatureEnabled;
    private String description;
    private String name;
    private Boolean projectLevelAdjustments;
    private String projectOwner;
    private Integer projectTier;

    // version fields
    private String cloneFromReleaseUrl;
    private ProjectVersionDistributionType distribution;
    private String nickname;
    private ProjectVersionPhaseType phase;
    private String releaseComments;
    private Date releasedOn;
    private String versionName;

    private final Set<Field> fieldsWithSetValues = new HashSet<>();

    private static Field getFieldSafely(String fieldName) {
        try {
            Field field = ProjectSyncModel.class.getDeclaredField(fieldName);
            ProjectSyncModel.ALL_FIELDS.add(field);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    public static ProjectSyncModel createWithDefaults(String projectName, String projectVersionName) {
        return createWithDefaults(new NameVersion(projectName, projectVersionName));
    }

    public static ProjectSyncModel createWithDefaults(NameVersion projectAndVersion) {
        ProjectSyncModel projectSyncModel = new ProjectSyncModel(projectAndVersion);
        projectSyncModel.setDistribution(ProjectVersionDistributionType.EXTERNAL);
        projectSyncModel.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        return projectSyncModel;
    }

    public ProjectSyncModel() {

    }

    public ProjectSyncModel(String projectName, String projectVersionName) {
        this(new NameVersion(projectName, projectVersionName));
    }

    public ProjectSyncModel(NameVersion projectAndVersion) {
        setName(projectAndVersion.getName());
        setVersionName(projectAndVersion.getVersion());
    }

    public ProjectRequest createProjectRequest() {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(name);
        projectRequest.setDescription(description);
        projectRequest.setProjectLevelAdjustments(projectLevelAdjustments);
        projectRequest.setProjectOwner(projectOwner);
        projectRequest.setProjectTier(projectTier);
        projectRequest.setCloneCategories(cloneCategories);
        projectRequest.setCustomSignatureEnabled(customSignatureEnabled);

        projectRequest.setVersionRequest(createProjectVersionRequest());

        return projectRequest;
    }

    public ProjectVersionRequest createProjectVersionRequest() {
        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setDistribution(distribution);
        projectVersionRequest.setPhase(phase);
        projectVersionRequest.setVersionName(versionName);
        projectVersionRequest.setReleaseComments(releaseComments);
        projectVersionRequest.setCloneFromReleaseUrl(cloneFromReleaseUrl);
        projectVersionRequest.setReleasedOn(releasedOn);
        projectVersionRequest.setNickname(nickname);

        return projectVersionRequest;
    }

    public void populateProjectView(ProjectView projectView) {
        if (fieldSet(ProjectSyncModel.CLONE_CATEGORIES_FIELD)) {
            projectView.setCloneCategories(cloneCategories);
        }

        if (fieldSet(ProjectSyncModel.CUSTOM_SIGNATURE_ENABLED_FIELD)) {
            projectView.setCustomSignatureEnabled(customSignatureEnabled);
        }

        if (fieldSet(ProjectSyncModel.DESCRIPTION_FIELD)) {
            projectView.setDescription(description);
        }

        if (fieldSet(ProjectSyncModel.NAME_FIELD)) {
            projectView.setName(name);
        }

        if (fieldSet(ProjectSyncModel.PROJECT_LEVEL_ADJUSTMENTS_FIELD)) {
            projectView.setProjectLevelAdjustments(projectLevelAdjustments);
        }

        if (fieldSet(ProjectSyncModel.PROJECT_OWNER_FIELD)) {
            projectView.setProjectOwner(projectOwner);
        }

        if (fieldSet(ProjectSyncModel.PROJECT_TIER_FIELD)) {
            projectView.setProjectTier(projectTier);
        }
    }

    public void populateProjectVersionView(ProjectVersionView projectVersionView) {
        if (fieldSet(ProjectSyncModel.DISTRIBUTION_FIELD)) {
            projectVersionView.setDistribution(distribution);
        }

        if (fieldSet(ProjectSyncModel.NICKNAME_FIELD)) {
            projectVersionView.setNickname(nickname);
        }

        if (fieldSet(ProjectSyncModel.PHASE_FIELD)) {
            projectVersionView.setPhase(phase);
        }

        if (fieldSet(ProjectSyncModel.RELEASE_COMMENTS_FIELD)) {
            projectVersionView.setReleaseComments(releaseComments);
        }

        if (fieldSet(ProjectSyncModel.RELEASED_ON_FIELD)) {
            projectVersionView.setReleasedOn(releasedOn);
        }

        if (fieldSet(ProjectSyncModel.VERSION_NAME_FIELD)) {
            projectVersionView.setVersionName(versionName);
        }
    }

    public boolean shouldHandleProjectVersion() {
        return fieldsWithSetValues.contains(ProjectSyncModel.VERSION_NAME_FIELD) && StringUtils.isNotBlank(versionName);
    }

    public List<ProjectCloneCategoriesType> getCloneCategories() {
        return cloneCategories;
    }

    public void setCloneCategories(List<ProjectCloneCategoriesType> cloneCategories) {
        this.cloneCategories = cloneCategories;
        fieldsWithSetValues.add(ProjectSyncModel.CLONE_CATEGORIES_FIELD);
    }

    public Boolean getCustomSignatureEnabled() {
        return customSignatureEnabled;
    }

    public void setCustomSignatureEnabled(Boolean customSignatureEnabled) {
        this.customSignatureEnabled = customSignatureEnabled;
        fieldsWithSetValues.add(ProjectSyncModel.CUSTOM_SIGNATURE_ENABLED_FIELD);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        fieldsWithSetValues.add(ProjectSyncModel.DESCRIPTION_FIELD);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        fieldsWithSetValues.add(ProjectSyncModel.NAME_FIELD);
    }

    public Boolean getProjectLevelAdjustments() {
        return projectLevelAdjustments;
    }

    public void setProjectLevelAdjustments(Boolean projectLevelAdjustments) {
        this.projectLevelAdjustments = projectLevelAdjustments;
        fieldsWithSetValues.add(ProjectSyncModel.PROJECT_LEVEL_ADJUSTMENTS_FIELD);
    }

    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
        fieldsWithSetValues.add(ProjectSyncModel.PROJECT_OWNER_FIELD);
    }

    public Integer getProjectTier() {
        return projectTier;
    }

    public void setProjectTier(Integer projectTier) {
        this.projectTier = projectTier;
        fieldsWithSetValues.add(ProjectSyncModel.PROJECT_TIER_FIELD);
    }

    public String getCloneFromReleaseUrl() {
        return cloneFromReleaseUrl;
    }

    public void setCloneFromReleaseUrl(String cloneFromReleaseUrl) {
        this.cloneFromReleaseUrl = cloneFromReleaseUrl;
        fieldsWithSetValues.add(ProjectSyncModel.CLONE_FROM_RELEASE_URL_FIELD);
    }

    public ProjectVersionDistributionType getDistribution() {
        return distribution;
    }

    public void setDistribution(ProjectVersionDistributionType distribution) {
        this.distribution = distribution;
        fieldsWithSetValues.add(ProjectSyncModel.DISTRIBUTION_FIELD);
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        fieldsWithSetValues.add(ProjectSyncModel.NICKNAME_FIELD);
    }

    public ProjectVersionPhaseType getPhase() {
        return phase;
    }

    public void setPhase(ProjectVersionPhaseType phase) {
        this.phase = phase;
        fieldsWithSetValues.add(ProjectSyncModel.PHASE_FIELD);
    }

    public String getReleaseComments() {
        return releaseComments;
    }

    public void setReleaseComments(String releaseComments) {
        this.releaseComments = releaseComments;
        fieldsWithSetValues.add(ProjectSyncModel.RELEASE_COMMENTS_FIELD);
    }

    public Date getReleasedOn() {
        return releasedOn;
    }

    public void setReleasedOn(Date releasedOn) {
        this.releasedOn = releasedOn;
        fieldsWithSetValues.add(ProjectSyncModel.RELEASED_ON_FIELD);
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
        fieldsWithSetValues.add(ProjectSyncModel.VERSION_NAME_FIELD);
    }

    private boolean fieldSet(Field field) {
        return fieldsWithSetValues.contains(field);
    }

}
