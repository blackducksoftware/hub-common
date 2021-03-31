/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
var filteredSecurityClassName = " rowFilteredSecurity";
var filteredLicenseClassName = " rowFilteredLicense";
var filteredOperationalClassName = " rowFilteredOperational";

var tableId = "blackDuckBomReport";


var policyViolationColumnNum = 0;
var highSecurityColumnNum = 4;
var mediumSecurityColumnNum = 5;
var lowSecurityColumnNum = 6;
var licenseRiskColumnNum = 7;
var operationRiskColumnNum = 8;

var sortTableInitialized = false;

function adjustWidth(object) {
    var percentageSpan = object.getElementsByTagName("SPAN")[0];
    var percent = percentageSpan.innerHTML;
    percentageSpan.style.display = "none";
    object.style.width = percent;
}

function adjustTable() {
    var riskReportTable = document.getElementById(tableId).tBodies[0];
    var odd = true;
    for (var i = 0; i < riskReportTable.rows.length; i++) {
        if (riskReportTable.rows[i].className
                .indexOf(filteredSecurityClassName) != -1) {
            continue;
        }
        if (riskReportTable.rows[i].className.indexOf(filteredLicenseClassName) != -1) {
            continue;
        }
        if (riskReportTable.rows[i].className
                .indexOf(filteredOperationalClassName) != -1) {
            continue;
        }
        adjustTableRow(riskReportTable.rows[i], odd);
        adjustSecurityRisks(riskReportTable.rows[i]);
        adjustOtherRisks(riskReportTable.rows[i], licenseRiskColumnNum);
        adjustOtherRisks(riskReportTable.rows[i], operationRiskColumnNum);
        odd = !odd;
    }
}

function adjustTableRow(row, odd) {
    var className = row.className;

    if (odd) {
        if (!className || className.length == 0) {
            className += "oddRow";
        } else {
            if (className.indexOf("evenRow") != -1) {
                className = className.replace("evenRow", "oddRow");
            }
        }
    } else {
        if (!className || className.length == 0) {
            className += "evenRow";
        } else {
            if (className.indexOf("oddRow") != -1) {
                className = className.replace("oddRow", "evenRow");
            }
        }
    }

    row.className = className;

    if (row.cells[policyViolationColumnNum].children.length > 1) {
        var violationStatus = row.cells[policyViolationColumnNum].children[1];
        if (violationStatus) {
            violationStatus = violationStatus.innerHTML;
        } else {
            violationStatus = "NOT_IN_VIOLATION";
        }

        var violationClasses = row.cells[policyViolationColumnNum].className;

        if (violationStatus == "IN_VIOLATION") {
            //Do nothing
        } else {
            row.cells[policyViolationColumnNum].removeChild(row.cells[policyViolationColumnNum].firstChild);
        }
        row.cells[policyViolationColumnNum].removeChild(row.cells[policyViolationColumnNum].lastChild);

        row.cells[policyViolationColumnNum].className = violationClasses;
    }
}

function adjustSecurityRisks(row) {
    if (row.cells[highSecurityColumnNum].children[0].innerHTML > 0) {
        if (row.cells[highSecurityColumnNum].children[0].className
                .indexOf("security-risk-high-count") == -1) {
            row.cells[highSecurityColumnNum].children[0].className += " security-risk-high-count";
        }
    }
    if (row.cells[mediumSecurityColumnNum].children[0].innerHTML > 0) {
        if (row.cells[mediumSecurityColumnNum].children[0].className
                .indexOf("security-risk-med-count") == -1) {
            row.cells[mediumSecurityColumnNum].children[0].className += " security-risk-med-count";
        }
    }
    if (row.cells[lowSecurityColumnNum].children[0].innerHTML > 0) {
        if (row.cells[lowSecurityColumnNum].children[0].className
                .indexOf("security-risk-low-count") == -1) {
            row.cells[lowSecurityColumnNum].children[0].className += " security-risk-low-count";
        }
    }
}

function adjustOtherRisks(row, riskColumnNum) {
    if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("H") != -1) {
        if (row.cells[riskColumnNum].children[0].className
                .indexOf("security-risk-high-count") == -1) {
            row.cells[riskColumnNum].children[0].className += " security-risk-high-count";
        }
    }
    if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("M") != -1) {
        if (row.cells[riskColumnNum].children[0].className
                .indexOf("security-risk-med-count") == -1) {
            row.cells[riskColumnNum].children[0].className += " security-risk-med-count";
        }
    }
    if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("L") != -1) {
        if (row.cells[riskColumnNum].children[0].className
                .indexOf("security-risk-low-count") == -1) {
            row.cells[riskColumnNum].children[0].className += " security-risk-low-count";
        }
    }
}

function filterTableByVulnerabilityRisk(risk) {
    filterTableByRisk(risk, 'highSecurityRiskLabel', 'mediumSecurityRiskLabel',
        'lowSecurityRiskLabel', 'noneSecurityRiskLabel',
        filteredSecurityClassName);
}

function filterTableByLicenseRisk(risk) {
    filterTableByRisk(risk, 'highLicenseRiskLabel', 'mediumLicenseRiskLabel',
        'lowLicenseRiskLabel', 'noneLicenseRiskLabel',
        filteredLicenseClassName);
}

function filterTableByOperationalRisk(risk) {
    filterTableByRisk(risk, 'highOperationalRiskLabel',
        'mediumOperationalRiskLabel', 'lowOperationalRiskLabel',
        'noneOperationalRiskLabel', filteredOperationalClassName);
}

