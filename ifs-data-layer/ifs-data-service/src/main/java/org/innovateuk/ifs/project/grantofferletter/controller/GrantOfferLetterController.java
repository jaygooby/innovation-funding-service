package org.innovateuk.ifs.project.grantofferletter.controller;

import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FilesizeAndTypeFileValidator;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterApprovalResource;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterState;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterStateResource;
import org.innovateuk.ifs.project.grantofferletter.transactional.GrantOfferLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static org.innovateuk.ifs.file.controller.FileControllerUtils.*;

/**
 * Project Controller extension for grant offer data and operations through a REST API
 **/
@RestController
@RequestMapping("/project")
public class GrantOfferLetterController {

    @Value("${ifs.data.service.file.storage.projectsetupgrantofferletter.max.filesize.bytes}")
    private Long maxFilesizeBytesForProjectSetupGrantOfferLetter;

    @Value("${ifs.data.service.file.storage.projectsetupgrantofferletter.valid.media.types}")
    private List<String> validMediaTypesForProjectSetupGrantOfferLetter;

    @Autowired
    private GrantOfferLetterService grantOfferLetterService;

    @Autowired
    @Qualifier("mediaTypeStringsFileValidator")
    private FilesizeAndTypeFileValidator<List<String>> fileValidator;

    @GetMapping("/{projectId}/signed-grant-offer")
    public @ResponseBody ResponseEntity<Object> getGrantOfferLetterFileContents(
            @PathVariable("projectId") long projectId) throws IOException {
        return handleFileDownload(() -> grantOfferLetterService.getSignedGrantOfferLetterFileAndContents(projectId));
    }

    @GetMapping("/{projectId}/grant-offer")
    public @ResponseBody ResponseEntity<Object> getGeneratedGrantOfferLetterFileContents(
            @PathVariable("projectId") long projectId) throws IOException {
        return handleFileDownload(() -> grantOfferLetterService.getGrantOfferLetterFileAndContents(projectId));
    }


    @GetMapping("/{projectId}/additional-contract")
    public @ResponseBody ResponseEntity<Object> getAdditionalContractFileContents(
            @PathVariable("projectId") long projectId) throws IOException {
        return handleFileDownload(() -> grantOfferLetterService.getAdditionalContractFileAndContents(projectId));
    }


    @GetMapping(value = "/{projectId}/signed-grant-offer/details", produces = "application/json")
    public RestResult<FileEntryResource> getSignedGrantOfferLetterFileEntryDetails(
            @PathVariable("projectId") long projectId) throws IOException {

        return grantOfferLetterService.getSignedGrantOfferLetterFileEntryDetails(projectId).toGetResponse();
    }

    @GetMapping(value = "/{projectId}/grant-offer/details", produces = "application/json")
    public RestResult<FileEntryResource> getGrantOfferLetterFileEntryDetails(
            @PathVariable("projectId") long projectId) throws IOException {

        return grantOfferLetterService.getGrantOfferLetterFileEntryDetails(projectId).toGetResponse();
    }


    @GetMapping(value = "/{projectId}/additional-contract/details", produces = "application/json")
    public RestResult<FileEntryResource> getAdditionalContractFileEntryDetails(
            @PathVariable("projectId") long projectId) throws IOException {

        return grantOfferLetterService.getAdditionalContractFileEntryDetails(projectId).toGetResponse();
    }


    @PostMapping(value = "/{projectId}/signed-grant-offer", produces = "application/json")
    public RestResult<FileEntryResource> addSignedGrantOfferLetterFile(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return handleFileUpload(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupGrantOfferLetter, maxFilesizeBytesForProjectSetupGrantOfferLetter, request, (fileAttributes, inputStreamSupplier) ->
                grantOfferLetterService.createSignedGrantOfferLetterFileEntry(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier)
        );
    }

    @PostMapping(value = "/{projectId}/grant-offer", produces = "application/json")
    public RestResult<FileEntryResource> addGrantOfferLetterFile(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return handleFileUpload(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupGrantOfferLetter, maxFilesizeBytesForProjectSetupGrantOfferLetter, request, (fileAttributes, inputStreamSupplier) ->
                grantOfferLetterService.createGrantOfferLetterFileEntry(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier)
        );
    }

    @DeleteMapping(value = "/{projectId}/grant-offer", produces = "application/json")
    public RestResult<Void> removeGrantOfferLetterFile(@PathVariable(value = "projectId") long projectId) {

        return grantOfferLetterService.removeGrantOfferLetterFileEntry(projectId).toDeleteResponse();
    }

