/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
var RiskReport = function (myJQuery, jsonData) {
    this.myJQuery = myJQuery;
    this.rawdata = jsonData;
};
RiskReport.prototype.getPercentage = function (count) {
    var totalCount = this.rawdata.totalComponents;
    var percentage = 0;
    if (totalCount > 0 && count > 0) {
        percentage = (count / totalCount) * 100;
    }
    return percentage;
};

RiskReport.prototype.createRiskString = function (high, medium, low) {
    if (high != 0) {
        return "H";
    } else if (medium != 0) {
        return "M";
    } else if (low != 0) {
        return "L";
    } else {
        return "-";
    }
};

RiskReport.prototype.createPhaseString = function (phase) {
    if (phase == "PLANNING") {
        return "In Planning";
    } else if (phase == "DEVELOPMENT") {
        return "In Development";
    } else if (phase == "RELEASED") {
        return "Released";
    } else if (phase == "DEPRECATED") {
        return "Deprecated";
    } else if (phase == "ARCHIVED") {
        return "Archived";
    } else {
        return "Unknown Phase";
    }
};

RiskReport.prototype.createDistributionString = function (distribution) {
    if (distribution == "EXTERNAL") {
        return "External";
    } else if (distribution == "SAAS") {
        return "SaaS";
    } else if (distribution == "INTERNAL") {
        return "Internal";
    } else if (distribution == "OPENSOURCE") {
        return "Open Source";
    } else {
        return "Unknown Distribution";
    }
};

RiskReport.prototype.createHeader = function () {
    var reportHeader = document.createElement("div");
    this.myJQuery(reportHeader).addClass("reportHeader");
    var title = document.createElement("div");
    this.myJQuery(title).addClass("h1 reportHeaderTitle");
    this.myJQuery(title).text("Black Duck Risk Report");
    var icon = document.createElement("div");
    this.myJQuery(icon).addClass("reportHeaderIcon");
    this.myJQuery(icon).css({"float": "right"});

    this.myJQuery(reportHeader).append(title);
    this.myJQuery(reportHeader).append(icon);
    return reportHeader;
};

RiskReport.prototype.createVersionSummary = function () {
    var table = document.createElement("div");
    this.myJQuery(table).addClass("versionSummaryTable");
    var versionInfo = document.createElement("div");
    var projectName = document.createElement("div");
    var projectVersion = document.createElement("div");
    var moreDetail = document.createElement("div");

    this.myJQuery(projectName).addClass("clickable linkText versionSummaryLargeLabel");

    if (this.rawdata.projectURL) {
        this.myJQuery(projectName).attr("onclick", "window.open('" + this.rawdata.projectURL + "', '_blank');");
    }
    this.myJQuery(projectName).text(this.rawdata.projectName);

    this.myJQuery(projectVersion).addClass("clickable linkText versionSummaryLargeLabel");
    if (this.rawdata.projectVersionURL) {
        this.myJQuery(projectVersion).attr("onclick", "window.open('" + this.rawdata.projectVersionURL + "', '_blank');");
    }
    this.myJQuery(projectVersion).text(this.rawdata.projectVersion);

    this.myJQuery(moreDetail).addClass("linkText riskReportText clickable evenPadding");
    this.myJQuery(moreDetail).css({"float": "right"});
    if (this.rawdata.projectVersionURL) {
        this.myJQuery(moreDetail).attr("onclick", "window.open('" + this.rawdata.projectVersionURL + "', '_blank');");
    }
    this.myJQuery(moreDetail).text("See more detail...");

    this.myJQuery(versionInfo).append(projectName);
    this.myJQuery(versionInfo).append(this.myJQuery('<div class="versionSummaryLargeLabel"><i class="fa fa-caret-right"></i></div>'))
    this.myJQuery(versionInfo).append(projectVersion);
    this.myJQuery(versionInfo).append(moreDetail);

    var info = this.myJQuery(document.createElement("div"));
    this.myJQuery(info).append(this.myJQuery('<div class="versionSummaryLabel">Phase:</div>'));
    this.myJQuery(info).append(this.myJQuery('<div class="versionSummaryLabel">' + this.createPhaseString(this.rawdata.phase) + '</div>'));
    this.myJQuery(info).append(this.myJQuery('<div class="versionSummaryLabel">|</div>'));
    this.myJQuery(info).append(this.myJQuery('<div class="versionSummaryLabel">Distribution:</div>'));
    this.myJQuery(info).append(this.myJQuery('<div class="versionSummaryLabel">' + this.createDistributionString(this.rawdata.distribution) + '</div>'));
    this.myJQuery(table).append(versionInfo);
    this.myJQuery(table).append(info);

    return table;
};

