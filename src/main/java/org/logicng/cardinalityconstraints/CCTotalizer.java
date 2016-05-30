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

/*****************************************************************************************
 * Open-WBO -- Copyright (c) 2013-2015, Ruben Martins, Vasco Manquinho, Ines Lynce
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *****************************************************************************************/

package org.logicng.cardinalityconstraints;

import org.logicng.collections.LNGVector;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Variable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Super class for totalizers due to Bailleux and Boufkhad.
 * @version 1.1
 * @since 1.0
 */
final class CCTotalizer {

  private enum Bound {LOWER, UPPER, BOTH}

  private final FormulaFactory f;
  private LNGVector<Variable> cardinalityInvars;
  private List<Formula> result;
  private CCIncrementalData incData;

  /**
   * Constructs a new totalizer.
   * @param f the formula factory
   */
  CCTotalizer(final FormulaFactory f) {
    this.f = f;
    this.result = new LinkedList<>();
  }

  /**
   * Builds an at-most-k constraint.
   * @param vars the variables
   * @param rhs  the right-hand side
   * @return the constraint
   * @throws IllegalArgumentException if the right hand side of the constraint was negative
   */
  List<Formula> buildAMK(final Variable[] vars, int rhs) {
    final LNGVector<Variable> cardinalityOutvars = this.initializeConstraint(vars);
    this.incData = new CCIncrementalData(this.f, CCConfig.AMK_ENCODER.TOTALIZER, rhs, cardinalityOutvars);
    this.toCNF(cardinalityOutvars, rhs, Bound.UPPER);
    assert this.cardinalityInvars.size() == 0;
    for (int i = rhs; i < cardinalityOutvars.size(); i++)
      this.result.add(cardinalityOutvars.get(i).negate());
    return this.result;
  }

  /**
   * Builds an at-least-k constraint.
   * @param vars the variables
   * @param rhs  the right-hand side
   * @return the constraint
   * @throws IllegalArgumentException if the right hand side of the constraint was negative
   */
  List<Formula> buildALK(final Variable[] vars, int rhs) {
    final LNGVector<Variable> cardinalityOutvars = this.initializeConstraint(vars);
    this.incData = new CCIncrementalData(this.f, CCConfig.ALK_ENCODER.TOTALIZER, rhs, cardinalityOutvars);
    this.toCNF(cardinalityOutvars, rhs, Bound.LOWER);
    assert this.cardinalityInvars.size() == 0;
    for (int i = 0; i < rhs; i++)
      this.result.add(cardinalityOutvars.get(i));
    return this.result;
  }

  /**
   * Builds an exactly-k constraint.
   * @param vars the variables
   * @param rhs  the right-hand side
   * @return the constraint
   * @throws IllegalArgumentException if the right hand side of the constraint was negative
   */
  List<Formula> buildEXK(final Variable[] vars, int rhs) {
    final LNGVector<Variable> cardinalityOutvars = this.initializeConstraint(vars);
    this.toCNF(cardinalityOutvars, rhs, Bound.BOTH);
    assert this.cardinalityInvars.size() == 0;
    for (int i = 0; i < rhs; i++)
      this.result.add(cardinalityOutvars.get(i));
    for (int i = rhs; i < cardinalityOutvars.size(); i++)
      this.result.add(cardinalityOutvars.get(i).negate());
    return this.result;
  }

  /**
   * Initializes the constraint.
   * @param vars the variables
   * @return the auxiliary variables
   */
  private LNGVector<Variable> initializeConstraint(final Variable[] vars) {
    this.result = new ArrayList<>();
    this.cardinalityInvars = new LNGVector<>(vars.length);
    final LNGVector<Variable> cardinalityOutvars = new LNGVector<>(vars.length);
    for (final Variable var : vars) {
      this.cardinalityInvars.push(var);
      cardinalityOutvars.push(this.f.newCCVariable());
    }
    return cardinalityOutvars;
  }

  /**
   * Returns the incremental data for the current encoded constraint.
   * @return the incremental data for the current encoded constraint
   */
  CCIncrementalData incrementalData() {
    return this.incData;
  }

  private void toCNF(final LNGVector<Variable> vars, int rhs, final Bound bound) {
    final LNGVector<Variable> left = new LNGVector<>();
    final LNGVector<Variable> right = new LNGVector<>();
    assert vars.size() > 1;
    int split = vars.size() / 2;
    for (int i = 0; i < vars.size(); i++) {
      if (i < split) {
        if (split == 1) {
          assert this.cardinalityInvars.size() > 0;
          left.push(this.cardinalityInvars.back());
          this.cardinalityInvars.pop();
        } else
          left.push(this.f.newCCVariable());
      } else {
        if (vars.size() - split == 1) {
          assert this.cardinalityInvars.size() > 0;
          right.push(this.cardinalityInvars.back());
          this.cardinalityInvars.pop();
        } else
          right.push(this.f.newCCVariable());
      }
    }
    if (bound == Bound.UPPER || bound == Bound.BOTH)
      this.adderAMK(left, right, vars, rhs);
    if (bound == Bound.LOWER || bound == Bound.BOTH)
      this.adderALK(left, right, vars);
    if (left.size() > 1)
      this.toCNF(left, rhs, bound);
    if (right.size() > 1)
      this.toCNF(right, rhs, bound);
  }

  private void adderAMK(final LNGVector<Variable> left, final LNGVector<Variable> right,
                        final LNGVector<Variable> output, int rhs) {
    assert output.size() == left.size() + right.size();
    for (int i = 0; i <= left.size(); i++) {
      for (int j = 0; j <= right.size(); j++) {
        if (i == 0 && j == 0)
          continue;
        if (i + j > rhs + 1)
          continue;
        if (i == 0)
          this.result.add(this.f.clause(right.get(j - 1).negate(), output.get(j - 1)));
        else if (j == 0)
          this.result.add(this.f.clause(left.get(i - 1).negate(), output.get(i - 1)));
        else
          this.result.add(this.f.clause(left.get(i - 1).negate(), right.get(j - 1).negate(), output.get(i + j - 1)));
      }
    }
  }

  private void adderALK(final LNGVector<Variable> left, final LNGVector<Variable> right,
                        final LNGVector<Variable> output) {
    assert output.size() == left.size() + right.size();
    for (int i = 0; i <= left.size(); i++) {
      for (int j = 0; j <= right.size(); j++) {
        if (i == 0 && j == 0)
          continue;
        if (i == 0)
          this.result.add(this.f.clause(right.get(j - 1), output.get(left.size() + j - 1).negate()));
        else if (j == 0)
          this.result.add(this.f.clause(left.get(i - 1), output.get(right.size() + i - 1).negate()));
        else
          this.result.add(this.f.clause(left.get(i - 1), right.get(j - 1), output.get(i + j - 2).negate()));
      }
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
