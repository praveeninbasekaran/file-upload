package com.yourapp.controller;

import com.yourapp.dto.AlertActionRequest;
import com.yourapp.service.AlertActionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/alerts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlertActionController {

    @Inject
    AlertActionService alertActionService;

    @PUT
    @Path("/{alertId}/actions")
    public Response performAlertAction(
            @PathParam("alertId") Long alertId,
            @Valid AlertActionRequest request) {
        try {
            alertActionService.processAlertAction(alertId, request);
            return Response.ok("Action [" + request.getAction() + "] performed successfully on Alert ID: " + alertId).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid action: " + e.getMessage())
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Alert not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }
}

package com.yourapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AlertActionRequest {

    @NotBlank(message = "Action must not be blank")
    private String action;

    @NotBlank(message = "Bank ID must not be blank")
    private String bankId;

    @NotBlank(message = "Assigned To must not be blank")
    private String assignedTo;

    @NotBlank(message = "Assigned Role must not be blank")
    private String assignedRole;

    @NotBlank(message = "Comments must not be blank")
    private String comments;

    @NotBlank(message = "Risk Management Action must not be blank")
    private String riskManagementAction;

    // Getters and Setters
}

package com.yourapp.service;

import com.yourapp.dto.AlertActionRequest;
import com.yourapp.entity.Alert;
import com.yourapp.entity.AlertAction;
import com.yourapp.entity.AlertAudit;
import com.yourapp.entity.AlertStageDecision;
import com.yourapp.repository.AlertActionRepository;
import com.yourapp.repository.AlertAuditRepository;
import com.yourapp.repository.AlertRepository;
import com.yourapp.repository.AlertStageDecisionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;

@ApplicationScoped
public class AlertActionService {

    @Inject
    AlertRepository alertRepository;

    @Inject
    AlertActionRepository alertActionRepository;

    @Inject
    AlertStageDecisionRepository alertStageDecisionRepository;

    @Inject
    AlertAuditRepository alertAuditRepository;

    @Transactional
    public void processAlertAction(Long alertId, AlertActionRequest request) {
        Alert alert = alertRepository.findById(alertId);
        if (alert == null) {
            throw new NotFoundException("Alert ID " + alertId + " not found.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Update Alert based on action
        updateAlertBasedOnAction(alert, request.getAction(), request.getAssignedTo(), request.getAssignedRole(), now);

        // Persist Alert Action
        AlertAction alertAction = new AlertAction(alertId, request.getAction(), request.getAssignedTo(), request.getAssignedRole(), now);
        alertActionRepository.persist(alertAction);

        // Persist Alert Stage Decision
        AlertStageDecision stageDecision = new AlertStageDecision(alertId, request.getAssignedRole(), request.getAction(), now, request.getComments(), request.getRiskManagementAction());
        alertStageDecisionRepository.persist(stageDecision);

        // Persist Alert Audit
        AlertAudit alertAudit = new AlertAudit(alertId, request.getAction(), request.getAssignedTo(), request.getAssignedRole(), now, request.getComments());
        alertAuditRepository.persist(alertAudit);
    }

    private void updateAlertBasedOnAction(Alert alert, String action, String assignedTo, String assignedRole, LocalDateTime now) {
        alert.setLastActionTaken(action);
        alert.setLastActionDate(now);

        switch (action.toLowerCase()) {
            case "resolved":
            case "acknowledge":
                alert.setAssignedTo(null);
                alert.setAssignedRole(null);
                alert.setAlertStatus("Closed");
                break;

            case "review needed":
            case "aligned":
            case "not-aligned":
            case "refer back":
                alert.setAssignedTo(assignedTo);
                alert.setAssignedRole(assignedRole);
                alert.setAlertStatus("In Progress");
                alert.setDueDate(now.plusDays(fetchDueInDaysFromLibrary(alert.getRuleId())));
                break;

            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }

        alertRepository.persist(alert);
    }

    private int fetchDueInDaysFromLibrary(Long ruleId) {
        // Implement logic to fetch due days based on ruleId
        return 5; // Placeholder value
    }
}

package com.yourapp.repository;

import com.yourapp.entity.Alert;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertRepository implements PanacheRepository<Alert> {
    // Standard CRUD operations are available
}

package com.yourapp.repository;

import com.yourapp.entity.AlertAction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertActionRepository implements PanacheRepository<AlertAction> {
    // Standard CRUD operations are available
}

package com.yourapp.repository;

import com.yourapp.entity.AlertStageDecision;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertStageDecisionRepository implements PanacheRepository<AlertStageDecision> {
    // Standard CRUD operations are available
}

package com.yourapp.repository;

import com.yourapp.entity.AlertAudit;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertAuditRepository implements PanacheRepository<AlertAudit> {
    // Standard CRUD operations are available
}

@Transactional
public Response handleAlertAction(Long alertId, String action, String bankId,
                                  String assignedTo, String assignedRole,
                                  String comments, String riskManagementAction,
                                  String userRole, String userId, int stage) {

    if (Stream.of(alertId, action, bankId, comments, userRole, userId).anyMatch(StringUtils::isBlank)) {
        throw new BadRequestException("All mandatory fields must be provided.");
    }

    AlertData alertData = alertDataRepository.findById(alertId)
        .orElseThrow(() -> new NotFoundException("Alert not found for id: " + alertId));

    LocalDateTime currentDateTime = LocalDateTime.now();

    // Common fields for all actions
    alertData.setLastActionTaken(action);
    alertData.setLastActionDate(currentDateTime);

    // Initialize new action entity
    AlertAction alertAction = new AlertAction();
    alertAction.setAlertId(alertId);
    alertAction.setActionStage((long) stage);
    alertAction.setUserRole(userRole);
    alertAction.setUserId(userId);
    alertAction.setActionTaken(action);
    alertAction.setActionDate(currentDateTime);
    alertAction.setComments(comments);
    alertAction.setRuleManagementAction(riskManagementAction);

    // Initialize stage decision entity
    AlertStageDecision decision = new AlertStageDecision();
    decision.setAlertId(alertId);
    decision.setStage(stage);
    decision.setRole(userRole);
    decision.setDecision(action);
    decision.setActionDate(currentDateTime);
    decision.setFindings(comments);
    decision.setRiskManagementAction(riskManagementAction);

    // Audit trail entry
    AlertAuditHistory audit = new AlertAuditHistory();
    audit.setAlertId(alertId);
    audit.setActionStage(stage);
    audit.setUserRole(userRole);
    audit.setUserId(userId);
    audit.setActionTaken(action);
    audit.setActionDate(currentDateTime);
    audit.setComments(comments);
    audit.setRuleManagementAction(riskManagementAction);

    switch (action.toUpperCase()) {
        case "RESOLVED", "ACKNOWLEDGE" -> {
            alertData.setAlertStatus("Closed");
            alertData.setAssignedTo(null);
            alertData.setAssignedRole(null);
            alertAction.setAssignedToUser(null);
            alertAction.setAssignedToRole(null);
        }

        case "REVIEW NEEDED", "ALIGNED", "NOT-ALIGNED" -> {
            alertData.setAlertStatus("In Progress");
            alertData.setAssignedTo(assignedTo);
            alertData.setAssignedRole(assignedRole);
            alertData.setDueDate(LocalDateTime.now().plusDays(getDueInDays(alertData.getRuleId())));
            alertAction.setAssignedToUser(assignedTo);
            alertAction.setAssignedToRole(assignedRole);
        }

        case "REFER BACK" -> {
            alertData.setAlertStatus("In Progress");
            alertData.setAssignedTo(assignedTo);
            alertData.setAssignedRole(assignedRole);
            alertData.setDueDate(LocalDateTime.now().plusDays(getDueInDays(alertData.getRuleId())));
            alertAction.setAssignedToUser(assignedTo);
            alertAction.setAssignedToRole(assignedRole);
        }

        default -> throw new BadRequestException("Unsupported action: " + action);
    }

    // Save all
    alertDataRepository.persist(alertData);
    alertActionsRepository.persist(alertAction);
    alertStageDecisionRepository.persist(decision);
    alertAuditHistoryRepository.persist(audit);

    return Response.ok("Alert action processed successfully.").build();
}

// Mock implementation – replace with ruleService call
private int getDueInDays(Long ruleId) {
    return 7; // Placeholder logic
}

