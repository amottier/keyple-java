/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Single APDU request wrapper
 */
public final class ApduRequest extends AbstractApduBuffer {

    /**
     * a ‘case 4’ flag in order to explicitly specify, if it’s expected that the APDU command
     * returns data → this flag is required to manage revision 2.4 Calypso Portable Objects and
     * ‘S1Dx’ SAMs that presents a behaviour not compliant with ISO 7816-3 in contacts mode (not
     * returning the 61XYh status).
     */
    private boolean case4;

    /**
     * List of status codes that should be considered successful although they are different from
     * 9000
     */
    private Set<Short> successfulStatusCodes = new LinkedHashSet<Short>();

    /**
     * Name of the request being sent
     */
    private String name;

    /**
     * the constructor called by a ticketing application in order to build the APDU command requests
     * to push to the ProxyReader.
     *
     * @param buffer Buffer of the APDU request
     * @param case4 the case 4
     * @param successfulStatusCodes the list of status codes to be considered as successful although
     *        different from 9000
     */
    public ApduRequest(ByteBuffer buffer, boolean case4, Set<Short> successfulStatusCodes) {
        super(buffer);
        this.case4 = case4;
        this.successfulStatusCodes = successfulStatusCodes;
    }

    /**
     * Alternate constructor without status codes list
     * 
     * @param buffer
     * @param case4
     */
    public ApduRequest(ByteBuffer buffer, boolean case4) {
        this(buffer, case4, null);
    }

    /**
     * Checks if is case 4.
     *
     * @return the case4 flag.
     */
    public boolean isCase4() {
        return case4;
    }


    /**
     * Name this APDU request
     * 
     * @param name Name of the APDU request
     * @return Name of the APDU request
     */
    public ApduRequest setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the list of successful status codes for the request
     * 
     * @return the list of status codes
     */
    public Set<Short> getSuccessfulStatusCodes() {
        return successfulStatusCodes;
    }

    /**
     * Get the name of this APDU request
     * 
     * @return Name of the APDU request
     */
    public String getName() {
        return name;
    }
}