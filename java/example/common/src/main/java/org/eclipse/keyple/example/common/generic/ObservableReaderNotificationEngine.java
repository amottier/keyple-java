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
package org.eclipse.keyple.example.common.generic;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ObservableReaderNotificationEngine {
    private final static Logger logger =
            LoggerFactory.getLogger(ObservableReaderNotificationEngine.class);

    private SpecificPluginObserver pluginObserver;


    public ObservableReaderNotificationEngine() {
        /* initializes observers */
        SpecificReaderObserver readerObserver = new SpecificReaderObserver();
        pluginObserver = new SpecificPluginObserver(readerObserver);
    }

    public void setPluginObserver() {

        /*
         * We add an observer to each plugin (only one in this example) the readers observers will
         * be added dynamically upon plugin notification (see SpecificPluginObserver.update)
         */
        for (ReaderPlugin plugin : SeProxyService.getInstance().getPlugins()) {

            if (plugin instanceof ObservablePlugin) {
                logger.info("Add observer PLUGINNAME = {}", plugin.getName());
                ((ObservablePlugin) plugin).addObserver(this.pluginObserver);
            } else {
                logger.info("PLUGINNAME = {} isn't observable", plugin.getName());
            }
        }
    }

    /**
     * This method is called whenever a Reader event occurs (SE insertion/removal)
     */
    public class SpecificReaderObserver implements ObservableReader.ReaderObserver {

        SpecificReaderObserver() {
            super();
        }

        public void update(ReaderEvent event) {
            /* just log the event */
            logger.info("Event: PLUGINNAME = {}, READERNAME = {}, EVENT = {}",
                    event.getPluginName(), event.getReaderName(), event.getEventType().getName());
        }
    }

    /**
     * This method is called whenever a Plugin event occurs (reader insertion/removal)
     */
    public class SpecificPluginObserver implements ObservablePlugin.PluginObserver {

        SpecificReaderObserver readerObserver;

        SpecificPluginObserver(SpecificReaderObserver readerObserver) {
            this.readerObserver = readerObserver;
        }

        @Override
        public void update(PluginEvent event) {
            for (String readerName : event.getReaderNames()) {
                SeReader reader = null;
                logger.info("PluginEvent: PLUGINNAME = {}, READERNAME = {}, EVENTTYPE = {}",
                        event.getPluginName(), readerName, event.getEventType());

                /* We retrieve the reader object from its name. */
                try {
                    reader = SeProxyService.getInstance().getPlugin(event.getPluginName())
                            .getReader(readerName);
                } catch (KeyplePluginNotFoundException e) {
                    e.printStackTrace();
                } catch (KeypleReaderNotFoundException e) {
                    e.printStackTrace();
                }
                switch (event.getEventType()) {
                    case READER_CONNECTED:
                        logger.info("New reader! READERNAME = {}", reader.getName());

                        /*
                         * We are informed here of a disconnection of a reader.
                         *
                         * We add an observer to this reader if this is possible.
                         */
                        if (reader instanceof ObservableReader) {
                            if (readerObserver != null) {
                                logger.info("Add observer READERNAME = {}", reader.getName());
                                ((ObservableReader) reader).addObserver(readerObserver);
                            } else {
                                logger.info("No observer to add READERNAME = {}", reader.getName());
                            }
                        }
                        break;
                    case READER_DISCONNECTED:
                        /*
                         * We are informed here of a disconnection of a reader.
                         *
                         * The reader object still exists but will be removed from the reader list
                         * right after. Thus, we can properly remove the observer attached to this
                         * reader before the list update.
                         */
                        logger.info("Reader removed. READERNAME = {}", readerName);
                        if (reader instanceof ObservableReader) {
                            if (readerObserver != null) {
                                logger.info("Remove observer READERNAME = {}", readerName);
                                ((ObservableReader) reader).removeObserver(readerObserver);
                            } else {
                                logger.info("Unplugged reader READERNAME = {} wasn't observed.",
                                        readerName);
                            }
                        }
                        break;
                    default:
                        logger.info("Unexpected reader event. EVENT = {}",
                                event.getEventType().getName());
                        break;
                }
            }
        }
    }
}
