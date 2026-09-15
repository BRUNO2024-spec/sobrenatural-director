package com.sobrenaturaldirector;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.composition.GraveStoneCompositionAllowlist;
import com.sobrenaturaldirector.composition.model.CompositionBlockOperation;
import com.sobrenaturaldirector.composition.model.DirectorOwnedCompositionPlan;
import com.sobrenaturaldirector.composition.model.ExternalBlockReference;

public class ControlledCompositionModelTest {
    @Test public void allowlistAcceptsOnlyTheThreePhaseBlocks() {
        assertTrue(GraveStoneCompositionAllowlist.contains(new ExternalBlockReference("GraveStone", "GSBoneBlock", 0)));
        assertTrue(GraveStoneCompositionAllowlist.contains(new ExternalBlockReference("GraveStone", "GSBoneSlab", 0)));
        assertTrue(GraveStoneCompositionAllowlist.contains(new ExternalBlockReference("GraveStone", "GSBoneStairs", 0)));
        assertFalse(GraveStoneCompositionAllowlist.contains(new ExternalBlockReference("GraveStone", "GSHauntedChest", 0)));
        assertFalse(GraveStoneCompositionAllowlist.contains(new ExternalBlockReference("GraveStone", "GSBoneBlock", 1)));
        assertFalse(GraveStoneCompositionAllowlist.contains(new ExternalBlockReference("gravestone", "GSBoneBlock", 0)));
    }

    @Test public void planCopiesOperationsAndRejectsOverBudget() {
        ExternalBlockReference block = new ExternalBlockReference("GraveStone", "GSBoneBlock", 0);
        CompositionBlockOperation op = new CompositionBlockOperation("child", 0, 0, 0, block);
        DirectorOwnedCompositionPlan plan = new DirectorOwnedCompositionPlan("group", "test", "GraveStone", "2.13.0", 0, 1, 64, 1, Arrays.asList(op));
        assertEquals(1, plan.getOperations().size());
        ArrayList<CompositionBlockOperation> tooMany = new ArrayList<CompositionBlockOperation>();
        for (int i = 0; i <= DirectorOwnedCompositionPlan.MAX_BLOCKS; i++) tooMany.add(new CompositionBlockOperation("child" + i, i, 0, 0, block));
        try {
            new DirectorOwnedCompositionPlan("too-many", "test", "GraveStone", "2.13.0", 0, 1, 64, 1, tooMany);
            fail("expected bounded plan rejection");
        } catch (IllegalArgumentException expected) { }
    }
}
