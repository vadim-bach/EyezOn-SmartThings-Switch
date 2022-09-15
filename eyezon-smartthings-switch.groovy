/*
 * Author: Vadim Bachmutsky
 * Contributors:
 *     John Constantelos (https://github.com/jsconstantelos)
 * Program: Eyez-On SmartThings Switch
 *
 * Description:
 * Integrates SmartThings with EnvisaLink-enabled home security alarm system through
 * the use of the Eyez-On server to enable automation for arming/disarming system.
 *
 * Copyright 2020 Vadim Bachmutsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
*/

/* 
APP CONSTANTS (how ST doesn't support one of the most basic constructs of
modern programming is beyond me)
*/
def EYEZON_URI() {
    return "https://www.eyez-on.com"
}
def EYEZON_PATH() {
    return "/EZMOBILE/index.php"
}
def STATUS_READY() {
    return "Ready"
}
def STATUS_BUSY() {
    return "Busy"
}
def STATUS_EXIT_DELAY() {
    return "Exit Delay"
}
def STATUS_AWAY_ARMED() {
    return "Away Armed"
}
def STATUS_STAY_ARMED() {
    return "Stay Armed"
}
def STATUS_NIGHT_ARMED() {
    return "Night Armed"
}
def STATUS_INSTANT_ARMED() {
    return "Instant Armed"
}
def STATUS_MAX_ARMED() {
    return "Max Armed"
}
def STATUS_UNKNOWN() {
    return "Unknown"
}
def OPERATION_ARM_STAY() {
    return "armstay"
}
def OPERATION_ARM_NIGHT() {
    return "armnight"
}
def OPERATION_ARM_INSTANT() {
    return "arminstant"
}
def OPERATION_ARM_MAX() {
    return "armmax"
}
def OPERATION_ARM_AWAY() {
    return "armaway"
}
def OPERATION_DISARM() {
    return "disarm"
}
def ARM_MODE_STAY() {
    return "Stay"
}
def ARM_MODE_NIGHT() {
    return "Night"
}
def ARM_MODE_INSTANT() {
    return "Instant"
}
def ARM_MODE_MAX() {
    return "Max"
}
def ARM_MODE_AWAY() {
    return "Away"
}

preferences {
    section("Settings"){
        input "mid", "text", title: "Account ID", required: true
        input "did", "text", title: "Device ID", required: true
        input "part", "number", title: "Partition #", defaultValue: 1, required: true
        input "partName", "text", title: "Partition Label (as it appears in EyezOn)", defaultValue: "Partition 1", required: true
        input "pin", "password", title: "Disarm PIN", required: true
        input "mode", "enum", title: "Arm Mode", options: [ARM_MODE_STAY(), ARM_MODE_AWAY(), ARM_MODE_NIGHT(), ARM_MODE_INSTANT(), ARM_MODE_MAX()], required: true
        input "exitDelay", "number", title: "System exit delay (in seconds, default is 60)", defaultValue: 60, range: "1..180", required: true
        input "variant", "enum", title: "Code Variant", options: ["1", "2"], required: true
    }
}

