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
package org.eclipse.keyple.example.calypso.pc.UseCase_Calypso5_MultipleSession;


import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.MatchingSelection;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.example.common.calypso.pc.transaction.CalypsoUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 5’ – PO Multiple Sessions (PC/SC)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a Calypso PO
 * transaction in multiple mode including a number (N) of modification commands that exceed by one
 * command the PO modification buffer. (open and close a secure session performed with the debit
 * key).
 * <p>
 * Two sessions are performed:
 * <ul>
 * <li>A first session proceeds the N-1 first modification commands</li>
 * <li>A second session proceeds the last modification command</li>
 * </ul>
 * <p>
 * The SAM messages are handled transparently by the Calypso transaction API.</li>
 * <li>
 * <p>
 * <code>
 Explicit Selection
 </code> means that it is the terminal application which start the SE processing.</li>
 * <li>PO messages:
 * <ul>
 * <li>1 - SE message to explicitly select the application in the reader</li>
 * <li>2 - transaction SE message to operate the session opening in multiple mode</li>
 * <li>3 - transaction SE message to operate multiple updates of the same file (a first session
 * proceeding with the first modification commands is open and closed)</li>
 * <li>4 - transaction SE message to operate the closing opening</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class UseCase_Calypso5_MultipleSession_Pcsc {
    private static final Logger logger =
            LoggerFactory.getLogger(UseCase_Calypso5_MultipleSession_Pcsc.class);

    public static void main(String[] args) throws KeypleBaseException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.registerPlugin(new PcscPluginFactory());

        /*
         * Get a PO reader ready to work with Calypso PO. Use the getReader helper method from the
         * CalypsoUtilities class.
         */
        SeReader poReader = CalypsoUtilities.getDefaultPoReader();


        /*
         * Get a SAM reader ready to work with Calypso PO. Use the getReader helper method from the
         * CalypsoUtilities class.
         */
        SamResource samResource = CalypsoUtilities.getDefaultSamResource();

        /* Check if the readers exists */
        if (poReader == null || samResource == null) {
            throw new IllegalStateException("Bad PO or SAM reader setup");
        }

        logger.info("=============== UseCase Calypso #5: Po Authentication ==================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());
        logger.info("= SAM Reader  NAME = {}", samResource.getSeReader().getName());

        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= 1st PO exchange: AID based selection with reading of Environment file.         =");
            logger.info(
                    "==================================================================================");

            /*
             * Prepare a Calypso PO selection
             */
            SeSelection seSelection = new SeSelection();

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelectionRequest with all the desired attributes to
             * make the selection and read additional information afterwards
             */
            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(
                    new PoSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                            new PoSelector.PoAidSelector(
                                    new SeSelector.AidSelector.IsoAid(CalypsoClassicInfo.AID),
                                    PoSelector.InvalidatedPo.REJECT),
                            "AID: " + CalypsoClassicInfo.AID),
                    ChannelState.KEEP_OPEN);

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            seSelection.prepareSelection(poSelectionRequest);

            /*
             * Actual PO communication: operate through a single request the Calypso PO selection
             * and the file read
             */
            SelectionsResult selectionsResult = seSelection.processExplicitSelection(poReader);

            if (selectionsResult.hasActiveSelection()) {
                MatchingSelection matchingSelection = selectionsResult.getActiveSelection();

                CalypsoPo calypsoPo = (CalypsoPo) matchingSelection.getMatchingSe();
                logger.info("The selection of the PO has succeeded.");

                /* Go on with the reading of the first record of the EventLog file */
                logger.info(
                        "==================================================================================");
                logger.info(
                        "= 2nd PO exchange: open and close a secure session to perform authentication.    =");
                logger.info(
                        "==================================================================================");

                PoTransaction poTransaction = new PoTransaction(new PoResource(poReader, calypsoPo),
                        samResource, CalypsoUtilities.getSecuritySettings());

                /*
                 * Open Session for the debit key
                 */
                boolean poProcessStatus = poTransaction.processOpening(
                        PoTransaction.ModificationMode.MULTIPLE,
                        PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0, (byte) 0);

                if (!poProcessStatus) {
                    throw new IllegalStateException("processingOpening failure.");
                }

                if (!poTransaction.wasRatified()) {
                    logger.info(
                            "========= Previous Secure Session was not ratified. =====================");
                }
                /*
                 * Compute the number of append records (29 bytes) commands that will overflow the
                 * PO modifications buffer. Each append records will consume 35 (29 + 6) bytes in
                 * the buffer.
                 *
                 * We'll send one more command to demonstrate the MULTIPLE mode
                 */
                int modificationsBufferSize = calypsoPo.getModificationsCounter();

                int nbCommands = (modificationsBufferSize / 35) + 1;

                int appendRecordParsers[] = new int[nbCommands];

                logger.info(
                        "==== Send {} Append Record commands. Modifications buffer capacity = {} bytes i.e. {} 29-byte commands ====",
                        nbCommands, modificationsBufferSize, modificationsBufferSize / 35);

                for (int i = 0; i < nbCommands; i++) {
                    appendRecordParsers[i] =
                            poTransaction.prepareAppendRecordCmd(CalypsoClassicInfo.SFI_EventLog,
                                    ByteArrayUtil.fromHex(CalypsoClassicInfo.eventLog_dataFill),
                                    String.format("EventLog (SFI=%02X) #%d",
                                            CalypsoClassicInfo.SFI_EventLog, i));
                }

                /* proceed with the sending of commands, don't close the channel */
                poProcessStatus = poTransaction.processPoCommandsInSession();

                if (!poProcessStatus) {
                    for (int i = 0; i < nbCommands; i++) {
                        if (!poTransaction.getResponseParser(appendRecordParsers[i])
                                .isSuccessful()) {
                            logger.error("Append record #%d failed with errror %s.", i,
                                    poTransaction.getResponseParser(appendRecordParsers[i])
                                            .getStatusInformation());
                        }
                    }
                }

                /*
                 * Close the Secure Session.
                 */

                logger.info(
                        "========= PO Calypso session ======= Closing ============================");

                /*
                 * A ratification command will be sent (CONTACTLESS_MODE).
                 */
                poProcessStatus = poTransaction.processClosing(ChannelState.KEEP_OPEN);

                if (!poProcessStatus) {
                    throw new IllegalStateException("processClosing failure.");
                }

                logger.info(
                        "==================================================================================");
                logger.info(
                        "= End of the Calypso PO processing.                                              =");
                logger.info(
                        "==================================================================================");
            } else {
                logger.error("The selection of the PO has failed.");
            }
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
