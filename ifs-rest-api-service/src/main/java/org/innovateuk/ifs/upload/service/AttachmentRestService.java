package org.innovateuk.ifs.upload.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.threads.attachment.resource.AttachmentResource;
import org.springframework.core.io.ByteArrayResource;

import java.util.Optional;

public interface AttachmentRestService {
    RestResult<AttachmentResource> find(Long fileId);

    RestResult<AttachmentResource> upload(String contentType, long contentLength,
                                          String originalFilename, byte[] bytes);

    RestResult<Void> delete(Long id);

    RestResult<Optional<ByteArrayResource>> download(Long fileId);
}
