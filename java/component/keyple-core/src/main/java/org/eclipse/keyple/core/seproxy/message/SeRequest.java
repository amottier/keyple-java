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
package org.eclipse.keyple.core.seproxy.message;

import java.io.Serializable;
import java.util.List;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeSelector;

/**
 * List of APDU requests that will result in a {@link SeResponse}
 * 
 * @see SeResponse
 */
public final class SeRequest implements Serializable {

    static final long serialVersionUID = 6018469841127325812L;

    /**
     * SE seSelector is either an AID or an ATR regular expression
     */
    private final SeSelector seSelector;

    /**
     * contains a group of APDUCommand to operate on the selected SE application by the SE reader.
     */
    private List<ApduRequest> apduRequests;


    /**
     * the final logical channel status: the SE reader may kept active the logical channel of the SE
     * application after processing the group of APDU commands otherwise the SE reader will close
     * the logical channel of the SE application after processing the group of APDU commands (i.e.
     * after the receipt of the last APDU response).
     */
    private ChannelState channelState;


    /**
     * The constructor called by a ProxyReader in order to open a logical channel, to send a set of
     * APDU commands to a SE application, or both of them.
     *
     * @param seSelector the SeSelector containing the selection information to process the SE
     *        selection
     * @param apduRequests a optional list of {@link ApduRequest} to execute after a successful
     *        selection process
     * @param channelState the channel management parameter allowing to close or keep the channel
     *        open after the request execution
     */
    public SeRequest(SeSelector seSelector, List<ApduRequest> apduRequests,
            ChannelState channelState) {
        this.seSelector = seSelector;
        this.apduRequests = apduRequests;
        this.channelState = channelState;
    }

    /**
     * Constructor to be used when the SE is already selected (without {@link SeSelector})
     * 
     * @param apduRequests a list of ApudRequest
     * @param channelState a flag to tell if the channel has to be closed at the end
     */
    public SeRequest(List<ApduRequest> apduRequests, ChannelState channelState) {
        this.seSelector = null;
        this.apduRequests = apduRequests;
        this.channelState = channelState;
    }


    /**
     * Gets the SE seSelector.
     *
     * @return the current SE seSelector
     */
    public SeSelector getSeSelector() {
        return seSelector;
    }

    /**
     * Gets the apdu requests.
     *
     * @return the group of APDUs to be transmitted to the SE application for this instance of
     *         SERequest.
     */
    public List<ApduRequest> getApduRequests() {
        return apduRequests;
    }

    /**
     * Define if the channel should be kept open after the {@link SeRequest} has been executed.
     *
     * @return If the channel should be kept open
     */
    public boolean isKeepChannelOpen() {
        return channelState == ChannelState.KEEP_OPEN;
    }

    @Override
    public String toString() {
        return String.format("SeRequest:{REQUESTS = %s, SELECTOR = %s, KEEPCHANNELOPEN = %s}",
                getApduRequests(), getSeSelector(), channelState);
    }
}
