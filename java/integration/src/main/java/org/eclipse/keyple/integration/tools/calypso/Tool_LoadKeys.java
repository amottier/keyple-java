/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.integration.tools.calypso;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.KeyReference;
import org.eclipse.keyple.calypso.command.po.builder.security.ChangeKeyCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.PoGetChallengeCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.security.PoGetChallengeRespPars;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.CardGenerateKeyCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.security.GiveRandomCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.security.SelectDiversifierCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.security.UnlockCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.CardGenerateKeyRespPars;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeProxyService;
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
import org.eclipse.keyple.integration.IntegrationUtils;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tool_LoadKeys {
    private static final Logger logger = LoggerFactory.getLogger(Tool_LoadKeys.class);

    /**
     * Load a key
     * 
     * @param poResource
     * @param samResource
     * @param keyIndex the key index (1, 2 or 3)
     * @param cipheringKeyReference if null the ciphering key is the null key
     * @param sourceKeyReference the reference of the key to be loaded
     * @return execution status of the change key command
     * @throws KeypleReaderException
     */
    private static boolean loadKey(PoResource poResource, SamResource samResource, int keyIndex,
            KeyReference cipheringKeyReference, KeyReference sourceKeyReference)
            throws KeypleReaderException {
        // create an apdu requests list to handle PO and SAM commands
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        // get the challenge from the PO
        apduRequests.add(new PoGetChallengeCmdBuild(poResource.getMatchingSe().getPoClass())
                .getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests, ChannelState.KEEP_OPEN);

        SeResponse seResponse = ((ProxyReader) poResource.getSeReader()).transmit(seRequest);

        if (seResponse == null || !seResponse.getApduResponses().get(0).isSuccessful()) {
            throw new IllegalStateException("PO get challenge command failed.");
        }

        PoGetChallengeRespPars poGetChallengeRespPars =
                new PoGetChallengeRespPars(seResponse.getApduResponses().get(0));
        byte[] poChallenge = poGetChallengeRespPars.getPoChallenge();

        // send diversifier, PO challenge and Card Generate key commands to the SAM (default
        // revision), get the ciphered data
        apduRequests.clear();

        apduRequests.add(new SelectDiversifierCmdBuild(SamRevision.C1,
                poResource.getMatchingSe().getApplicationSerialNumber()).getApduRequest());
        apduRequests.add(new GiveRandomCmdBuild(SamRevision.C1, poChallenge).getApduRequest());

        apduRequests.add(new CardGenerateKeyCmdBuild(SamRevision.C1, cipheringKeyReference,
                sourceKeyReference).getApduRequest());

        seResponse = ((ProxyReader) samResource.getSeReader()).transmit(seRequest);

        if (seResponse == null || !seResponse.getApduResponses().get(2).isSuccessful()) {
            throw new IllegalStateException("Card Generate Key command failed.");
        }

        CardGenerateKeyRespPars cardGenerateKeyRespPars =
                new CardGenerateKeyRespPars(seResponse.getApduResponses().get(2));
        byte[] cipheredData = cardGenerateKeyRespPars.getCipheredData();
        String keyInfo;
        if (cipheringKeyReference == null) {
            keyInfo = String.format("CIPHERING KEY: 00/00, SOURCE KEY: %02X/%02X",
                    sourceKeyReference.getKif(), sourceKeyReference.getKvc());
        } else {
            keyInfo = String.format("CIPHERING KEY: %02X/%02X, SOURCE KEY: %02X/%02X",
                    cipheringKeyReference.getKif(), cipheringKeyReference.getKvc(),
                    sourceKeyReference.getKif(), sourceKeyReference.getKvc());
        }
        logger.info("LOAD KEY {}, {}, CRYPTOGRAM: {}", keyIndex, keyInfo,
                ByteArrayUtil.toHex(cipheredData));

        // send change key command to the PO
        apduRequests.clear();

        apduRequests.add(new ChangeKeyCmdBuild(poResource.getMatchingSe().getPoClass(),
                (byte) keyIndex, cipheredData).getApduRequest());

        seResponse = ((ProxyReader) poResource.getSeReader()).transmit(seRequest);

        return (seResponse != null && seResponse.getApduResponses().get(0).isSuccessful()) ? true
                : false;
    }

    /**
     * Unlock the SAM
     * 
     * @param samResource
     * @param unlockData
     * @return true if the command is successful
     * @throws KeypleReaderException
     */
    private static boolean unlockSam(SamResource samResource, byte[] unlockData)
            throws KeypleReaderException {
        // create an apdu requests list to handle SAM command
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        // get the challenge from the PO
        apduRequests.add(new UnlockCmdBuild(SamRevision.C1, unlockData).getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests, ChannelState.KEEP_OPEN);

        SeResponse seResponse = ((ProxyReader) samResource.getSeReader()).transmit(seRequest);

        if (seResponse == null) {
            throw new IllegalStateException("Unlock SAM command command failed. Null response");
        }

        return seResponse.getApduResponses().get(0).isSuccessful();
    }

    /**
     * Main entry
     * 
     * @param args
     * @throws KeypleBaseException
     * @throws NoStackTraceThrowable
     */
    public static void main(String[] args) throws KeypleBaseException, NoStackTraceThrowable {
        // the unlocking data must be set to the expected value
        final String UNLOCK_DATA = "00112233445566778899AABBCCDDEEFF";

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.registerPlugin(new PcscPluginFactory());

        ProxyReader poReader = (ProxyReader) IntegrationUtils.getReader(seProxyService,
                IntegrationUtils.PO_READER_NAME_REGEX);

        poReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                PcscProtocolSetting.PCSC_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        ProxyReader samReader = (ProxyReader) IntegrationUtils.getReader(seProxyService,
                IntegrationUtils.SAM_READER_NAME_REGEX);

        samReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3,
                PcscProtocolSetting.PCSC_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO7816_3));

        /* Check if the readers exist */
        if (poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO/SAM reader setup");
        }

        logger.info("= PO Reader   NAME = {}", poReader.getName());
        logger.info("= SAM Reader  NAME = {}", samReader.getName());

        samReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the settings
        poReader.setSeProtocolSetting(PcscProtocolSetting.getAllSettings());

        // do the SAM selection to open the logical channel
        final String SAM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        SeSelection samSelection = new SeSelection();

        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(
                new SamSelector(SamRevision.C1, null, "SAM Selection"), ChannelState.KEEP_OPEN);

        /* Prepare selector, ignore AbstractMatchingSe here */
        samSelection.prepareSelection(samSelectionRequest);

        SelectionsResult samSelectionsResult;
        try {
            samSelectionsResult = samSelection.processExplicitSelection(samReader);
            if (!samSelectionsResult.hasActiveSelection()) {
                System.out.println("Unable to open a logical channel for SAM!");
                throw new IllegalStateException("SAM channel opening failure");
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());
        }

        CalypsoSam calypsoSam =
                (CalypsoSam) samSelectionsResult.getActiveSelection().getMatchingSe();

        SamResource samResource = new SamResource(samReader, calypsoSam);

        // Unlock the SAM before trying to load PO keys
        if (!unlockSam(samResource, ByteArrayUtil.fromHex(UNLOCK_DATA))) {
            throw new IllegalStateException("The unlock SAM command failed!");
        }

        // Check if a PO is present in the reader
        if (poReader.isSePresent()) {
            // do the PO selection (kif/kvc values below must be adapted accordingly)
            // byte[] aid = ByteArrayUtil.fromHex("D2760000850101"); // NFC NDEF
            // byte[] aid = ByteArrayUtil.fromHex("315449432E49434132"); // INTERCODE 2.2
            // byte[] aid = ByteArrayUtil.fromHex("304554502E494341"); // STORED VALUE
            byte[] aid = ByteArrayUtil.fromHex("315449432E49434131"); // CD LIGHT/GTML

            SeSelection seSelection = new SeSelection();

            seSelection.prepareSelection(new PoSelectionRequest(
                    new PoSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                            new PoSelector.PoAidSelector(new SeSelector.AidSelector.IsoAid(aid),
                                    null),

                            "Calypso Classic AID"),
                    ChannelState.KEEP_OPEN));

            SelectionsResult poSelectionsResult = seSelection.processExplicitSelection(poReader);

            if (poSelectionsResult == null || !poSelectionsResult.hasActiveSelection()) {
                throw new IllegalStateException("No recognizable PO detected.");
            }

            // the selection succeeded, get the CalypsoPo
            CalypsoPo calypsoPo =
                    (CalypsoPo) poSelectionsResult.getActiveSelection().getMatchingSe();

            PoResource poResource = new PoResource(poReader, calypsoPo);

            KeyReference nullKey = new KeyReference((byte) 0x00, (byte) 0x00);
            KeyReference key1 = new KeyReference((byte) 0x21, (byte) 0x79);
            KeyReference key2 = new KeyReference((byte) 0x27, (byte) 0x79);
            KeyReference key3 = new KeyReference((byte) 0x30, (byte) 0x79);

            logger.info("Revert keys to virgin state... (null key)");
            // load the null key index 3 ciphered by the key 1
            if (!loadKey(poResource, samResource, 3, key1, nullKey)) {
                throw new IllegalStateException(
                        "The loading of the null key #3 ciphered by the key #1 failed!");
            }
            // load the null key index 2 ciphered by the key 1
            if (!loadKey(poResource, samResource, 2, key1, nullKey)) {
                throw new IllegalStateException(
                        "The loading of the null key #2 ciphered by the key #1 failed!");
            }
            // load the null key index 1 ciphered by the key 1
            if (!loadKey(poResource, samResource, 1, key1, nullKey)) {
                throw new IllegalStateException(
                        "The loading of the null key #1 ciphered by the key #1 failed!");
            }

            logger.info("Load new keys...");
            // load key #1 ciphered by the null key
            if (!loadKey(poResource, samResource, 1, null, key1)) {
                throw new IllegalStateException(
                        "The loading of key #1 ciphered by the null key failed!");
            }

            // load key #2 ciphered by the key #1
            if (!loadKey(poResource, samResource, 2, key1, key2)) {
                throw new IllegalStateException(
                        "The loading of key #2 ciphered by the key #1 failed!");
            }

            // load key #3 ciphered by the key #1
            if (!loadKey(poResource, samResource, 3, key1, key3)) {
                throw new IllegalStateException(
                        "The loading of key #3 ciphered by the key #1 failed!");
            }

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= End of the Calypso PO key loading.                                                =");
            logger.info(
                    "==================================================================================");
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
