package com.sobrenaturaldirector.shadow;

/** Pure scorer contract. It receives DTOs and has no decision/execution API. */
public interface ShadowScorer { ShadowScoreResult score(ShadowDecisionSnapshot snapshot); }