RiskReport.prototype.createHorizontalBar = function (labelId, labelValue, clickFnName, barId, barValue, barStyleClass) {
    var percentage = this.getPercentage(barValue) + '%';
    return this.myJQuery('<div class="progress-bar horizontal">'
        + '<div id="' + labelId + '" class="clickable riskSummaryLabel"'
        + 'onclick="' + clickFnName + '(this)">' + labelValue + '</div>'
        + '<div class="riskSummaryCount">' + barValue + '</div>'
        + '<div class="progress-track">'
        + '    <div id="' + barId + '" class="' + barStyleClass + '" style="width:' + percentage + '">'
        + '        <span style="display:none;">' + percentage + '</span>'
        + '    </div>'
        + '</div>'
        + '</div>');
};

RiskReport.prototype.createSecurityRiskContainer = function () {
    var container = document.createElement("div");
    this.myJQuery(container).addClass("riskSummaryContainer horizontal rounded");
    var labelDiv = document.createElement("div");
    this.myJQuery(labelDiv).addClass("riskSummaryContainerLabel");
    this.myJQuery(labelDiv).text("Security Risk").append('<i id="securityDescriptionIcon"'
        + 'class="fa fa-info-circle infoIcon"'
        + 'title="Calculated risk on number of component versions based on known vulnerabilities."></i>');
    this.myJQuery(container).append(labelDiv);

    this.myJQuery(container).append(this.createHorizontalBar('highSecurityRiskLabel', 'High', 'filterTableByVulnerabilityRisk', 'highVulnerabilityRiskBar', this.rawdata.vulnerabilityRiskHighCount, 'progress-fill-high'));
    this.myJQuery(container).append(this.createHorizontalBar('mediumSecurityRiskLabel', 'Medium', 'filterTableByVulnerabilityRisk', 'mediumVulnerabilityRiskBar', this.rawdata.vulnerabilityRiskMediumCount, 'progress-fill-medium'));
    this.myJQuery(container).append(this.createHorizontalBar('lowSecurityRiskLabel', 'Low', 'filterTableByVulnerabilityRisk', 'lowVulnerabilityRiskBar', this.rawdata.vulnerabilityRiskLowCount, 'progress-fill-low'));
    this.myJQuery(container).append(this.createHorizontalBar('noneSecurityRiskLabel', 'None', 'filterTableByVulnerabilityRisk', 'noneVulnerabilityRiskBar', this.rawdata.vulnerabilityRiskNoneCount, 'progress-fill-none'));
    return container;
};

RiskReport.prototype.createLicenseRiskContainer = function () {
    var container = document.createElement("div");
    this.myJQuery(container).addClass("riskSummaryContainer horizontal rounded");
    var labelDiv = document.createElement("div");
    this.myJQuery(labelDiv).addClass("riskSummaryContainerLabel");
    this.myJQuery(labelDiv).text("License Risk").append('<i id="licenseDescriptionIcon"'
        + 'class="fa fa-info-circle infoIcon"'
        + 'title="Calculated risk based on open source software (OSS) license use in your projects."></i>');
    this.myJQuery(container).append(labelDiv);

    this.myJQuery(container).append(this.createHorizontalBar('highLicenseRiskLabel', 'High', 'filterTableByLicenseRisk', 'highLicenseRiskBar', this.rawdata.licenseRiskHighCount, 'progress-fill-high'));
    this.myJQuery(container).append(this.createHorizontalBar('mediumLicenseRiskLabel', 'Medium', 'filterTableByLicenseRisk', 'mediumLicenseRiskBar', this.rawdata.licenseRiskMediumCount, 'progress-fill-medium'));
    this.myJQuery(container).append(this.createHorizontalBar('lowLicenseRiskLabel', 'Low', 'filterTableByLicenseRisk', 'lowLicenseRiskBar', this.rawdata.licenseRiskLowCount, 'progress-fill-low'));
    this.myJQuery(container).append(this.createHorizontalBar('noneLicenseRiskLabel', 'None', 'filterTableByLicenseRisk', 'noneLicenseRiskBar', this.rawdata.licenseRiskNoneCount, 'progress-fill-none'));
    return container;
};