    @DeleteMapping(value = "/{projectId}/signed-grant-offer-letter", produces = "application/json")
    public RestResult<Void> removeSignedGrantOfferLetterFile(@PathVariable(value = "projectId") long projectId) {

        return grantOfferLetterService.removeSignedGrantOfferLetterFileEntry(projectId).toDeleteResponse();
    }

    @PostMapping(value = "/{projectId}/additional-contract", produces = "application/json")
    public RestResult<FileEntryResource> addAdditionalContractFile(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return handleFileUpload(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupGrantOfferLetter, maxFilesizeBytesForProjectSetupGrantOfferLetter, request, (fileAttributes, inputStreamSupplier) ->
                grantOfferLetterService.createAdditionalContractFileEntry(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier)
        );
    }

    @PutMapping(value = "/{projectId}/signed-grant-offer", produces = "application/json")
    public RestResult<Void> updateGrantOfferLetterFile(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return handleFileUpdate(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupGrantOfferLetter, maxFilesizeBytesForProjectSetupGrantOfferLetter, request, (fileAttributes, inputStreamSupplier) ->
                grantOfferLetterService.updateSignedGrantOfferLetterFile(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier));
    }

    @PostMapping("/{projectId}/grant-offer/submit")
    public RestResult<Void> submitGrantOfferLetter(@PathVariable("projectId") long projectId) {
        return grantOfferLetterService.submitGrantOfferLetter(projectId).toPostResponse();
    }

    @ZeroDowntime(reference = "IFS-2579", description = "Remove in Sprint 19 - replaced with usage of getGrantOfferLetterState()")
    @GetMapping("/{projectId}/is-send-grant-offer-letter-allowed")
    public RestResult<Boolean> isSendGrantOfferLetterAllowed(@PathVariable("projectId") final Long projectId) {
        return grantOfferLetterService.isSendGrantOfferLetterAllowed(projectId).toGetResponse();
    }

    @PostMapping("/{projectId}/grant-offer/send")
    public RestResult<Void> sendGrantOfferLetter(@PathVariable("projectId") final Long projectId) {
        return grantOfferLetterService.sendGrantOfferLetter(projectId).toPostResponse();
    }

    @ZeroDowntime(reference = "IFS-2579", description = "Remove in Sprint 19 - replaced with usage of getGrantOfferLetterState()")
    @GetMapping("/{projectId}/is-grant-offer-letter-already-sent")
    public RestResult<Boolean> isGrantOfferLetterAlreadySent(@PathVariable("projectId") final Long projectId) {
        return grantOfferLetterService.isGrantOfferLetterAlreadySent(projectId).toGetResponse();
    }

    @PostMapping("/{projectId}/signed-grant-offer-letter/approval")
    public RestResult<Void> approveOrRejectSignedGrantOfferLetter(@PathVariable("projectId") final Long projectId,
                                                                  @RequestBody final GrantOfferLetterApprovalResource grantOfferLetterApprovalResource) {
        return grantOfferLetterService.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource).toPostResponse();
    }

    @ZeroDowntime(reference = "IFS-2579", description = "Remove in Sprint 19 - replaced with usage of getGrantOfferLetterState()")
    @GetMapping("/{projectId}/signed-grant-offer-letter/approval")
    public RestResult<Boolean> isSignedGrantOfferLetterApproved(@PathVariable("projectId") final Long projectId) {
        return grantOfferLetterService.isSignedGrantOfferLetterApproved(projectId).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-2579", description = "Remove in Sprint 19 - replaced with usage of getGrantOfferLetterState()")
    @GetMapping("/{projectId}/signed-grant-offer-letter/is-rejected")
    public RestResult<Boolean> isSignedGrantOfferLetterRejected(@PathVariable("projectId") final Long projectId) {
        return grantOfferLetterService.isSignedGrantOfferLetterRejected(projectId).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-2579", description = "Remove in Sprint 19 - replaced with usage of getGrantOfferLetterState()")
    @GetMapping("/{projectId}/grant-offer-letter/state")
    public RestResult<GrantOfferLetterState> getGrantOfferLetterWorkflowState(@PathVariable("projectId") final Long projectId) {
        return grantOfferLetterService.getGrantOfferLetterWorkflowState(projectId).toGetResponse();
    }

    @GetMapping("/{projectId}/grant-offer-letter/current-state")
    public RestResult<GrantOfferLetterStateResource> getGrantOfferLetterState(@PathVariable("projectId") final Long projectId) {
        return grantOfferLetterService.getGrantOfferLetterState(projectId).toGetResponse();
    }
}
