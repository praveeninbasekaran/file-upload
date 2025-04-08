@PUT
@Path("/alertAction/{alertId}/{action}/{bankId}/{assignedTo}/{assignedRole}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response alertAction(@PathParam("alertId") Long alertId,
                            @PathParam("action") String action,
                            @PathParam("bankId") String bankId,
                            @PathParam("assignedTo") String assignedTo,
                            @PathParam("assignedRole") String assignedRole,
                            @QueryParam("comments") String comments,
                            @QueryParam("riskManagementAction") String riskManagementAction) {
    try {
        alertActionService.performAction(alertId, action, bankId, assignedTo, assignedRole, comments, riskManagementAction);
        return Response.ok("Action [" + action + "] performed successfully on Alert ID: " + alertId).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("Error processing action [" + action + "] for alert ID " + alertId + ": " + e.getMessage())
                       .build();
    }
}