metadata {
    definition (name: "My Eyez-On SmartThings Switch", namespace: "vadimbach", author: "Vadim Bachmutsky", ocfDeviceType: "x.com.st.d.remotecontroller") {
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
        capability "Refresh"
        capability "Polling"
        capability "Health Check"
        attribute "alarmActivity", "string"
        command "getSystemStatus"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                                attributeState "on", label:'Armed', action:"switch.off", icon:"st.security.alarm.alarm", backgroundColor:"#00A0DC", nextState: "busy"
                attributeState "off", label:'Disarmed', action:"switch.on", icon:"st.security.alarm.clear", backgroundColor:"#ffffff", nextState: "exiting"
                attributeState "busy", label:'Busy', icon:"st.security.alarm.alarm", backgroundColor:"#ffa81e"
                attributeState "exiting", label:'Exiting', action:"switch.off", icon:"st.security.alarm.alarm", backgroundColor:"#ffa81e", nextState: "busy"
            }
            tileAttribute ("device.alarmActivity", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'Updated ${currentValue}')
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"getSystemStatus", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

def initialize() {
    sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
}

void installed() {
    log.debug "installed()"
    initialize()
}

def updated() {
    log.debug "updated()"
    initialize()
}

def getSystemStatus() {
    def textData
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    try {
        def params = [
            uri: EYEZON_URI(),
            path: EYEZON_PATH(),
            query: [
                "mid": settings.mid
            ],
            contentType: "text/plain"
        ] 
        httpGet(params) { resp ->
            textData = resp.data.getText()
        }
    } catch (e) {
        log.error 'Unable to get current system status', e
        throw e
    }

    // SmartThings doesn't support regex: https://community.smartthings.com/t/unable-to-use-matcher-methods/1280
    // So we have to do this the dumb/brittle way
    def systemStatus = STATUS_UNKNOWN()
    if (textData.contains("<div id=\"s1p${settings.part}stat\" class=\"text-success\">Ready</div>")) {
        systemStatus = STATUS_READY()
        sendEvent(name: "switch", value: "off")
    } else if (textData.contains("<div id=\"s1p${settings.part}stat\" class>Busy</div>")) {
        systemStatus = STATUS_BUSY()
        sendEvent(name: "switch", value: "busy")
    } else if (textData.contains("<div id=\"s1p${settings.part}stat\" class=\"text-danger\">Exit Delay</div>")) {
        systemStatus = STATUS_EXIT_DELAY()
        sendEvent(name: "switch", value: "exiting")
    } else if (textData.contains("<div id=\"s1p${settings.part}stat\" class=\"text-danger\">Away Armed</div>")) {
        systemStatus = STATUS_AWAY_ARMED()
        sendEvent(name: "switch", value: "on")
    } else if (textData.contains("<div id=\"s1p${settings.part}stat\" class=\"text-danger\">Stay Armed</div>")) {
        systemStatus = STATUS_STAY_ARMED()
        sendEvent(name: "switch", value: "on")
    } else if (textData.contains("<div id=\"s1p${settings.part}stat\" class=\"text-danger\">Night Armed</div>")) {
        systemStatus = STATUS_NIGHT_ARMED()
        sendEvent(name: "switch", value: "on")
    } else if (textData.contains("<div id=\"s1p${settings.part}stat\" class=\"text-danger\">Instant Armed</div>")) {
        systemStatus = STATUS_INSTANT_ARMED()
        sendEvent(name: "switch", value: "on")
    } else if (textData.contains("<div id=\"s1p${settings.part}stat\" class=\"text-danger\">Max Armed</div>")) {
        systemStatus = STATUS_MAX_ARMED()
        sendEvent(name: "switch", value: "on")
    } else {
        log.error "Unable to determine system status from response: ${textData}. Setting to Unknown."
    }
    
    log.info "Determined system status to be ${systemStatus} at ${timeString}"
    sendEvent(name: "alarmActivity", value: timeString, descriptionText: text, displayed: true)
    return systemStatus
}

def performOperation(operation) {
    log.info "Received request to perform operation: ${operation}"
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    try {
        def random = new Random().nextInt(99999999) + 1
        def waitTime = operation == OPERATION_DISARM() ? 5 : settings.exitDelay
        def eventName = operation == OPERATION_DISARM() ? "busy" : "exiting"
        def typeParam = settings.variant == 1 ? 15 : 18

        def path = "${EYEZON_URI()}${EYEZON_PATH()}?mid=${settings.mid}&action=s&did=${settings.did}&type=${typeParam}&dmdben=f&rand=${random}"

        def body
        if (settings.variant == "1") {
            body = "part=${settings.part}&pin=${settings.pin}"
            if (operation == OPERATION_DISARM()) {
                body += "&extaction=hdisarm"
            } else if (operation == OPERATION_ARM_STAY()) {
                body += "&extaction=dohpincommand&hextaction=harmstay"
            } else if (operation == OPERATION_ARM_NIGHT()) {
                body += "&extaction=dohpincommand&hextaction=harmnight"
            } else if (operation == OPERATION_ARM_INSTANT()) {
                body += "&extaction=dohpincommand&hextaction=hinstant"
            } else if (operation == OPERATION_ARM_MAX()) {
                body += "&extaction=dohpincommand&hextaction=hmax"
            } else if (operation == OPERATION_ARM_AWAY()) {
                body += "&extaction=dohpincommand&hextaction=harmaway"
            } else {
                log.error "Unable to perform operation ${operation}: operation is unknown."
                return
            }
        } else if (settings.variant == "2") {
            body = "extaction=${operation}&part=${settings.part}"
            if (operation == OPERATION_DISARM()) {
                body += "&type=18&dmdben=f&pin=${settings.pin}"
            }
        } else {
            log.error "Unable to perform operation ${operation}: unknown variant ${settings.variant}."
            return
        }

        sendEvent(name: "switch", value: eventName)
        httpPost(path, body)

        log.info "Operation ${operation} successfully submitted, now waiting for system exit/disarm delay (${waitTime} sec) for confirmation..."

        runIn(waitTime, getSystemStatus, [overwrite: true])  // wait for system delay and then get system status to update the tile, and overwrite any pending schedules
        sendEvent(name: "alarmActivity", value: timeString, descriptionText: text, displayed: true)
    } catch (e) {
        log.error "Unable to perform operation ${operation}: POST failed.", e
    }
}

def on() {
    log.info "Received request to arm system. Mode: ${settings.mode}"
    def systemStatus = getSystemStatus()
    if (systemStatus != STATUS_READY()) {
        log.error "Cannot arm system. Expecting status ${STATUS_READY()}. Actual status: ${systemStatus}"
        return
    }
    def operation
    if (settings.mode == ARM_MODE_AWAY()) {
        operation = OPERATION_ARM_AWAY()
    } else if (settings.mode == ARM_MODE_STAY()) {
        operation = OPERATION_ARM_STAY()
    } else if (settings.mode == ARM_MODE_NIGHT()) {
        operation = OPERATION_ARM_NIGHT()
    } else if (settings.mode == ARM_MODE_INSTANT()) {
        operation = OPERATION_ARM_INSTANT()
    } else if (settings.mode == ARM_MODE_MAX()) {
        operation = OPERATION_ARM_MAX()
    } else {
        log.error "Cannot arm system. Unknown switch mode ${settings.mode}."
        return
    }

    performOperation(operation)
}

def off() {
    log.info "Received request to disarm system. Mode: ${settings.mode}"
    def systemStatus = getSystemStatus()
    if (![STATUS_AWAY_ARMED(), STATUS_STAY_ARMED(), STATUS_NIGHT_ARMED(), STATUS_INSTANT_ARMED(), STATUS_MAX_ARMED(), STATUS_EXIT_DELAY()].contains(systemStatus)) {
        log.error "Cannot disarm system: system is in invalid state ${systemStatus}"
        return
    }
    if (systemStatus == STATUS_AWAY_ARMED() && settings.mode != ARM_MODE_AWAY()) {
        log.error "Cannot disarm system in mode ${systemStatus} via switch in mode ${settings.mode}."
        return
    } else if (systemStatus == STATUS_STAY_ARMED() && settings.mode != ARM_MODE_STAY()) {
        log.error "Cannot disarm system in mode ${systemStatus} via switch in mode ${settings.mode}."
        return
    } else if (systemStatus == STATUS_NIGHT_ARMED() && settings.mode != ARM_MODE_NIGHT()) {
        log.error "Cannot disarm system in mode ${systemStatus} via switch in mode ${settings.mode}."
        return
    } else if (systemStatus == STATUS_INSTANT_ARMED() && settings.mode != ARM_MODE_INSTANT()) {
        log.error "Cannot disarm system in mode ${systemStatus} via switch in mode ${settings.mode}."
        return
    } else if (systemStatus == STATUS_MAX_ARMED() && settings.mode != ARM_MODE_MAX()) {
        log.error "Cannot disarm system in mode ${systemStatus} via switch in mode ${settings.mode}."
        return
    }
    performOperation(OPERATION_DISARM())
}

def refresh() {
    log.debug "refresh()..."
    getSystemStatus()
}

def ping() {
    log.debug "ping()..."
    refresh()
}

def poll() {
    log.debug "poll()..."
    refresh()
}
