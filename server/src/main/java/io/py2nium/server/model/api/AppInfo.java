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


package io.py2nium.server.model.api;

public class AppInfo {

    public final String packageName;

    public final String applicationName;

    public final int uid;
    
    public final String[] activities;

    public final String[] services;

    public final String dumpInfo;

    public AppInfo(String packageName, String applicationName, int uid, String[] activities, String[] services, String dumpInfo) {
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.uid = uid;
        this.activities = activities;
        this.services = services;
        this.dumpInfo = dumpInfo;
    }

}
