/* Manages persistent state information useful for crash reporting such as
 * number of sessions, session length, etc.
 */

#ifndef HDR_KSCrashMonitor_AppState_h
#define HDR_KSCrashMonitor_AppState_h

#ifdef __cplusplus
extern "C" {
#endif

#include "KSCrashMonitor.h"

#include <stdbool.h>


typedef struct
{
    // Saved data
    
    /** Total active time elapsed since the last crash. */
    double activeDurationSinceLastCrash;
    
    /** Total time backgrounded elapsed since the last crash. */
    double backgroundDurationSinceLastCrash;
    
    /** Number of app launches since the last crash. */
    int launchesSinceLastCrash;
    
    /** Number of sessions (launch, resume from suspend) since last crash. */
    int sessionsSinceLastCrash;
    
    /** Total active time elapsed since launch. */
    double activeDurationSinceLaunch;
    
    /** Total time backgrounded elapsed since launch. */
    double backgroundDurationSinceLaunch;
    
    /** Number of sessions (launch, resume from suspend) since app launch. */
    int sessionsSinceLaunch;
    
    /** If true, the application crashed on the previous launch. */
    bool crashedLastLaunch;
    
    // Live data
    
    /** If true, the application crashed on this launch. */
    bool crashedThisLaunch;
    
    /** Timestamp for when the app state was last changed (active<->inactive,
     * background<->foreground) */
    double appStateTransitionTime;
    
    /** If true, the application is currently active. */
    bool applicationIsActive;
    
    /** If true, the application is currently in the foreground. */
    bool applicationIsInForeground;
    
} KSCrash_AppState;
    

/** Initialize the state monitor.
 *
 * @param stateFilePath Where to store on-disk representation of state.
 */
void kscrashstate_initialize(const char* stateFilePath);

/** Reset the crash state.
 */
bool kscrashstate_reset(void);

/** Notify the crash reporter of the application active state.
 *
 * @param isActive true if the application is active, otherwise false.
 */
void kscrashstate_notifyAppActive(bool isActive);

/** Notify the crash reporter of the application foreground/background state.
 *
 * @param isInForeground true if the application is in the foreground, false if
 *                 it is in the background.
 */
void kscrashstate_notifyAppInForeground(bool isInForeground);

/** Notify the crash reporter that the application is terminating.
 */
void kscrashstate_notifyAppTerminate(void);

/** Notify the crash reporter that the application has crashed.
 */
void kscrashstate_notifyAppCrash(void);

/** Read-only access into the current state.
 */
const KSCrash_AppState* const kscrashstate_currentState(void);

/** Access the Monitor API.
 */
KSCrashMonitorAPI* kscm_appstate_getAPI(void);


#ifdef __cplusplus
}
#endif

#endif // HDR_KSCrashMonitor_AppState_h
