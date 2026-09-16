# Robustness evidence

Remote PyTorch checks passed for unknown-category forward, reordered-feature
and schema/checkpoint compatibility guards, CPU/CUDA inference, repeated
deterministic inference, extreme finite values, one candidate and multiple
candidates. Missing features, NaN/Inf and empty candidate sets fail closed.

The empty-set guard was added as an isolated offline model-boundary check; it
does not change normal non-empty scores or any Director runtime path.