RiskReport.prototype.createOperationalRiskContainer = function () {
    var container = document.createElement("div");
    this.myJQuery(container).addClass("riskSummaryContainer horizontal rounded");
    var labelDiv = document.createElement("div");
    this.myJQuery(labelDiv).addClass("riskSummaryContainerLabel");
    this.myJQuery(labelDiv).text("Operational Risk").append('<i id="securityDescriptionIcon"'
        + 'class="fa fa-info-circle infoIcon"'
        + 'title="Calculated risk based on tracking overall open source software (OSS) component activity."></i>');
    this.myJQuery(container).append(labelDiv);

    this.myJQuery(container).append(this.createHorizontalBar('highOperationalRiskLabel', 'High', 'filterTableByOperationalRisk', 'highOperationalRiskBar', this.rawdata.operationalRiskHighCount, 'progress-fill-high'));
    this.myJQuery(container).append(this.createHorizontalBar('mediumOperationalRiskLabel', 'Medium', 'filterTableByOperationalRisk', 'mediumOperationalRiskBar', this.rawdata.operationalRiskMediumCount, 'progress-fill-medium'));
    this.myJQuery(container).append(this.createHorizontalBar('lowOperationalRiskLabel', 'Low', 'filterTableByOperationalRisk', 'lowOperationalRiskBar', this.rawdata.operationalRiskLowCount, 'progress-fill-low'));
    this.myJQuery(container).append(this.createHorizontalBar('noneOperationalRiskLabel', 'None', 'filterTableByOperationalRisk', 'noneOperationalRiskBar', this.rawdata.operationalRiskNoneCount, 'progress-fill-none'));
    return container;
};

RiskReport.prototype.createSummaryTable = function () {
    var table = document.createElement("table");
    this.myJQuery(table).addClass("table-summary horizontal")
    var tableBody = document.createElement("tbody");
    var tableRow = document.createElement("tr");
    var tableDataLabel = document.createElement("td");
    this.myJQuery(tableDataLabel).addClass("summaryLabel");
    this.myJQuery(tableDataLabel).css({"font-weight": "bold"});
    this.myJQuery(tableDataLabel).text("BOM Entries");
    var tableDataValue = document.createElement("td");
    this.myJQuery(tableDataValue).addClass("summaryLabel");
    this.myJQuery(tableDataValue).text(this.rawdata.totalComponents);

    this.myJQuery(tableRow).append(tableDataLabel);
    this.myJQuery(tableRow).append(tableDataValue);
    this.myJQuery(tableBody).append(tableRow);
    this.myJQuery(table).append(tableBody);

    return table;
};

RiskReport.prototype.columnClickEvent = function () {
    if (this.initSortTable == false) {
        console.log("init sorttable");
        sorttable.makeSortable(document.getElementById('blackDuckBomReport'));
    } else {
        console.log("sortable table inited");
    }
};

