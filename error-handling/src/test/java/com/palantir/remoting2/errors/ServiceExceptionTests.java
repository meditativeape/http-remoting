/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

package com.palantir.remoting2.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;

public final class ServiceExceptionTests {

    @Test
    public void testExceptionMessage() {
        String messageTemplate = "arg1={}, arg2={}, arg3={}";
        Param<?>[] args = {
                SafeParam.of("arg1", "foo"),
                UnsafeParam.of("arg2", 2),
                UnsafeParam.of("arg3", null)};

        String expectedMessage = "arg1=foo, arg2=2, arg3=null";

        ServiceException ex = new ServiceException(messageTemplate, args);

        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void testDefaultSerializableError() {
        ServiceException ex = new ServiceException("foo");
        SerializableError expected = SerializableError.of(
                "Refer to the server logs with this errorId: " + ex.getErrorId(),
                ServiceException.class);
        assertThat(ex.getError()).isEqualTo(expected);
    }

    @Test
    public void testDefaultSerializableErrorWithSubclass() {
        CustomServiceException ex = new CustomServiceException("foo");
        SerializableError expected = SerializableError.of(
                "Refer to the server logs with this errorId: " + ex.getErrorId(),
                CustomServiceException.class);
        assertThat(ex.getError()).isEqualTo(expected);
    }

    @Test
    public void testExceptionCause() {
        Throwable cause = new RuntimeException("foo");
        ServiceException ex = new ServiceException(cause, "");

        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    public void testStatus() {
        int status = 399;
        ServiceException ex = new ServiceException(status, "");

        assertThat(ex.getStatus()).isEqualTo(status);
    }

    @Test
    public void testDefaultStatus() {
        ServiceException ex = new ServiceException("");

        assertThat(ex.getStatus()).isEqualTo(500);
    }

    @Test
    public void testErrorIdsAreUnique() {
        UUID errorId1 = UUID.fromString(new ServiceException("").getErrorId());
        UUID errorId2 = UUID.fromString(new ServiceException("").getErrorId());

        assertThat(errorId1).isNotEqualTo(errorId2);
    }

    private static class CustomServiceException extends ServiceException {

        CustomServiceException(String messageFormat, Param<?>... messageParams) {
            super(messageFormat, messageParams);
        }

    }

}