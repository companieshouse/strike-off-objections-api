package uk.gov.companieshouse.api.strikeoffobjections.file;

import java.io.IOException;

@FunctionalInterface
public interface FileTransferResponseBuilder<T> {
    FileTransferApiClientResponse createResponse(T input) throws IOException;
}
