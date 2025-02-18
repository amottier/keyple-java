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
import org.eclipse.keyple.example.remote.transport.wspolling.client.WsPollingFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo Web Service with jdk http client library The master device uses the webservice client
 * whereas the slave device uses the webservice server
 */
@Deprecated
public class Demo_Webservice_MasterClient {

    public static void main(String[] args) throws Exception {


        final String CLIENT_NODE_ID = "Demo_Webservice_MasterClient1";
        final String SERVER_NODE_ID = "Demo_Webservice_MasterClientServer1";

        // Create a HTTP Web Polling factory
        TransportFactory factory = new WsPollingFactory(SERVER_NODE_ID);

        // Launch the Server thread
        // Server is slave
        Demo_Slave slave = new Demo_Slave(factory, true, factory.getServerNodeId(), CLIENT_NODE_ID);

        Thread.sleep(1000);

        // Launch the client
        // Client is Master
        Demo_Master master = new Demo_Master(factory, false, CLIENT_NODE_ID);
        master.boot();

        // execute Calypso Transaction Scenario
        slave.executeScenario(new StubCalypsoClassic(), true);

    }
}
