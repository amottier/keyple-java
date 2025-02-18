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
package org.eclipse.keyple.plugin.android.omapi;


import static org.powermock.api.mockito.PowerMockito.when;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.Session;

@RunWith(PowerMockRunner.class)
public class AndroidOmapiReaderTest {

    final String PLUGIN_NAME = "AndroidOmapiPluginImpl";
    final String poAid = "A000000291A000000191";
    final String poAidResponse =
            "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000";

    Reader omapiReader;
    ProxyReader proxyReader;

    // init before each test
    @Before
    public void SetUp() throws Exception {
        // default reader connected with secure element with poAid
        omapiReader = mockReader();

        // instanciate proxyReader with omapiReader
        proxyReader = new AndroidOmapiReaderImpl(PLUGIN_NAME, omapiReader, omapiReader.getName());
    }


    /*
     * TEST READERS
     */

    @Test
    public void getInstance() throws Exception {
        Assert.assertNotNull(proxyReader);
    }

    @Test
    public void getName() throws Exception {
        Assert.assertEquals(omapiReader.getName(), proxyReader.getName());
    }

    @Test
    public void isSEPresent() throws NoStackTraceThrowable {
        Assert.assertEquals(true, proxyReader.isSePresent());
    }

    @Test
    public void getParameters() throws NoStackTraceThrowable {
        Assert.assertNotNull(proxyReader.getParameters());
    }

    @Test
    public void setParameters() throws KeypleBaseException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key1", "value1");
        proxyReader.setParameters(parameters);
        Assert.assertTrue(proxyReader.getParameters().size() == 1);
        Assert.assertTrue(proxyReader.getParameters().get("key1").equals("value1"));
    }

    @Test
    public void setParameter() throws KeypleBaseException {
        proxyReader.setParameter("key2", "value2");
        Assert.assertTrue(proxyReader.getParameters().size() == 1);
        Assert.assertTrue(proxyReader.getParameters().get("key2").equals("value2"));
    }

    /*
     * TRANSMIT
     */

    @Test
    public void transmitHoplinkSuccessfull() throws KeypleBaseException {
        // default init

        // test
        List<SeResponse> seResponseList = proxyReader.transmitSet(getCalypsoRequestSample());

        // assert
        Assert.assertTrue(
                seResponseList.get(0).getApduResponses().get(0).isSuccessful());

    }

    @Test
    public void transmitNoAid() throws KeypleBaseException, IOException {

        // init
        omapiReader = mockReaderWithNoAid();
        proxyReader = new AndroidOmapiReaderImpl(PLUGIN_NAME, omapiReader, omapiReader.getName());

        // test
        List<SeResponse> seResponseList = proxyReader.transmitSet(getCalypsoRequestSample());

        // assert
        Assert.assertNull(seResponseList.get(0));

    }

    @Test
    public void transmitWrongProtocol() throws KeypleBaseException {
        // init
        String poAid = "A000000291A000000191";
        ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoClass.ISO,
                (byte) 0x14,ReadDataStructure.SINGLE_RECORD_DATA,  (byte) 0x01, true, (byte) 0x20, "Hoplink EF T2Environment");
        List<ApduRequest> poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

        // wrong protocol
        SeRequest seRequest = new SeRequest(new SeSelector( SeCommonProtocols.PROTOCOL_MIFARE_UL, null,
                new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid(poAid),null),null), poApduRequestList,
                ChannelState.CLOSE_AFTER);

        // test
        Set<SeRequest> seRequestSet = new LinkedHashSet<SeRequest>();
        seRequestSet.add(seRequest);
        List<SeResponse> seResponseList = proxyReader.transmitSet(seRequestSet);

        // assert
        Assert.assertNull(seResponseList.get(0));

    }

    @Test(expected = KeypleReaderException.class)
    public void transmitNotConnected() throws KeypleBaseException, IOException {

        // init
        Reader omapiReader = Mockito.mock(Reader.class);
        when(omapiReader.getName()).thenReturn("SIM1");
        when(omapiReader.isSecureElementPresent()).thenReturn(false);
        when(omapiReader.openSession()).thenThrow(new IOException());
        proxyReader = new AndroidOmapiReaderImpl(PLUGIN_NAME, omapiReader, omapiReader.getName());

        // test
        List<SeResponse> seResponseList = proxyReader.transmitSet(getCalypsoRequestSample());

        // expected = KeypleReaderException.class
    }



    /*
     * HELPERS
     */


    Reader mockReader() throws IOException {

        Reader omapiReader = Mockito.mock(Reader.class);
        Session session = Mockito.mock(Session.class);
        Channel channel = Mockito.mock(Channel.class);

        when(omapiReader.getName()).thenReturn("SIM1");
        when(omapiReader.isSecureElementPresent()).thenReturn(true);
        when(session.openLogicalChannel(ByteArrayUtil.fromHex(poAid), (byte)0x00)).thenReturn(channel);
        when(omapiReader.openSession()).thenReturn(session);
        when(session.getATR()).thenReturn(null);
        when(channel.getSelectResponse()).thenReturn(ByteArrayUtil.fromHex(poAidResponse));
        when(channel.getSession()).thenReturn(session);

        when(channel.transmit(ByteArrayUtil.fromHex("00B201A420"))).thenReturn(ByteArrayUtil
                .fromHex("00000000000000000000000000000000000000000000000000000000000000009000"));

        return omapiReader;

    }

    Reader mockReaderWithNoAid() throws KeypleBaseException, IOException {

        Reader omapiReader = Mockito.mock(Reader.class);
        Session session = Mockito.mock(Session.class);
        Channel channel = Mockito.mock(Channel.class);

        when(omapiReader.getName()).thenReturn("SIM1");
        when(omapiReader.isSecureElementPresent()).thenReturn(true);
        when(omapiReader.openSession()).thenReturn(session);
        when(session.openLogicalChannel(ByteArrayUtil.fromHex(poAid), (byte)0x00))
                .thenThrow(new NoSuchElementException(""));

        return omapiReader;

    }

    Set<SeRequest> getCalypsoRequestSample() {
        String poAid = "A000000291A000000191";

        ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoClass.ISO,
                (byte) 0x14, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true, (byte) 0x20, "Hoplink EF T2Environment");

        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

        SeRequest seRequest = new SeRequest(new SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid(poAid), null), null), poApduRequestList,
                ChannelState.CLOSE_AFTER);

        Set<SeRequest> seRequestSet = new LinkedHashSet<SeRequest>();
        seRequestSet.add(seRequest);
        return seRequestSet;

    }


}
