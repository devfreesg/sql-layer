/**
 * Copyright (C) 2009-2013 Akiban Technologies, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.foundationdb.sql.embedded;

import com.foundationdb.qp.operator.QueryBindings;

import com.foundationdb.sql.aisddl.AISDDL;
import com.foundationdb.sql.parser.DDLStatementNode;
import static com.foundationdb.server.service.dxl.DXLFunctionsHook.DXLFunction;

class ExecutableDDLStatement extends ExecutableStatement
{
    private DDLStatementNode ddl;
    private String sql;

    protected ExecutableDDLStatement(DDLStatementNode ddl, String sql) {
        this.ddl = ddl;
        this.sql = sql;
    }

    @Override
    public ExecuteResults execute(EmbeddedQueryContext context, QueryBindings bindings) {
        context.lock(DXLFunction.UNSPECIFIED_DDL_WRITE);
        try {
            AISDDL.execute(ddl, sql, context);
        }
        finally {
            context.unlock(DXLFunction.UNSPECIFIED_DDL_WRITE);
        }
        return new ExecuteResults();
    }

    @Override
    public TransactionMode getTransactionMode() {
        return TransactionMode.NONE;
    }

    @Override
    public TransactionAbortedMode getTransactionAbortedMode() {
        return TransactionAbortedMode.NOT_ALLOWED;
    }

    @Override
    public AISGenerationMode getAISGenerationMode() {
        return AISGenerationMode.ALLOWED;
    }

}