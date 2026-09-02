package com.pswied.loan.strasbourg.interfaces.rest.loanorigination;

import com.pswied.loan.strasbourg.application.loanorigination.LoanApplicationService;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionRequest;
import com.pswied.loan.strasbourg.domain.loanorigination.LoanApplicationSubmissionResult;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/loan-applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanApplicationResource {

    private final LoanApplicationService loanApplicationService;

    @Inject
    public LoanApplicationResource(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @POST
    public LoanApplicationSubmissionResult submit(@Valid LoanApplicationSubmissionRequest request) {
        return loanApplicationService.submit(request);
    }
}
