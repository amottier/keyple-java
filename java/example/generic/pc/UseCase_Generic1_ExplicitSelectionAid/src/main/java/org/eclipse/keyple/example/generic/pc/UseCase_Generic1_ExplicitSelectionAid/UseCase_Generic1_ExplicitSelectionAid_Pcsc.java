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
package org.eclipse.keyple.example.generic.pc.UseCase_Generic1_ExplicitSelectionAid;


import java.io.IOException;
import org.eclipse.keyple.core.selection.*;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.generic.GenericSeSelectionRequest;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h1>Use Case ‘generic 1’ – Explicit Selection Aid (PC/SC)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Check if a ISO 14443-4 SE is in the reader, select a SE (here a Calypso PO).</li>
 * <li><code>
 Explicit Selection
 </code> means that it is the terminal application which start the SE processing.</li>
 * <li>SE messages:
 * <ul>
 * <li>A single SE message to select the application in the reader</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class UseCase_Generic1_ExplicitSelectionAid_Pcsc {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Generic1_ExplicitSelectionAid_Pcsc.class);
    private static String seAid = "A0000004040125090101"; /* Here a Calypso AID */


    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.registerPlugin(new PcscPluginFactory());

        /*
         * Get a SE reader ready to work with generic SE. Use the getReader helper method from the
         * ReaderUtilities class.
         */
        SeReader seReader = ReaderUtilities.getDefaultContactLessSeReader();

        /* Check if the reader exists */
        if (seReader == null) {
            throw new IllegalStateException("Bad SE reader setup");
        }

        logger.info(
                "=============== UseCase Generic #1: AID based explicit selection ==================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        /* Check if a SE is present in the reader */
        if (seReader.isSePresent()) {

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= AID based selection.                                                           =");
            logger.info(
                    "==================================================================================");

            /*
             * Prepare the SE selection
             */
            SeSelection seSelection = new SeSelection();

            /*
             * Setting of an AID based selection (in this example a Calypso REV3 PO)
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Generic selection: configures a SeSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            GenericSeSelectionRequest genericSeSelectionRequest =
                    new GenericSeSelectionRequest(
                            new SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                                    new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid(
                                            ByteArrayUtil.fromHex(seAid)), null),
                                    "AID: " + seAid),
                            ChannelState.KEEP_OPEN);

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            seSelection.prepareSelection(genericSeSelectionRequest);

            /*
             * Actual SE communication: operate through a single request the SE selection
             */
            SelectionsResult selectionsResult = seSelection.processExplicitSelection(seReader);
            if (selectionsResult.hasActiveSelection()) {
                AbstractMatchingSe matchedSe =
                        selectionsResult.getActiveSelection().getMatchingSe();
                logger.info("The selection of the SE has succeeded.");
                logger.info("Application FCI = {}", matchedSe.getSelectionStatus().getFci());

                logger.info(
                        "==================================================================================");
                logger.info(
                        "= End of the generic SE processing.                                              =");
                logger.info(
                        "==================================================================================");
            } else {
                logger.error("The selection of the SE has failed.");
            }
        } else {
            logger.error("No SE were detected.");
        }
        System.exit(0);
    }
}
