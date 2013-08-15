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


package com.foundationdb.server.expression.std;

import com.foundationdb.server.expression.std.UnaryBitExpression.UnaryBitOperator;
import org.junit.runner.RunWith;
import com.foundationdb.server.types.util.ValueHolder;
import com.foundationdb.junit.ParameterizationBuilder;
import com.foundationdb.junit.Parameterization;
import java.util.Collection;
import com.foundationdb.junit.NamedParameterizedRunner;
import com.foundationdb.server.types.ValueSource;
import java.math.BigInteger;
import com.foundationdb.server.expression.Expression;
import com.foundationdb.server.expression.ExpressionComposer;
import com.foundationdb.server.types.AkType;

import com.foundationdb.server.types.NullValueSource;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@RunWith(NamedParameterizedRunner.class)
public class UnaryBitExpressionTest extends ComposedExpressionTestBase
{
    private static boolean alreadyExc = false;
    @Override
    public boolean alreadyExc()
    {
        return alreadyExc;
    }

    static interface Functors
    {
        ValueSource calc (BigInteger n);
        ValueSource error();
    }
    
    private static final Functors functor[] = new Functors[]
    {
        new Functors() 
        {
            @Override
            public ValueSource calc(BigInteger n)
            {
                return new ValueHolder(AkType.U_BIGINT, n.not().and(n64));
            }
            
            @Override 
            public ValueSource error()
            {
                return new ValueHolder(AkType.U_BIGINT,n64);
            }

            private final BigInteger n64 = new BigInteger("FFFFFFFFFFFFFFFF", 16);
        },
        new Functors()
        {
            @Override
            public ValueSource calc(BigInteger n)
            {
                 return new ValueHolder(AkType.LONG, n.signum() >= 0 ?  n.bitCount() : 64 - n.bitCount());
            }
            
            @Override 
            public ValueSource error()
            {
                return new ValueHolder(AkType.LONG, 0L);
            }
        }
    };
    
    private ExpressionComposer composer; 
    private UnaryBitOperator op;
    
    
    public UnaryBitExpressionTest (ExpressionComposer com, UnaryBitOperator op)
    {
        composer = com;
        this.op = op;
    }
    
    @NamedParameterizedRunner.TestParameters
    public static Collection<Parameterization> params()
    {
        ParameterizationBuilder pb = new ParameterizationBuilder();
        
        param(pb, UnaryBitExpression.BIT_COUNT_COMPOSER, UnaryBitOperator.COUNT);
        param(pb, UnaryBitExpression.NOT_COMPOSER, UnaryBitOperator.NOT);
        
        return pb.asList();
    }
    
    private static void param(ParameterizationBuilder pb, ExpressionComposer c,  UnaryBitOperator op)
    {
        pb.add(op.name() , c, op);
    }

    @Test
    public void testLong ()
    {
        Expression arg = new LiteralExpression(AkType.LONG, 15L);
        
        assertEquals(functor[op.ordinal()].calc(BigInteger.valueOf(15L)), getActualSource(arg));
        alreadyExc = true;
    }
    
    @Test 
    public void testDouble() 
    {
        Expression arg = new LiteralExpression(AkType.DOUBLE, 15.5);
        
        assertEquals(functor[op.ordinal()].calc(BigInteger.valueOf(16L)), getActualSource(arg));
    }
    
    @Test
    public void testBigInteger()
    {
        Expression arg = new LiteralExpression(AkType.U_BIGINT, new BigInteger("101011", 2));
        
        assertEquals(functor[op.ordinal()].calc(new BigInteger("101011",2)), getActualSource(arg));
    }
    
    @Test
    public void testString()
    {
        Expression arg = new LiteralExpression(AkType.VARCHAR, "15");
        
        assertEquals(functor[op.ordinal()].calc(BigInteger.valueOf(15)), getActualSource(arg));
    }
    
    @Test
    public void testNull ()
    {
        Expression arg = new LiteralExpression(AkType.NULL, null);
        
        assertEquals(NullValueSource.only(), getActualSource(arg));
    }
    
    @Test
    public void testNonNumeric()
    {
        Expression arg = new LiteralExpression(AkType.VARCHAR, "a");
        
        assertEquals(functor[op.ordinal()].error(), getActualSource(arg));
    }    
  
    private ValueSource getActualSource (Expression arg)
    {
        return compose(getComposer(), Arrays.asList(arg)).evaluation().eval();
    }
    
    @Override
    protected CompositionTestInfo getTestInfo() 
    {
        return testInfo;
    }

    @Override
    protected ExpressionComposer getComposer() 
    {
        return composer;
    }    
 
    private final CompositionTestInfo testInfo = new CompositionTestInfo(1, AkType.LONG, true);
}