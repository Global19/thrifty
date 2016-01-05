/*
 * Copyright (C) 2015 Benjamin Bader
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bendb.thrifty.integration;

import com.bendb.thrifty.integration.gen.ThriftTestClient;
import com.bendb.thrifty.integration.gen.Xtruct;
import com.bendb.thrifty.integration.gen.Xtruct2;
import com.bendb.thrifty.protocol.BinaryProtocol;
import com.bendb.thrifty.service.ClientBase;
import com.bendb.thrifty.testing.TestServer;
import com.bendb.thrifty.transport.SocketTransport;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * A test of auto-generated service code for the standard ThriftTest
 * service.
 *
 * <p>Conformance is checked by roundtripping requests to a local server that
 * is run on the official Apache Thrift Java codebase.  The test server has
 * an implementation of ThriftTest methods with semantics as described in the
 * .thrift file itself and in the Apache Thrift git repo, along with Java code
 * generated by their compiler.
 *
 * <p>Meanwhile, the client used here is entirely generated by Thrifty.  We
 * currently test the BinaryProtocol with an unadorned (i.e. non-framed)
 * transport.
 */
public class Conformance {
    @Rule public TestServer testServer = new TestServer();

    private SocketTransport transport;
    private BinaryProtocol protocol;

    @Before
    public void setup() throws Exception {
        int port = testServer.port();
        transport = new SocketTransport.Builder("localhost", port)
                .readTimeout(2000)
                .build();

        transport.connect();
        protocol = new BinaryProtocol(transport);
    }

    @After
    public void teardown() throws Exception {
        if (protocol != null) {
            protocol.close();
            protocol = null;
        }

        if (transport != null) {
            transport.close();
            transport = null;
        }
    }

    @Test
    public void testStruct() throws Throwable {
        ThriftTestClient client = createClient();

        Xtruct xtruct = new Xtruct.Builder()
                .byte_thing((byte) 1)
                .i32_thing(2)
                .i64_thing(3L)
                .string_thing("foo")
                .build();

        AssertingCallback<Xtruct> callback = new AssertingCallback<>();
        client.testStruct(xtruct, callback);

        callback.await();
        assertThat(callback.getResult(), equalTo(xtruct));
    }

    @Test
    public void testNest() throws Throwable {
        ThriftTestClient client = createClient();

        Xtruct xtruct = new Xtruct.Builder()
                .byte_thing((byte) 1)
                .i32_thing(2)
                .i64_thing(3L)
                .string_thing("foo")
                .build();

        Xtruct2 nest = new Xtruct2.Builder()
                .byte_thing((byte) 4)
                .i32_thing(5)
                .struct_thing(xtruct)
                .build();

        AssertingCallback<Xtruct2> callback = new AssertingCallback<>();

        client.testNest(nest, callback);

        callback.await();

        assertThat(callback.getResult(), equalTo(nest));
    }

    private ThriftTestClient createClient() {
        return new ThriftTestClient(protocol, new ClientBase.Listener() {
            @Override
            public void onTransportClosed() {

            }

            @Override
            public void onError(Throwable error) {

            }
        });
    }
}
