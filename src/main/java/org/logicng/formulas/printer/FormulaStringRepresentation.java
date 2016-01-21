///////////////////////////////////////////////////////////////////////////
//                   __                _      _   ________               //
//                  / /   ____  ____ _(_)____/ | / / ____/               //
//                 / /   / __ \/ __ `/ / ___/  |/ / / __                 //
//                / /___/ /_/ / /_/ / / /__/ /|  / /_/ /                 //
//               /_____/\____/\__, /_/\___/_/ |_/\____/                  //
//                           /____/                                      //
//                                                                       //
//               The Next Generation Logic Library                       //
//                                                                       //
///////////////////////////////////////////////////////////////////////////
//                                                                       //
//  Copyright 2015-2016 Christoph Zengler                                //
//                                                                       //
//  Licensed under the Apache License, Version 2.0 (the "License");      //
//  you may not use this file except in compliance with the License.     //
//  You may obtain a copy of the License at                              //
//                                                                       //
//  http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                       //
//  Unless required by applicable law or agreed to in writing, software  //
//  distributed under the License is distributed on an "AS IS" BASIS,    //
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or      //
//  implied.  See the License for the specific language governing        //
//  permissions and limitations under the License.                       //
//                                                                       //
///////////////////////////////////////////////////////////////////////////

package org.logicng.formulas.printer;

import org.logicng.formulas.CType;
import org.logicng.formulas.Equivalence;
import org.logicng.formulas.FType;
import org.logicng.formulas.Formula;
import org.logicng.formulas.Implication;
import org.logicng.formulas.Literal;
import org.logicng.formulas.NAryOperator;
import org.logicng.formulas.Not;
import org.logicng.formulas.PBConstraint;

/**
 * Super class for a formula string representation.
 * @author Christoph Zengler
 * @version 1.0
 * @since 1.0
 */
public abstract class FormulaStringRepresentation {

  /**
   * Returns the string representation of the given formula.
   * @param formula the formula
   * @return the string representation of the formula
   */
  public String toString(final Formula formula) {
    switch (formula.type()) {
      case FALSE:
        return falsum();
      case TRUE:
        return verum();
      case LITERAL:
        final Literal lit = (Literal) formula;
        return lit.phase() ? lit.name() : negation() + lit.name();
      case NOT:
        final Not not = (Not) formula;
        return negation() + bracket(not.operand());
      case IMPL:
        final Implication impl = (Implication) formula;
        return binaryOperator(FType.IMPL, impl.left(), impl.right(), implication());
      case EQUIV:
        final Equivalence equiv = (Equivalence) formula;
        return binaryOperator(FType.EQUIV, equiv.left(), equiv.right(), equivalence());
      case AND:
      case OR:
        final NAryOperator nary = (NAryOperator) formula;
        final String op = formula.type() == FType.AND ? and() : or();
        return concatOperands(nary, String.format(" %s ", op));
      case PBC:
        final PBConstraint pbc = (PBConstraint) formula;
        return String.format("%s %s %d", pbLhs(pbc.operands(), pbc.coefficients()), pbComparator(pbc.comparator()), pbc.rhs());
      default:
        throw new IllegalArgumentException("Cannot print the unknown formula type " + formula.type());
    }
  }

  /**
   * Returns a bracketed string version of a given formula.
   * @param formula the formula
   * @return {@code "(" + formula.toString() + ")"}
   */
  private String bracket(final Formula formula) {
    return String.format("%s%s%s", lbr(), toString(formula), rbr());
  }

  /**
   * Returns the string representation of a binary operator.
   * @param type     the type of the binary operator
   * @param left     the left-hand formula
   * @param right    the right-hand formula
   * @param operator the operator string
   * @return the string representation
   */
  private String binaryOperator(final FType type, final Formula left, final Formula right, final String operator) {
    String leftString = type.precedence() < left.type().precedence() ? toString(left) : bracket(left);
    String rightString = type.precedence() < right.type().precedence() ? toString(right) : bracket(right);
    return String.format("%s %s %s", leftString, operator, rightString);
  }

  /**
   * Concatenates the operands of this n-ary operator with a given separator to a string.
   * @param operator the n-ary operator
   * @param sep      the separator
   * @return the concatenated operands
   */
  private String concatOperands(final NAryOperator operator, final String sep) {
    StringBuilder sb = new StringBuilder();
    int count = 0;
    int size = operator.numberOfOperands();
    Formula last = null;
    for (final Formula op : operator) {
      if (++count == size)
        last = op;
      else {
        sb.append(operator.type().precedence() < op.type().precedence() ? toString(op) : bracket(op));
        sb.append(sep);
      }
    }
    if (last != null)
      sb.append(operator.type().precedence() < last.type().precedence() ? toString(last) : bracket(last));
    return sb.toString();
  }

  /**
   * Returns the string representation of the left-hand side of a pseudo-Boolean constraint.
   * @param operands     the literals of the constraint
   * @param coefficients the coefficients of the constraint
   * @return the string representation
   */
  private String pbLhs(final Literal[] operands, final int[] coefficients) {
    assert operands.length == coefficients.length;
    StringBuilder sb = new StringBuilder();
    final String mul = pbMul();
    final String add = pbAdd();
    for (int i = 0; i < operands.length - 1; i++)
      if (coefficients[i] != 1)
        sb.append(coefficients[i]).append(mul).append(operands[i]).append(" ").append(add).append(" ");
      else
        sb.append(operands[i]).append(" ").append(add).append(" ");
    if (operands.length > 0)
      if (coefficients[operands.length - 1] != 1)
        sb.append(coefficients[operands.length - 1]).append(mul).append(operands[operands.length - 1]);
      else
        sb.append(operands[operands.length - 1]);
    return sb.toString();
  }

  /**
   * Returns the string representation of false.
   * @return the string representation of false
   */
  protected abstract String falsum();

  /**
   * Returns the string representation of true.
   * @return the string representation of true
   */
  protected abstract String verum();

  /**
   * Returns the string representation of not.
   * @return the string representation of not
   */
  protected abstract String negation();

  /**
   * Returns the string representation of an implication.
   * @return the string representation of an implication
   */
  protected abstract String implication();

  /**
   * Returns the string representation of an equivalence.
   * @return the string representation of an equivalence
   */
  protected abstract String equivalence();

  /**
   * Returns the string representation of and.
   * @return the string representation of and
   */
  protected abstract String and();

  /**
   * Returns the string representation of or.
   * @return the string representation of or
   */
  protected abstract String or();

  /**
   * Returns the string representation of a pseudo-Boolean comparator.
   * @param comparator the pseudo-Boolean comparator
   * @return the string representation of a pseudo-Boolean comparator
   */
  protected abstract String pbComparator(final CType comparator);

  /**
   * Returns the string representation of a pseudo-Boolean multiplication.
   * @return the string representation of a pseudo-Boolean multiplication
   */
  protected abstract String pbMul();

  /**
   * Returns the string representation of a pseudo-Boolean addition.
   * @return the string representation of a pseudo-Boolean addition
   */
  protected abstract String pbAdd();

  /**
   * Returns the string representation of a left bracket.
   * @return the string representation of a left bracket
   */
  protected abstract String lbr();

  /**
   * Returns the string representation of right bracket.
   * @return the string representation of right bracket
   */
  protected abstract String rbr();
}
