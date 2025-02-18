/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.remote.application;

import org.eclipse.keyple.example.common.calypso.stub.StubCalypsoClassic;
import org.eclipse.keyple.example.remote.transport.wspolling.client_retrofit.WsPollingRetrofitFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo Web Service with Retrofit http client library (Android friendly) The master device uses the
 * websocket master whereas the slave device uses the websocket client
 */
public class Demo_WebserviceWithRetrofit_MasterServer {


    public static void main(String[] args) throws Exception {


        final String CLIENT_NODE_ID = "Demo_WebserviceWithRetrofit_MasterServerClient1";
        final String SERVER_NODE_ID = "Demo_WebserviceWithRetrofit_MasterServer1";


        // Create a HTTP Web Polling factory with retrofit Client
        TransportFactory factory = new WsPollingRetrofitFactory(SERVER_NODE_ID);


        // Launch the Server thread
        // Server is Master
        Demo_Master master = new Demo_Master(factory, true, null);
        master.boot();

        Thread.sleep(1000);

        // Launch the client thread
        // Client is Slave
        Demo_Slave slave = new Demo_Slave(factory, false, CLIENT_NODE_ID, SERVER_NODE_ID);

        // execute Calypso Transaction Scenario
        slave.executeScenario(new StubCalypsoClassic(), true);
    }
}
