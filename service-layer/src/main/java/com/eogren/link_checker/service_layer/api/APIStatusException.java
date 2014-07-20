package com.eogren.link_checker.service_layer.api;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * APIStatusException can be thrown when an error should be returned to an API client
 */
public class APIStatusException extends WebApplicationException {
    protected Response response;

    /**
     * Create a new APIStatusException object.
     * @param status Body of the response that will be returned to the user
     * @param status_code HTTP status code to return
     */
    public APIStatusException(APIStatus status, int status_code) {
        Response.ResponseBuilder builder = Response.status(status_code);
        response = builder.entity(status).build();
    }

    /**
     * Returns the HTTP Response
     */
    @Override
    public Response getResponse() {
        return response;
    }
}
