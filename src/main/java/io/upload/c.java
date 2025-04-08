@Path("/alertAction")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlertActionController {

    @Inject
    AlertActionService alertActionService;

    @PUT
    @Path("/{alertId}/{action}/{bankId}/{assignedTo}/{assignedRole}")
    public Response performAlertAction(
            @PathParam("alertId") Long alertId,
            @PathParam("action") String action,
            @PathParam("bankId") String bankId,
            @PathParam("assignedTo") String assignedTo,
            @PathParam("assignedRole") String assignedRole,
            @QueryParam("comments") String comments,
            @QueryParam("riskManagementAction") String riskManagementAction
    ) {
        try {
            alertActionService.processAlertAction(alertId, action, bankId, assignedTo, assignedRole, comments, riskManagementAction);
            return Response.ok("Action '" + action + "' performed successfully.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred.").build();
        }
    }
}

@ApplicationScoped
public class AlertActionService {

    @Inject
    AlertActionRepository actionRepo;

    @Inject
    AlertDashboardRepository dashboardRepo;

    @Inject
    AlertStageDecisionRepository stageRepo;

    @Inject
    AlertAuditHistoryRepository auditRepo;

    public void processAlertAction(Long alertId, String action, String bankId, String assignedTo,
                                   String assignedRole, String comments, String riskManagementAction) {

        if (Stream.of(alertId, action, assignedTo, assignedRole, comments).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Missing required parameters.");
        }

        AlertData alert = dashboardRepo.findById(alertId);
        if (alert == null) throw new NotFoundException("Alert not found for ID: " + alertId);

        LocalDateTime now = LocalDateTime.now();

        alert.setLastActionTaken(action);
        alert.setLastActionDate(now);
        alert.setAlertStatus("Closed".equalsIgnoreCase(action) || "Acknowledge".equalsIgnoreCase(action) ? "Closed" : "In Progress");

        if ("Closed".equalsIgnoreCase(alert.getAlertStatus())) {
            alert.setAssignedRole(null);
            alert.setAssignedTo(null);
        } else {
            alert.setAssignedRole(assignedRole);
            alert.setAssignedTo(assignedTo);
        }

        // Update due date only for certain actions
        if (List.of("Review Needed", "Aligned", "Not-Aligned", "Refer Back").contains(action)) {
            alert.setDueDate(now.plusDays(30)); // replace 30 with dueInDays from rule config if needed
        }

        dashboardRepo.persist(alert);

        AlertAction alertAction = new AlertAction(alertId, 1L, assignedRole, assignedTo, action, now, comments, riskManagementAction);
        actionRepo.persist(alertAction);

        AlertStageDecision decision = new AlertStageDecision(alertId, assignedRole, action, now, comments, riskManagementAction);
        stageRepo.persist(decision);

        AlertAuditHistory audit = new AlertAuditHistory(alertId, 1, assignedRole, "user123", action, assignedRole, assignedTo, now, comments, riskManagementAction);
        auditRepo.persist(audit);
    }
}


@ApplicationScoped
public class AlertActionRepository implements PanacheRepository<AlertAction> {}

@ApplicationScoped
public class AlertDashboardRepository implements PanacheRepository<AlertData> {}

@ApplicationScoped
public class AlertStageDecisionRepository implements PanacheRepository<AlertStageDecision> {}

@ApplicationScoped
public class AlertAuditHistoryRepository implements PanacheRepository<AlertAuditHistory> {}