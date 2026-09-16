import json, tempfile, unittest
from pathlib import Path
import sys
sys.path.insert(0,str(Path(__file__).parents[1]))
from v3_dataset import validate_row, training_rows

class V3DatasetTest(unittest.TestCase):
    def row(self, split="TRAIN"):
        return {"metadata":{"schemaVersion":"DIRECTOR_EXPERIENCE_FEATURES_V3","stateId":"s","candidateId":"c","split":split,"trainingAllowed":split=="TRAIN"},"stateFeatures":{"family":"WILDERNESS"},"candidateFeatures":{"candidateKind":"ambient"},"supervision":{"teacherQuality":.5,"teacherChosen":False}}
    def test_schema_and_leakage(self): validate_row(self.row())
    def test_locked_training_guard(self):
        with self.assertRaises(ValueError): training_rows(tempfile.mkdtemp(),"TEST")
    def test_target_in_input_rejected(self):
        r=self.row(); r["candidateFeatures"]["teacherQuality"]=.5
        with self.assertRaises(ValueError): validate_row(r)
