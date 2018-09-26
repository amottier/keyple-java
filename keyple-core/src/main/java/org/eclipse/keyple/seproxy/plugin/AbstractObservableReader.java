/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.plugin;


import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Abstract definition of an observable reader. Factorizes setSetProtocols and will factorize the
 * transmit method logging
 * 
 */

public abstract class AbstractObservableReader extends AbstractLoggedObservable<ReaderEvent>
        implements ProxyReader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractObservableReader.class);

    private final String pluginName;

    private long before; // timestamp recorder

    protected abstract SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws KeypleIOReaderException, KeypleChannelStateException, KeypleReaderException;

    protected abstract SeResponse processSeRequest(SeRequest seRequest)
            throws KeypleIOReaderException, KeypleChannelStateException, KeypleReaderException;

    /**
     * Reader constructor
     *
     * Force the definition of a name through the use of super method.
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    protected AbstractObservableReader(String pluginName, String readerName) {
        super(readerName);
        this.pluginName = pluginName;
        this.before = System.nanoTime();
    }

    /**
     * Execute the transmission of a list of {@link SeRequest} and returns a list of
     * {@link SeResponse}
     *
     * @param requestSet the request set
     * @return responseSet the response set
     * @throws KeypleReaderException if a reader error occurs
     */
    public final SeResponseSet transmit(SeRequestSet requestSet) throws KeypleReaderException {
        if (requestSet == null) {
            throw new IllegalArgumentException("seRequestSet must not be null");
        }

        SeResponseSet responseSet;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SEREQUESTSET = {}, elapsed {} ms.", this.getName(),
                    requestSet.toString(), elapsedMs);
        }

        try {
            responseSet = processSeRequestSet(requestSet);
        } catch (KeypleChannelStateException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SEREQUESTSET channel failure. elapsed {}", elapsedMs);
            throw new KeypleReaderException("SeRequestSet Transmit failed", ex);
        } catch (KeypleIOReaderException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SEREQUESTSET IO failure. elapsed {}", elapsedMs);
            throw new KeypleReaderException("SeRequestSet Transmit failed", ex);
        }

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SERESPONSESET = {}, elapsed {} ms.", this.getName(),
                    responseSet.toString(), elapsedMs);
        }

        return responseSet;
    }

    /**
     * Execute the transmission of a {@link SeRequest} and returns a {@link SeResponse}
     * 
     * @param seRequest the request to be transmitted
     * @return the received response
     * @throws KeypleReaderException if a reader error occurs
     */
    public final SeResponse transmit(SeRequest seRequest) throws KeypleReaderException {
        SeResponse seResponse;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SEREQUEST = {}, elapsed {} ms.", this.getName(),
                    seRequest.toString(), elapsedMs);
        }

        try {
            seResponse = processSeRequest(seRequest);
        } catch (KeypleChannelStateException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SEREQUEST channel failure. elapsed {}", elapsedMs);
            throw new KeypleReaderException("SeRequest Transmit failed", ex);
        } catch (KeypleIOReaderException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SEREQUEST IO failure. elapsed {}", elapsedMs);
            throw new KeypleReaderException("SeRequest Transmit failed", ex);
        }

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] transmit => SERESPONSE = {}, elapsed {} ms.", this.getName(),
                    seResponse.toString(), elapsedMs);
        }

        return seResponse;
    }

    /**
     * @return Plugin name
     */
    protected final String getPluginName() {
        return pluginName;
    }

    /**
     * Compare the name of the current ProxyReader to the name of the ProxyReader provided in
     * argument
     * 
     * @param proxyReader a ProxyReader object
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(ProxyReader proxyReader) {
        return this.getName().compareTo(proxyReader.getName());
    }
}
