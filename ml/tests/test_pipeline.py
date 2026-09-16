import json, tempfile, unittest, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parents[1]))
from data import fit_transform, encode, read_rows

class PipelineTest(unittest.TestCase):
    def row(self, split="TRAIN"):
        return {"schemaVersion":"DIRECTOR_BENCHMARK_FEATURES_V2","scenarioId":"s","split":split,"stateFeatures":{"hardValid":True,"noAction":False,"environment":"WILDERNESS","dimension":0,"providerAvailable":True,"continuation":False,"recentHigh":0},"candidateFeatures":{"candidateKind":"a","intent":"AMBIENT_HINT","safety":"NON_DESTRUCTIVE","provider":"structure","baseUtility":55},"target":{"quality":.5,"chosen":False}}
    def test_target_is_not_encoded(self):
        prep=fit_transform([self.row()]); x,_,y=encode(self.row(),prep); self.assertNotIn(y,x)
    def test_fit_rejects_non_train(self):
        with self.assertRaises(ValueError): fit_transform([self.row("VALIDATION")])
    def test_schema_guard(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d)/"x"; p.write_text(json.dumps({"schemaVersion":"bad"})+"\n")
            with self.assertRaises(ValueError): read_rows(p)
if __name__=="__main__": unittest.main()
