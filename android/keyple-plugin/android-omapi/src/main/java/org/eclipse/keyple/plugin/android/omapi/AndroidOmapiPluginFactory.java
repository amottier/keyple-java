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
package org.eclipse.keyple.plugin.android.omapi;

import org.eclipse.keyple.core.seproxy.AbstractPluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;

public final class AndroidOmapiPluginFactory extends AbstractPluginFactory {
    @Override
    public String getPluginName() {
        return AndroidOmapiPlugin.PLUGIN_NAME;
    }

    @Override
    protected ReaderPlugin getPluginInstance() {
        return AndroidOmapiPluginImpl.getInstance();
    }
}
