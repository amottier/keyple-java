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
package org.eclipse.keyple.example.common.generic;

import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

/**
 * Create a new class extending AbstractSeSelectionRequest
 */
public class GenericSeSelectionRequest extends AbstractSeSelectionRequest {
    TransmissionMode transmissionMode;

    public GenericSeSelectionRequest(SeSelector seSelector, ChannelState channelState) {
        super(seSelector, channelState);
        transmissionMode = seSelector.getSeProtocol().getTransmissionMode();
    }

    @Override
    protected AbstractMatchingSe parse(SeResponse seResponse) {
        class GenericMatchingSe extends AbstractMatchingSe {
            public GenericMatchingSe(SeResponse selectionResponse,
                    TransmissionMode transmissionMode, String extraInfo) {
                super(selectionResponse, transmissionMode, extraInfo);
            }
        }
        return new GenericMatchingSe(seResponse, transmissionMode, "Generic Matching SE");
    }
}
