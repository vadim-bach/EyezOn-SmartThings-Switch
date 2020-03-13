/**
 * Author: Vadim Bachmutsky
 * Program: Eyez-On SmartThings Switch
 * Version: 1.0
 *
 * Description:
 * Integrates SmartThings with EnvisaLink-enabled home security alarm system through
 * the use of the Eyez-On server to enable automation for arming/disarming system.
 *
 * TODO: Update switch state based on system status (feedback loop).
 */

/**
 *	Copyright 2020 Vadim Bachmutsky
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
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
def STATUS_UNKNOWN() {
	return "Unknown"
}
def OPERATION_ARM_STAY() {
	return "armstay"
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
def ARM_MODE_AWAY() {
	return "Away"
}

preferences {
    section("Settings"){
        input "mid", "text", title: "Account ID", required: true
        input "did", "text", title: "Device ID", required: true
        input "part", "number", title: "Partition #", required: true
        input "pin", "number", title: "Disarm PIN", required: true
        input "mode", "enum", title: "Arm Mode", options: [ARM_MODE_STAY(), ARM_MODE_AWAY()]
    }
}

metadata {
    definition (name: "Eyez-On SmartThings Switch", namespace: "vadim-bach", author: "Vadim Bachmutsky") {
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
    }

    // simulator metadata
    simulator {
    }

    // UI tile definitions
    tiles {
        standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
            state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
        }
        main "button"
            details (["button"])
    }
}

def getSystemStatus() {
    def textData
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
    
    def systemStatus = STATUS_UNKNOWN()
    if (textData.contains(STATUS_READY())) {
    	systemStatus = STATUS_READY()
    } else if (textData.contains(STATUS_BUSY())) {
    	systemStatus = STATUS_BUSY()
    } else if (textData.contains(STATUS_EXIT_DELAY())) {
    	systemStatus = STATUS_EXIT_DELAY()
    } else if (textData.contains(STATUS_AWAY_ARMED())) {
    	systemStatus = STATUS_AWAY_ARMED()
    } else if (textData.contains(STATUS_STAY_ARMED())) {
    	systemStatus = STATUS_STAY_ARMED()
    }

    log.info "Determined system status to be ${systemStatus}"
    return systemStatus
}

def performOperation(operation) {
    log.info "Received request to perform operation: ${operation}"
    
    try {
     	def random = new Random().nextInt(99999999) + 1
        def path = "${EYEZON_URI()}${EYEZON_PATH()}?mid=${settings.mid}&action=s&did=${settings.did}&type=15&dmdben=f&rand=${random}"
        
        def body = "part=${settings.part}&pin=${settings.pin}"
        if (operation == OPERATION_DISARM()) {
        	body += "&extaction=hdisarm"
        } else if (operation == OPERATION_ARM_STAY()) {
        	body += "&extaction=dohpincommand&hextaction=harmstay"
        } else {
        	body += "&extaction=dohpincommand&hextaction=harmaway"
        }
        
        httpPost(path, body)
        log.info "Operation ${operation} performed successfully"
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
    
    def operation = settings.mode == 'Away' ? OPERATION_ARM_AWAY() : OPERATION_ARM_STAY()
    performOperation(operation)
}

def off() {
    log.info "Received request to disarm system. Mode: ${settings.mode}"

    def systemStatus = getSystemStatus()
    if (![STATUS_AWAY_ARMED(), STATUS_STAY_ARMED(), STATUS_EXIT_DELAY()].contains(systemStatus)) {
    	log.error "Cannot disarm system: System is in invalid state: ${systemStatus}"
        return
    }
    
    if (systemStatus == STATUS_STAY_ARMED() && settings.mode == ARM_MODE_AWAY()) {
    	log.error "Cannot disarm system: Expecting status ${STATUS_AWAY_ARMED()}. Actual status: ${systemStatus}"
        return
    } else if (systemStatus == STATUS_AWAY_ARMED() && settings.mode == ARM_MODE_STAY()) {
    	log.error "Cannot disarm system: Expecting status ${STATUS_STAY_ARMED()}. Actual status: ${systemStatus}"
        return
    }
    
    performOperation(OPERATION_DISARM())
}