function filterTableByRisk(risk, highRiskId, mediumRiskId, lowRiskId,
                           noneRiskId, filterClassName) {
    if (removeFilter(highRiskId, risk, filterClassName)) {
        return;
    }
    if (removeFilter(mediumRiskId, risk, filterClassName)) {
        return;
    }
    if (removeFilter(lowRiskId, risk, filterClassName)) {
        return;
    }
    if (removeFilter(noneRiskId, risk, filterClassName)) {
        return;
    }
    risk.className += " filterSelected";
    filterTable(document.getElementById(tableId).tBodies[0], risk, false,
        filterClassName);
    adjustTable();
}

function removeFilter(id, currRisk, filterClassName) {
    var riskLabel = document.getElementById(id);
    if (riskLabel.className.indexOf(" filterSelected") != -1) {
        filterTable(document.getElementById(tableId).tBodies[0], null, true,
            filterClassName);
        document.getElementById(id).className = document.getElementById(id).className
            .replace(' filterSelected', '');

        adjustTable();
        if (id == currRisk.id) {
            return true;
        }
    }

}

function filterTable(riskReportTable, riskToFilter, shouldRemoveFilter,
                     filterClassName) {
    var odd = true;
    for (var i = 0; i < riskReportTable.rows.length; i++) {
        if (shouldRemoveFilter) {
            removeFilterFromRow(riskReportTable.rows[i], filterClassName);
        } else {
            if (filterClassName == filteredSecurityClassName) {
                filterRowBySecurity(riskReportTable.rows[i], riskToFilter,
                    filterClassName);
            } else if (filterClassName == filteredLicenseClassName) {
                filterRowByOtherRisk(riskReportTable.rows[i], riskToFilter,
                    filterClassName, licenseRiskColumnNum);
            } else if (filterClassName == filteredOperationalClassName) {
                filterRowByOtherRisk(riskReportTable.rows[i], riskToFilter,
                    filterClassName, operationRiskColumnNum);
            }
        }
        adjustTableRow(riskReportTable.rows[i], odd);
        odd = !odd;
    }
}

function filterRowBySecurity(row, riskToFilter, filterClassName) {
    if (riskToFilter.id.indexOf("none") != -1) {
        // only show the rows that have no security risks
        if (row.cells[highSecurityColumnNum].children[0].innerHTML != 0
            || row.cells[mediumSecurityColumnNum].children[0].innerHTML != 0
            || row.cells[lowSecurityColumnNum].children[0].innerHTML != 0) {
            filterRowByRisk(row, filterClassName);
        }
    } else if (riskToFilter.id.indexOf("high") > -1) {
        // only show the rows that have high security risks
        if (row.cells[highSecurityColumnNum].children[0].innerHTML == 0) {
            filterRowByRisk(row, filterClassName);
        }
    } else if (riskToFilter.id.indexOf("medium") > -1) {
        // only show the rows that have medium security risks without high risks
        // if the component has a high security risk then it is not included in
        // the medium risk components
        if (row.cells[highSecurityColumnNum].children[0].innerHTML != 0
            || row.cells[mediumSecurityColumnNum].children[0].innerHTML == 0) {
            filterRowByRisk(row, filterClassName);
        }
    } else if (riskToFilter.id.indexOf("low") > -1) {
        // only show the rows that have low security risks without high or
        // medium risks
        // if the component has a high or medium security risk then it is not
        // included in the low risk components
        if ((row.cells[highSecurityColumnNum].children[0].innerHTML != 0 && row.cells[mediumSecurityColumnNum].children[0].innerHTML != 0)
            || row.cells[lowSecurityColumnNum].children[0].innerHTML == 0) {
            filterRowByRisk(row, filterClassName);
        }
    }
}

function filterRowByRisk(row, filterClassName) {
    if (row.className.indexOf(filterClassName) == -1) {
        row.className += filterClassName;
    }
}

function filterRowByOtherRisk(row, riskToFilter, filterClassName, riskColumnNum) {
    if (riskToFilter.id.indexOf("none") != -1) {
        if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("H") != -1) {
            if (row.className.indexOf(filterClassName) == -1) {
                row.className += filterClassName;
            }
        }
        if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("M") != -1) {
            if (row.className.indexOf(filterClassName) == -1) {
                row.className += filterClassName;
            }
        }
        if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("L") != -1) {
            if (row.className.indexOf(filterClassName) == -1) {
                row.className += filterClassName;
            }
        }
    } else if (riskToFilter.id.indexOf("high") > -1) {
        if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("H") == -1) {
            if (row.className.indexOf(filterClassName) == -1) {
                row.className += filterClassName;
            }
        }
    } else if (riskToFilter.id.indexOf("medium") > -1) {
        if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("M") == -1) {
            if (row.className.indexOf(filterClassName) == -1) {
                row.className += filterClassName;
            }
        }
    } else if (riskToFilter.id.indexOf("low") > -1) {
        if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("L") == -1) {
            if (row.className.indexOf(filterClassName) == -1) {
                row.className += filterClassName;
            }
        }
    }
}

function removeFilterFromRow(row, filterClassName) {
    if (row.className.indexOf(filterClassName) != -1) {
        row.className = row.className.replace(filterClassName, "");
    }
}

function initSortTable() {
    if (sortTableInitialized == false) {
        sortTableInitialized = true;
        console.log("initializing sort table");
        var table = document.getElementById(tableId);
        sorttable.makeSortable(document.getElementById(tableId));
        // this is needed to initialize the risk report on Bamboo
        // this should work on other browsers as well.  Bamboo doesn't fire a document onload or JQuery ready event.
        console.log("table sortable initialization performed removing event to prevent re-initialization");
        table.removeEventListener('mouseentered', initSortTable);
    }
}
