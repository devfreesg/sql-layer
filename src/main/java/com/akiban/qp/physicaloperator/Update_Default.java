/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.qp.physicaloperator;

import com.akiban.qp.exec.CudPlannable;
import com.akiban.qp.exec.CudResult;
import com.akiban.qp.row.Row;
import com.akiban.server.RowData;
import com.akiban.server.RowDef;
import com.akiban.server.store.PersistitStore;
import com.akiban.util.ArgumentValidation;
import com.akiban.util.Strings;

import java.util.Collections;
import java.util.List;

public final class Update_Default implements CudPlannable {

    // Object interface

    @Override
    public String toString() {
        return String.format("%s(%s -> %s)", getClass().getSimpleName(), inputOperator, updateFunction);
    }

    // constructor

    public Update_Default(PhysicalOperator inputOperator, UpdateFunction updateFunction) {
        ArgumentValidation.notNull("update lambda", updateFunction);
        if (!inputOperator.cursorAbilitiesInclude(CursorAbility.MODIFY)) {
            throw new IllegalArgumentException("input operator must be modifiable: " + inputOperator.getClass());
        }
        
        this.inputOperator = inputOperator;
        this.updateFunction = updateFunction;
    }

    // CudResult interface

    @Override
    public CudResult run(Bindings bindings, StoreAdapter adapter) {
        int seen = 0;
        long start = System.currentTimeMillis();
        Cursor inputCursor = inputOperator.cursor(adapter);
        //Cursor execution =  new Execution(inputCursor, updateFunction);
        inputCursor.open(bindings);
        try {
            while (inputCursor.next()) {
                ++seen;
                Row oldRow = inputCursor.currentRow();
                if (updateFunction.rowIsSelected(oldRow)) {
                    Row newRow = updateFunction.evaluate(oldRow, bindings);
                    adapter.updateRow(oldRow, newRow, bindings);
                }
            }
        } finally {
            inputCursor.close();
        }
        long end = System.currentTimeMillis();
        return new StandardCudResult(end - start, seen, seen);
    }

    // Plannable interface

    @Override
    public List<PhysicalOperator> getInputOperators() {
        return Collections.singletonList(inputOperator);
    }

    @Override
    public String describePlan()
    {
        return describePlan(inputOperator);
    }

    @Override
    public String describePlan(PhysicalOperator inputOperator) {
        return inputOperator + Strings.nl() + this;
    }

    // Object state

    private final PhysicalOperator inputOperator;
    private final UpdateFunction updateFunction;
    
}
