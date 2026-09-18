#!/usr/bin/env python3
"""Compute the artifact-complete fingerprint for the approved provider stack."""
import argparse
import hashlib
from pathlib import Path

PROVIDERS = {
    "CustomNPCs_1.7.10d(19jun17).jar": ("customnpcs", "customnpcs", "1.7.10d", ("director:actor_source=MUTATION_VALIDATED",)),
    "GraveStone-2.13.0.jar": ("gravestone", "GraveStone", "2.13.0", ("director:block_mutation=MUTATION_VALIDATED", "director:rollback_safe=MUTATION_VALIDATED", "director:structure_source=MUTATION_VALIDATED")),
    "SlenderMan-3.3_1.7.10.jar": ("slenderman", "dg_slender", "3.3_1.7.10", ("director:threat_source=MUTATION_VALIDATED",)),
}

def sha(path):
    digest=hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024*1024), b""):
            digest.update(block)
    return digest.hexdigest()

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("original_mods", type=Path)
    args=parser.parse_args()
    records=[]
    for filename,(provider,mod,version,caps) in PROVIDERS.items():
        path=args.original_mods/filename
        if not path.is_file():
            raise SystemExit("missing approved provider artifact: "+str(path))
        records.append((provider,mod,version,sha(path),caps))
    canonical="".join(provider+"|"+mod+"|"+version+"|"+artifact_sha+"|1|AVAILABLE_SUPPORTED|"+";".join(caps)+",;" for provider,mod,version,artifact_sha,caps in sorted(records))
    print("PROVIDER_STACK_COUNT="+str(len(records)))
    print("PROVIDER_STACK_FINGERPRINT="+hashlib.sha256(canonical.encode("utf-8")).hexdigest())
    for provider,mod,version,artifact_sha,caps in sorted(records):
        print("PROVIDER="+provider+" MOD_ID="+mod+" VERSION="+version+" SHA256="+artifact_sha+" CAPABILITIES="+",".join(caps))
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
