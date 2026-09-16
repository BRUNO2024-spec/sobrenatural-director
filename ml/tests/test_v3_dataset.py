import json, tempfile, unittest
from pathlib import Path
import sys
sys.path.insert(0,str(Path(__file__).parents[1]))
from v3_dataset import validate_row, training_rows
from v3_preprocess import fit, encode

class V3DatasetTest(unittest.TestCase):
    def row(self, split="TRAIN"):
        return {"metadata":{"schemaVersion":"DIRECTOR_EXPERIENCE_FEATURES_V3","stateId":"s","candidateId":"c","split":split,"trainingAllowed":split=="TRAIN"},"stateFeatures":{"family":"WILDERNESS"},"candidateFeatures":{"candidateKind":"ambient"},"supervision":{"teacherQuality":.5,"teacherChosen":False}}
    def test_schema_and_leakage(self): validate_row(self.row())
    def test_locked_training_guard(self):
        with self.assertRaises(ValueError): training_rows(tempfile.mkdtemp(),"TEST")
    def test_target_in_input_rejected(self):
        r=self.row(); r["candidateFeatures"]["teacherQuality"]=.5
        with self.assertRaises(ValueError): validate_row(r)
    def test_v3_preprocess_requires_train_and_handles_oov(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d); (p/"TRAIN").mkdir(); (p/"VALIDATION").mkdir()
            r=self.row(); (p/"TRAIN"/"part-00000.jsonl").write_text(json.dumps(r)+"\n")
            prep=fit(p); x,c,y=encode(r,prep); self.assertEqual(len(x),19); self.assertEqual(len(c),8)