RiskReport.prototype.createComponentTableHead = function () {
    var compStyleClass = "clickable componentColumn columnLabel evenPadding";
    var licenseStyleClass = "clickable columnLabel evenPadding";
    var riskStyleClass = "clickable riskColumnLabel evenPadding";

    var tableHead = document.createElement("thead");
    var tableHeadRow = document.createElement("tr");
    this.myJQuery(tableHeadRow).append(document.createElement("th"));

    var columnHeadComponent = document.createElement("th");
    this.myJQuery(columnHeadComponent).addClass(compStyleClass);
    this.myJQuery(columnHeadComponent).text("Component");

    var columnHeadVersion = document.createElement("th");
    this.myJQuery(columnHeadVersion).addClass(compStyleClass);
    this.myJQuery(columnHeadVersion).text("Version");

    var columnHeadLicense = document.createElement("th");
    this.myJQuery(columnHeadLicense).addClass(licenseStyleClass);
    this.myJQuery(columnHeadLicense).text("License");

    var columnHeadEntryHigh = document.createElement("th");
    this.myJQuery(columnHeadEntryHigh).addClass(riskStyleClass);
    this.myJQuery(columnHeadEntryHigh).text("H");

    var columnHeadEntryMedium = document.createElement("th");
    this.myJQuery(columnHeadEntryMedium).addClass(riskStyleClass);
    this.myJQuery(columnHeadEntryMedium).text("M");

    var columnHeadEntryLow = document.createElement("th");
    this.myJQuery(columnHeadEntryLow).addClass(riskStyleClass);
    this.myJQuery(columnHeadEntryLow).text("L");

    var columnHeadLicenseRisk = document.createElement("th");
    this.myJQuery(columnHeadLicenseRisk).addClass(riskStyleClass);
    this.myJQuery(columnHeadLicenseRisk).attr("title", "License Risk");
    this.myJQuery(columnHeadLicenseRisk).text("Lic R");

    var columnHeadOperationRisk = document.createElement("th");
    this.myJQuery(columnHeadOperationRisk).addClass(riskStyleClass);
    this.myJQuery(columnHeadOperationRisk).attr("title", "Operational Risk");
    this.myJQuery(columnHeadOperationRisk).text("Opt R");

    this.myJQuery(tableHeadRow).append(columnHeadComponent);
    this.myJQuery(tableHeadRow).append(columnHeadVersion);
    this.myJQuery(tableHeadRow).append(columnHeadLicense);
    this.myJQuery(tableHeadRow).append(columnHeadEntryHigh);
    this.myJQuery(tableHeadRow).append(columnHeadEntryMedium);
    this.myJQuery(tableHeadRow).append(columnHeadEntryLow);
    this.myJQuery(tableHeadRow).append(columnHeadLicenseRisk);
    this.myJQuery(tableHeadRow).append(columnHeadOperationRisk);

    this.myJQuery(tableHead).append(tableHeadRow);
    return tableHead;
};

