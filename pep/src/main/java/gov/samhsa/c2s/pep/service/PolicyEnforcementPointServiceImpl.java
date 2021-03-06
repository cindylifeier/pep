package gov.samhsa.c2s.pep.service;

import feign.FeignException;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.pep.infrastructure.ContextHandlerService;
import gov.samhsa.c2s.pep.infrastructure.DssService;
import gov.samhsa.c2s.pep.infrastructure.dto.DSSRequest;
import gov.samhsa.c2s.pep.infrastructure.dto.DSSResponse;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResponseDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResult;
import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseWithDocumentDto;
import gov.samhsa.c2s.pep.service.exception.DssClientInterfaceException;
import gov.samhsa.c2s.pep.service.exception.PepException;
import gov.samhsa.c2s.pep.service.exception.InvalidDocumentException;
import gov.samhsa.c2s.pep.service.exception.NoDocumentFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyEnforcementPointServiceImpl implements PolicyEnforcementPointService {

    private static final String PERMIT = "permit";
    private final Logger logger = LoggerFactory.getLogger(PolicyEnforcementPointServiceImpl.class);

    @Autowired
    private ContextHandlerService contextHandler;

    @Autowired
    private DssService dssService;

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        logger.info("Initiating PolicyEnforcementPointService.accessDocument flow");
        final XacmlRequestDto xacmlRequest = accessRequest.getXacmlRequest();
        logger.debug(xacmlRequest::toString);
        final XacmlResponseDto xacmlResponse = enforcePolicy(xacmlRequest);
        final XacmlResult xacmlResult = XacmlResult.from(xacmlRequest, xacmlResponse);
        logger.debug(xacmlResult::toString);

        assertPDPPermitDecision(xacmlResponse);

        if (accessRequest.getDocument().isPresent()) {
            final DSSRequest dssRequest = accessRequest.toDSSRequest(xacmlResult);
            logger.debug(dssRequest::toString);
            DSSResponse dssResponse;
            try {
                logger.debug("Invoking dss feign client - Start");
                dssResponse = dssService.segmentDocument(dssRequest);
                logger.debug("Invoking dss feign client - End");
            } catch (FeignException fe) {
                int causedByStatus = fe.status();

                switch (causedByStatus) {
                    case 400:
                        logger.error("DSS client returned a 400 - BAD REQUEST status, indicating invalid document was passed" +
                                " to DSS client", fe);
                        throw new InvalidDocumentException("Invalid document was passed to DSS client");
                    default:
                        logger.error("DSS client returned an unexpected instance of FeignException", fe);
                        throw new DssClientInterfaceException("An unknown error occurred while attempting to communicate " +
                                "with" +
                                " DSS service");
                }
            }
            logger.debug(dssResponse::toString);
            final AccessResponseDto accessResponse = AccessResponseWithDocumentDto.from(dssResponse, xacmlResponse);
            logger.debug(accessResponse::toString);
            logger.info("Completed PolicyEnforcementPointService.accessDocument flow, returning response");
            return accessResponse;
        } else {
            final AccessResponseDto accessResponse = AccessResponseDto.from(xacmlResponse);
            logger.debug(accessResponse::toString);
            return accessResponse;
        }
    }

    private void assertPDPPermitDecision(XacmlResponseDto xacmlResponse) {
        Optional.of(xacmlResponse)
                .map(XacmlResponseDto::getPdpDecision)
                .filter(PERMIT::equalsIgnoreCase)
                .orElseThrow(NoDocumentFoundException::new);
    }

    private XacmlResponseDto enforcePolicy(XacmlRequestDto xacmlRequest) {
        logger.debug("Invoking context-handler feign client - Start");
        XacmlResponseDto xacmlResponseDto;
        try {
            xacmlResponseDto = contextHandler.enforcePolicy(xacmlRequest);
        } catch (FeignException fe) {
            int causedByStatus = fe.status();

            switch (causedByStatus) {
                case 404:
                    logger.info("consent not found");
                    logger.debug(fe.getMessage(), fe);
                    throw new NoDocumentFoundException();
                default:
                    logger.error(fe.getMessage(), fe);
                    throw new PepException(fe);
            }
        }
        logger.debug(() -> "Invoking context-handler feign client - End" + xacmlResponseDto.toString());

        return xacmlResponseDto;
    }

}
