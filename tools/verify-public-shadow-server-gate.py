import sys
from pathlib import Path

p = Path(sys.argv[1] if len(sys.argv) > 1 else 'runtime-tests/server/public-shadow-research-server.properties')
values = {}
for line in p.read_text(encoding='utf-8').splitlines():
    if '=' in line:
        key, value = line.split('=', 1)
        values[key] = value
required = 'RUN_ID SOURCE_COMMIT SERVER_ROOT FORGE_VERSION JAVA_VERSION SERVER_PORT LOCAL_PORT_LISTENING DEDICATED_MOD_STACK DEDICATED_COMPATIBILITY_AUDIT MODEL_LOAD CONSENT_REQUIRED SESSION_SCOPED_CONSENT OPAQUE_PARTICIPANT_ID PRIVACY_GATE SERVER_BOOT SHADOW_MODEL_LOAD GRACEFUL_SHUTDOWN JAVA_TESTS PYTHON_TESTS GUIDE_CREATED SERVER_MOD_MANIFEST_CREATED CLIENT_MOD_MANIFEST_CREATED ORACLE_GUIDE_CREATED PROTECTED_WORLD_TOUCHED ORIGINAL_MODS_MODIFIED SECRETS_COMMITTED FULL_REAL_EXPERIENCE_TRAINING MODEL_WEIGHTS_CHANGED FINAL_GATE'.split()
bad = [key for key in required if not values.get(key)]
expected = {'FORGE_VERSION':'10.13.4.1614', 'JAVA_VERSION':'1.8.0_502', 'SERVER_PORT':'25565', 'LOCAL_PORT_LISTENING':'PASS', 'DEDICATED_MOD_STACK':'PASS', 'DEDICATED_COMPATIBILITY_AUDIT':'PASS', 'CONSENT_REQUIRED':'YES', 'SESSION_SCOPED_CONSENT':'YES', 'OPAQUE_PARTICIPANT_ID':'PASS', 'SERVER_BOOT':'PASS', 'GRACEFUL_SHUTDOWN':'PASS', 'PROTECTED_WORLD_TOUCHED':'NO', 'ORIGINAL_MODS_MODIFIED':'NO', 'SECRETS_COMMITTED':'NO', 'FULL_REAL_EXPERIENCE_TRAINING':'NO', 'MODEL_WEIGHTS_CHANGED':'NO'}
bad += [key for key, expected_value in expected.items() if values.get(key) != expected_value]
if bad or values.get('FINAL_GATE') != 'FAIL':
    print('gate record invalid: ' + ','.join(sorted(set(bad))))
    sys.exit(1)
print('server foundation recorded: blocker remains V4 runtime wiring / consent collection')