RiskReport.prototype.createComponentTableRow = function (entry) {
    var tableRow = document.createElement("tr");
    var columnApprovalStatus = document.createElement("td");
    this.myJQuery(columnApprovalStatus).addClass("evenPadding violation");
    this.myJQuery(columnApprovalStatus).append(this.myJQuery('<i class="fa fa-ban"></i>'));
    var approvalDiv = document.createElement("div");
    this.myJQuery(approvalDiv).text(entry.policyStatus);
    this.myJQuery(columnApprovalStatus).append(approvalDiv);

    var columnComponent = document.createElement("td");
    this.myJQuery(columnComponent).addClass("clickable componentColumn evenPadding");
    if (entry.componentURL) {
        this.myJQuery(columnComponent).attr("onclick", "window.open('" + entry.componentURL + "', '_blank');");
    }
    this.myJQuery(columnComponent).text(entry.componentName);

    var columnVersion = document.createElement("td");
    if (entry.componentVersion) {
        this.myJQuery(columnVersion).addClass("clickable componentColumn evenPadding");
        if (entry.componentVersionURL) {
            this.myJQuery(columnVersion).attr("onclick", "window.open('" + entry.componentVersionURL + "', '_blank');");
        }
        this.myJQuery(columnVersion).text(entry.componentVersion);
    } else {
        this.myJQuery(columnVersion).addClass("componentColumn evenPadding");
        this.myJQuery(columnVersion).text("?");
    }

    var columnLicense = document.createElement("td");
    this.myJQuery(columnLicense).addClass("licenseColumn evenPadding");
    if (entry.license) {
        this.myJQuery(columnLicense).attr("title", entry.license);
        this.myJQuery(columnLicense).text(entry.license);
    } else {
        this.myJQuery(columnLicense).attr("title", "Unknown License");
        this.myJQuery(columnLicense).text("Unknown License");
    }

    var columnHighRisk = document.createElement("td");
    this.myJQuery(columnHighRisk).addClass("riskColumn");
    var highRiskDiv = document.createElement("div");
    this.myJQuery(highRiskDiv).addClass("risk-span riskColumn risk-count");
    this.myJQuery(highRiskDiv).text(entry.securityRiskHighCount);
    this.myJQuery(columnHighRisk).append(highRiskDiv);

    var columnMediumRisk = document.createElement("td");
    this.myJQuery(columnMediumRisk).addClass("riskColumn");
    var mediumRiskDiv = document.createElement("div");
    this.myJQuery(mediumRiskDiv).addClass("risk-span riskColumn risk-count");
    this.myJQuery(mediumRiskDiv).text(entry.securityRiskMediumCount);
    this.myJQuery(columnMediumRisk).append(mediumRiskDiv);

    var columnLowRisk = document.createElement("td");
    this.myJQuery(columnLowRisk).addClass("riskColumn");
    var lowRiskDiv = document.createElement("div");
    this.myJQuery(lowRiskDiv).addClass("risk-span riskColumn risk-count");
    this.myJQuery(lowRiskDiv).text(entry.securityRiskLowCount);
    this.myJQuery(columnLowRisk).append(lowRiskDiv);

    var columnLicenseRisk = document.createElement("td");
    this.myJQuery(columnLicenseRisk).addClass("riskColumn");
    var licRiskDiv = document.createElement("div");
    this.myJQuery(licRiskDiv).addClass("risk-span riskColumn risk-count");
    this.myJQuery(licRiskDiv).text(this.createRiskString(entry.licenseRiskHighCount, entry.licenseRiskMediumCount, entry.licenseRiskLowCount));
    this.myJQuery(columnLicenseRisk).append(licRiskDiv);

    var columnOperationalRisk = document.createElement("td");

    this.myJQuery(columnOperationalRisk).addClass("riskColumn");
    var opRiskDiv = document.createElement("div");
    this.myJQuery(opRiskDiv).addClass("risk-span riskColumn risk-count");
    this.myJQuery(opRiskDiv).text(this.createRiskString(entry.operationalRiskHighCount, entry.operationalRiskMediumCount, entry.operationalRiskLowCount));
    this.myJQuery(columnOperationalRisk).append(opRiskDiv);

    this.myJQuery(tableRow).append(columnApprovalStatus);
    this.myJQuery(tableRow).append(columnComponent);
    this.myJQuery(tableRow).append(columnVersion);
    this.myJQuery(tableRow).append(columnLicense);
    this.myJQuery(tableRow).append(columnHighRisk);
    this.myJQuery(tableRow).append(columnMediumRisk);
    this.myJQuery(tableRow).append(columnLowRisk);
    this.myJQuery(tableRow).append(columnLicenseRisk);
    this.myJQuery(tableRow).append(columnOperationalRisk);

    return tableRow;
};

RiskReport.prototype.createComponentTable = function () {
    var table = document.createElement("table");
    this.myJQuery(table).attr("id", "blackDuckBomReport");
    this.myJQuery(table).addClass("table sortable");
    this.myJQuery(table).attr("onmouseenter", "initSortTable();");

    this.myJQuery(table).append(this.createComponentTableHead());
    var tableBody = document.createElement("tbody");
    this.myJQuery(tableBody).attr("id", "blackDuckBomReportBody");
    var entryArray = this.rawdata.components;
    var odd = true;
    for (var index = 0; index < entryArray.length; index++) {
        try {
            var tableRow = this.createComponentTableRow(entryArray[index]);
            adjustTableRow(tableRow, odd);
            adjustSecurityRisks(tableRow);
            adjustOtherRisks(tableRow, licenseRiskColumnNum);
            adjustOtherRisks(tableRow, operationRiskColumnNum);
            odd = !odd;
            this.myJQuery(tableBody).append(tableRow);
        } catch (ex) {
            console.log("Exception creating table row in Component Table" + ex);
        }
    }
    this.myJQuery(table).append(tableBody);
    return table;
};

RiskReport.prototype.createReport = function () {
    var report = document.createElement("div")
    this.myJQuery(report).addClass("riskReportBackgroundColor");
    this.myJQuery(report).append(this.createHeader());
    this.myJQuery(report).append(this.createVersionSummary());
    this.myJQuery(report).append(this.createSecurityRiskContainer());
    this.myJQuery(report).append(this.createLicenseRiskContainer());
    this.myJQuery(report).append(this.createOperationalRiskContainer());
    this.myJQuery(report).append(this.createSummaryTable());
    var table = this.createComponentTable();
    this.myJQuery(report).append(table);
    this.myJQuery("#riskReportDiv").html(this.myJQuery(report).html());
};