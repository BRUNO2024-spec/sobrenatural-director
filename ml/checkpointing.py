import os, random, tempfile
from pathlib import Path
import torch

def atomic_save(payload, path):
    path = Path(path); path.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp = tempfile.mkstemp(prefix=path.name+".", suffix=".tmp", dir=path.parent)
    try:
        with os.fdopen(fd, "wb") as f:
            torch.save(payload, f); f.flush(); os.fsync(f.fileno())
        os.replace(tmp, path)
    finally:
        if os.path.exists(tmp): os.unlink(tmp)

def rng_state():
    state = {"python": random.getstate(), "torch": torch.get_rng_state()}
    try:
        import numpy as np; state["numpy"] = np.random.get_state()
    except ImportError: pass
    if torch.cuda.is_available(): state["cuda"] = torch.cuda.get_rng_state_all()
    return state

def restore_rng(state):
    random.setstate(state["python"]); torch.set_rng_state(state["torch"])
    if "numpy" in state:
        import numpy as np; np.random.set_state(state["numpy"])
    if "cuda" in state and torch.cuda.is_available(): torch.cuda.set_rng_state_all(state["cuda"])

def load_compatible(path, schema, manifest_hash, feature_order):
    try: c = torch.load(path, map_location="cpu", weights_only=False)
    except Exception as e: raise ValueError("corrupt checkpoint") from e
    if c.get("feature_schema") != schema or c.get("dataset_manifest_hash") != manifest_hash or c.get("feature_order") != feature_order:
        raise ValueError("checkpoint compatibility mismatch")
    return c
