/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.util.RequestUtils;
import org.eclipse.keyple.command.CommandsTable;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.util.ByteBufferUtils;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the Close Secure Session APDU command.
 *
 * @author Ixxi
 */
public class CloseSessionCmdBuild extends AbstractPoCommandBuilder {

    /** The command. */
    private final static CommandsTable command = CalypsoPoCommands.CLOSE_SESSION;

    /**
     * Instantiates a new CloseSessionCmdBuild depending of the revision of the PO.
     *
     * @param revision of the PO
     * @param ratificationAsked the ratification asked
     * @param terminalSessionSignature the sam half session signature
     * @throws java.lang.IllegalArgumentException - if the signature is null or has a wrong length
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public CloseSessionCmdBuild(PoRevision revision, boolean ratificationAsked,
            ByteBuffer terminalSessionSignature) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        // The optional parameter terminalSessionSignature could contain 4 or 8
        // bytes.
        if (terminalSessionSignature != null && terminalSessionSignature.limit() != 4
                && terminalSessionSignature.limit() != 8) {
            throw new IllegalArgumentException("Invalid terminal sessionSignature: "
                    + ByteBufferUtils.toHex(terminalSessionSignature));
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = ratificationAsked ? (byte) 0x80 : (byte) 0x00;

        request = RequestUtils.constructAPDURequest(cla, command, p1, (byte) 0x00,
                terminalSessionSignature);
    }

    /**
     * Instantiates a new close session cmd build.
     *
     * @param request the request
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public CloseSessionCmdBuild(ApduRequest request) throws IllegalArgumentException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}