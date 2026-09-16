import json, tempfile, unittest, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parents[1]))
from data import fit_transform, encode, read_rows, validate_row
from metrics import group_metrics

class PipelineTest(unittest.TestCase):
    def row(self, split="TRAIN"):
        return {"schemaVersion":"DIRECTOR_BENCHMARK_FEATURES_V2","scenarioId":"s","split":split,"trainingAllowed":split=="TRAIN","stateFeatures":{"hardValid":True,"noAction":False,"environment":"WILDERNESS","dimension":0,"providerAvailable":True,"continuation":False,"recentHigh":0},"candidateFeatures":{"candidateKind":"a","intent":"AMBIENT_HINT","safety":"NON_DESTRUCTIVE","provider":"structure","baseUtility":55},"target":{"quality":.5,"chosen":False}}
    def test_target_is_not_encoded(self):
        prep=fit_transform([self.row()]); x,_,y=encode(self.row(),prep); self.assertNotIn(y,x)
    def test_fit_rejects_non_train(self):
        with self.assertRaises(ValueError): fit_transform([self.row("VALIDATION")])
    def test_schema_guard(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d)/"x"; p.write_text(json.dumps({"schemaVersion":"bad"})+"\n")
            with self.assertRaises(ValueError): read_rows(p)
    def test_non_finite_rejected(self):
        r=self.row(); r["stateFeatures"]["recentHigh"]=float("nan")
        with self.assertRaises(ValueError): validate_row(r)
    def test_post_decision_input_rejected(self):
        r=self.row(); r["candidateFeatures"]["quality"]=.5
        with self.assertRaises(ValueError): validate_row(r)
    def test_group_metrics_handles_single_and_multiple_candidates(self):
        one={"s":[{"candidateId":"a","prediction":1.0,"target":1.0}]}
        self.assertEqual(group_metrics(one)["mean_regret"],0.0)
        two={"s":[{"candidateId":"a","prediction":2.0,"target":.8},{"candidateId":"b","prediction":1.0,"target":.5}]}
        self.assertEqual(group_metrics(two)["pairwise_accuracy"],1.0)
if __name__=="__main__": unittest.main()
