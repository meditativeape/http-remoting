/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.remoting3.servers.jersey;

import com.palantir.logsafe.SafeArg;
import com.palantir.remoting.api.errors.ErrorType;
import java.util.UUID;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Services should throw {@link com.palantir.remoting.api.errors.ServiceException}s instead.
 */
@Provider
@Deprecated
final class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger log = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        log.warn(this.getClass().getSimpleName() + " is deprecated. Servers should throw ServiceExceptions instead.");

        String errorInstanceId = UUID.randomUUID().toString();
        Response.Status.Family family = Response.Status.Family.familyOf(exception.getResponse().getStatus());
        if (family == Response.Status.Family.CLIENT_ERROR) {
            log.info("Error handling request {}", SafeArg.of("errorInstanceId", errorInstanceId), exception);
        } else {
            log.error("Error handling request {}", SafeArg.of("errorInstanceId", errorInstanceId), exception);
        }

        if (exception instanceof ForbiddenException) {
            return JsonExceptionMapper.createResponse(
                    ErrorType.PERMISSION_DENIED, errorInstanceId, exception.getClass().getName());
        } else if (exception instanceof NotFoundException) {
            return JsonExceptionMapper.createResponse(
                    ErrorType.NOT_FOUND, errorInstanceId, exception.getClass().getName());
        } else if (exception instanceof BadRequestException || exception instanceof ParamException) {
            return JsonExceptionMapper.createResponse(
                    ErrorType.INVALID_ARGUMENT, errorInstanceId, exception.getClass().getName());
        } else {
            return JsonExceptionMapper.createResponse(
                    exception.getResponse().getStatus(), exception.getClass().getName(),
                    exception.getClass().getSimpleName(), errorInstanceId, exception.getClass().getName());
        }
    }
}
