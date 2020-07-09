package uk.gov.companieshouse.api.strikeoffobjections.file;

import java.io.IOException;

@FunctionalInterface
public interface FileTransferOperation<T>  {
    T execute() throws IOException;
}
