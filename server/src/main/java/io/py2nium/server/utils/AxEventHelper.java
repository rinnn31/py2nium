/*
* Apache 2.0 License
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package io.py2nium.server.utils;

import android.app.UiAutomation;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import io.py2nium.server.core.UiAutomatorWrapper;

public class AxEventHelper {
    private static AxEventHelper INSTANCE;
    private static final int MAX_DELTA_TIME = 5 * 60 * 1000;
    private final UiAutomation mUiAutomation;
    private final List<AccessibilityEvent> mEvents;

    public static synchronized AxEventHelper getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AxEventHelper();
        }
        return INSTANCE;
    }

    private AxEventHelper() {
        mEvents = new ArrayList<>();
        mUiAutomation = UiAutomatorWrapper.getUiAutomation();

        mUiAutomation.setOnAccessibilityEventListener((event) -> {
            synchronized (mEvents) {
                long currentTime = System.currentTimeMillis();
                clearOldEvents(currentTime);

                AccessibilityEvent newEvent = AccessibilityEvent.obtain(event);
                mEvents.add(newEvent);
            }
        });
    }

    private void clearOldEvents(long currentTime) {
        int i = 0;
        while (i < mEvents.size()) {
            AccessibilityEvent event = mEvents.get(i);
            if (currentTime - event.getEventTime() > MAX_DELTA_TIME) {
                mEvents.remove(i);
                event.recycle();

                i++;
            } else {
                break;
            }
        }
    }
    public AccessibilityEvent getLatestEvent() {
        synchronized (mEvents) {
            if(mEvents.isEmpty()) {
                return null;
            }
            return AccessibilityEvent.obtain(mEvents.get(mEvents.size() - 1));
        }
    }

    public AccessibilityEvent getEvent(int eventId, long maxDeltaTime) {
        synchronized (mEvents) {
            long currentTime = System.currentTimeMillis();
            for (int i = mEvents.size() - 1; i >= 0; i--) {
                AccessibilityEvent event = mEvents.get(i);
                if (currentTime - event.getEventTime() > maxDeltaTime) {
                    break;
                }
                if (event.getEventType() == eventId) {
                    return AccessibilityEvent.obtain(event);
                }
            }
            return null;
        }
    }

    public List<AccessibilityEvent> getEventsByTime(int deltaTime) {
        synchronized (mEvents) {
            if(deltaTime > MAX_DELTA_TIME) {
                Logger.w(AxEventHelper.class, "Requested delta time is too large, setting to max value");
                deltaTime = MAX_DELTA_TIME;
            }

            long currentTime = System.currentTimeMillis();
            List<AccessibilityEvent> events = new ArrayList<>();
            for (int i = mEvents.size() - 1; i >= 0; i--) {
                AccessibilityEvent event = mEvents.get(i);
                if (currentTime - event.getEventTime() > deltaTime) {
                    break;
                }
                events.add(AccessibilityEvent.obtain(event));
            }
            return events;
        }
    }

    public List<AccessibilityEvent> getEventsByCount(int count) {
        synchronized (mEvents) {
            if(count > mEvents.size()) {
                Logger.w(AxEventHelper.class, "Requested count is too large, setting to current available event count");
                count = mEvents.size();
            }

            List<AccessibilityEvent> events = new ArrayList<>();
            for (int i = mEvents.size() - 1; i >= 0; i--) {
                events.add(AccessibilityEvent.obtain(mEvents.get(i)));
                if(events.size() == count) {
                    break;
                }
            }
            return events;
        }
    }
    public boolean waitForEvent(int eventId, int timeout) {
        Runnable emptyRunnable = () -> {};
        UiAutomation.AccessibilityEventFilter filter = (event) -> {
            return event.getEventType() == eventId;
        };

        try {
            mUiAutomation.executeAndWaitForEvent(emptyRunnable, filter, timeout).recycle();
            return true;
        } catch (TimeoutException e) {
            return false;
        }catch (Exception e) {
            Logger.e(AxEventHelper.class, "Error while waiting for event", e);
            return false;
        }
    }
}

