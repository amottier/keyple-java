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
package org.eclipse.keyple.example.common;

import java.util.regex.Pattern;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;

public class ReaderUtilities {
    /**
     * Get the terminal which names match the expected pattern
     *
     * @param pattern Pattern
     * @return SeReader
     * @throws KeypleReaderException Readers are not initialized
     */
    public static SeReader getReaderByName(String pattern) throws KeypleReaderException {
        Pattern p = Pattern.compile(pattern);
        for (ReaderPlugin plugin : SeProxyService.getInstance().getPlugins()) {
            for (SeReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        throw new KeypleReaderNotFoundException("Reader name pattern: " + pattern);
    }

    /**
     * Get a fully configured contactless proxy reader
     * 
     * @return the targeted SeReader to do contactless communications
     * @throws KeypleBaseException in case of an error while retrieving the reader or setting its
     *         parameters
     */
    public static SeReader getDefaultContactLessSeReader() throws KeypleBaseException {
        SeReader seReader =
                ReaderUtilities.getReaderByName(PcscReadersSettings.PO_READER_NAME_REGEX);

        ReaderUtilities.setContactlessSettings(seReader);

        return seReader;
    }

    /**
     * Sets the reader parameters for contactless secure elements
     * 
     * @param reader the reader to configure
     * @throws KeypleBaseException in case of an error while settings the parameters
     */
    public static void setContactlessSettings(SeReader reader) throws KeypleBaseException {
        /* Enable logging */
        reader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");

        /* Contactless SE works with T1 protocol */
        reader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);

        /*
         * PC/SC card access mode:
         *
         * The SAM is left in the SHARED mode (by default) to avoid automatic resets due to the
         * limited time between two consecutive exchanges granted by Windows.
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
         * that may result in session failures.
         *
         * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
         */
        reader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the PO reader protocol flag */
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                PcscProtocolSetting.PCSC_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

    }

    /**
     * Sets the reader parameters for contacts secure elements
     *
     * @param reader the reader to configure
     * @throws KeypleBaseException in case of an error while settings the parameters
     */
    public static void setContactsSettings(SeReader reader) throws KeypleBaseException {
        /* Enable logging */
        reader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");

        /* Contactless SE works with T0 protocol */
        reader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        /*
         * PC/SC card access mode:
         *
         * The SAM is left in the SHARED mode (by default) to avoid automatic resets due to the
         * limited time between two consecutive exchanges granted by Windows.
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
         * that may result in session failures.
         *
         * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
         */
        reader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the SAM reader protocol flag */
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3,
                PcscProtocolSetting.PCSC_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO7816_3));
    }
}
