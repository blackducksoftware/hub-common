/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.contract.NotificationViewData;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;

public class NotificationService extends DataService {
    // ejk - to get all notifications:
    // <blackduckserver>/api/notifications?startDate=2019-07-01T00:00:00.000Z&endDate=2019-07-15T00:00:00.000Z&filter=notificationType:BOM_EDIT&filter=notificationType:LICENSE_LIMIT&filter=notificationType:POLICY_OVERRIDE&filter=notificationType:RULE_VIOLATION&filter=notificationType:RULE_VIOLATION_CLEARED&filter=notificationType:VERSION_BOM_CODE_LOCATION_BOM_COMPUTED&filter=notificationType:VULNERABILITY&filter=notificationType:PROJECT&filter=notificationType:PROJECT_VERSION

    public NotificationService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<NotificationView> getAllNotifications(Date startDate, Date endDate) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        return blackDuckApiClient.getAllResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder);
    }

    public BlackDuckPageResponse<NotificationView> getPageOfNotifications(Date startDate, Date endDate, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(startDate, endDate, allKnownNotificationTypes);
        return blackDuckApiClient.getPageResponse(requestBuilder, NotificationView.class, blackDuckPageDefinition);
    }

    public List<NotificationUserView> getAllUserNotifications(UserView user, Date startDate, Date endDate) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        BlackDuckRequestBuilder requestBuilder = prepareUserNotificationsRequest(user, startDate, endDate, allKnownNotificationTypes);
        return blackDuckApiClient.getAllResponses(requestBuilder, NotificationUserView.class);
    }

    public BlackDuckPageResponse getPageOfUserNotifications(UserView user, Date startDate, Date endDate, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        List<String> allKnownNotificationTypes = getAllKnownNotificationTypes();
        BlackDuckRequestBuilder requestBuilder = prepareUserNotificationsRequest(user, startDate, endDate, allKnownNotificationTypes);
        return blackDuckApiClient.getPageResponse(requestBuilder, NotificationUserView.class, blackDuckPageDefinition);
    }

    public List<NotificationView> getFilteredNotifications(Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude);
        List<NotificationView> notificationViews = blackDuckApiClient.getAllResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder);
        return reallyFilterNotifications(notificationViews, notificationTypesToInclude);
    }

    public List<NotificationUserView> getFilteredUserNotifications(UserView user, Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = prepareUserNotificationsRequest(user, startDate, endDate, notificationTypesToInclude);
        List<NotificationUserView> notificationUserViews = blackDuckApiClient.getAllResponses(requestBuilder, NotificationUserView.class);
        return reallyFilterNotifications(notificationUserViews, notificationTypesToInclude);
    }

    /**
     * @return The java.util.Date of the most recent notification. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestNotificationDate() throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createLatestDateRequestBuilder();
        List<NotificationView> notifications = blackDuckApiClient.getSomeResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, 1);
        return getFirstCreatedAtDate(notifications);
    }

    /**
     * @return The java.util.Date of the most recent notification in the user's stream. If there are no notifications, the current date will be returned. This can set an initial start time window for all future notifications.
     * @throws IntegrationException
     */
    public Date getLatestUserNotificationDate(UserView userView) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createLatestDateRequestBuilder();
        List<NotificationUserView> userNotifications = blackDuckApiClient.getSomeResponses(userView, UserView.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, 1);
        return getFirstCreatedAtDate(userNotifications);
    }

    private Date getFirstCreatedAtDate(List<? extends NotificationViewData> notifications) {
        if (notifications.size() == 1) {
            return notifications.get(0).getCreatedAt();
        } else {
            return new Date();
        }
    }

    private BlackDuckRequestBuilder prepareUserNotificationsRequest(UserView user, Date startDate, Date endDate, List<String> notificationTypesToInclude) throws IntegrationException {
        HttpUrl url = user.getFirstLink(UserView.NOTIFICATIONS_LINK);
        return createNotificationRequestBuilder(startDate, endDate, notificationTypesToInclude)
                   .url(url);
    }

    private BlackDuckRequestBuilder createLatestDateRequestBuilder() {
        return blackDuckRequestFactory
                   .createCommonGetRequestBuilder()
                   .addBlackDuckFilter(createFilterForAllKnownTypes());
    }

    private List<String> getAllKnownNotificationTypes() {
        List<String> allKnownTypes = Stream.of(NotificationType.values()).map(Enum::name).collect(Collectors.toList());
        return allKnownTypes;
    }

    private BlackDuckRequestFilter createFilterForAllKnownTypes() {
        return createFilterForSpecificTypes(getAllKnownNotificationTypes());
    }

    private BlackDuckRequestFilter createFilterForSpecificTypes(List<String> notificationTypesToInclude) {
        return BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
    }

    private BlackDuckRequestBuilder createNotificationRequestBuilder(Date startDate, Date endDate, List<String> notificationTypesToInclude) {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        BlackDuckRequestFilter notificationTypeFilter = createFilterForSpecificTypes(notificationTypesToInclude);
        return blackDuckRequestFactory
                   .createCommonGetRequestBuilder()
                   .addQueryParameter("startDate", startDateString)
                   .addQueryParameter("endDate", endDateString)
                   .addBlackDuckFilter(notificationTypeFilter);
    }

    /*
    We can not trust the filtering from the Black Duck API. There have been at
    least 2 instances where the lack of filtering created customer issues, so
    we will do this in perpetuity.
    */
    private <T extends NotificationViewData> List<T> reallyFilterNotifications(List<T> notifications, List<String> notificationTypesToInclude) {
        return notifications
                   .stream()
                   .filter(notification -> notificationTypesToInclude.contains(notification.getType().name()))
                   .collect(Collectors.toList());
    }

}
