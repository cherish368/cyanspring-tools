/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 *
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package org.cyanspring.tools.common;

import java.util.Date;

public class Clock {
    public enum Mode {AUTO, MANUAL}

    private static Clock instance;
    private static Mode mode = Mode.AUTO;

    protected Clock() {
    }

    public static Clock getInstance() {
        if (null == instance) {
            createInstance();
        }
        return instance;
    }

    private static void createInstance() {
        if (mode.equals(Mode.AUTO)) {
            instance = new Clock();
        } else if (mode.equals(Mode.MANUAL)) {
            instance = new ClockSim();
        }
    }

    static public Mode getMode() {
        return Clock.mode;
    }

    static public void setMode(Mode mode) {
        Clock.mode = mode;
        createInstance();
    }

    public Date now() {
        return new Date();
    }

}
