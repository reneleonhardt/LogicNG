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

package org.logicng.io.parsers;

import org.junit.Assert;
import org.junit.Test;
import org.logicng.formulas.F;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Unit Tests for the class {@link PropositionalParser}.
 * @author Christoph Zengler
 * @version 1.0
 * @since 1.0
 */
public class PropositionalParserTest {

  @Test
  public void testParseConstants() throws ParserException {
    PropositionalParser parser = new PropositionalParser(F.f);
    Assert.assertEquals(F.f.verum(), parser.parse("$true"));
    Assert.assertEquals(F.f.falsum(), parser.parse("$false"));
  }

  @Test
  public void testParseLiterals() throws ParserException {
    PropositionalParser parser = new PropositionalParser(F.f);
    Assert.assertEquals(F.f.variable("A"), parser.parse("A"));
    Assert.assertEquals(F.f.variable("a"), parser.parse("a"));
    Assert.assertEquals(F.f.variable("a1"), parser.parse("a1"));
    Assert.assertEquals(F.f.variable("aA_Bb_Cc_12_3"), parser.parse("aA_Bb_Cc_12_3"));
    Assert.assertEquals(F.f.literal("A", false), parser.parse("~A"));
    Assert.assertEquals(F.f.literal("a", false), parser.parse("~a"));
    Assert.assertEquals(F.f.literal("a1", false), parser.parse("~a1"));
    Assert.assertEquals(F.f.literal("aA_Bb_Cc_12_3", false), parser.parse("~aA_Bb_Cc_12_3"));
    Assert.assertEquals(F.f.literal("@aA_Bb_Cc_12_3", false), parser.parse("~@aA_Bb_Cc_12_3"));
  }

  @Test
  public void testParseOperators() throws ParserException {
    PropositionalParser parser = new PropositionalParser(F.f);
    Assert.assertEquals(F.f.not(F.f.variable("a")), parser.parse("~a"));
    Assert.assertEquals(F.f.not(F.f.variable("Var")), parser.parse("~Var"));
    Assert.assertEquals(F.f.and(F.f.variable("a"), F.f.variable("b")), parser.parse("a & b"));
    Assert.assertEquals(F.f.and(F.f.literal("a", false), F.f.literal("b", false)), parser.parse("~a & ~b"));
    Assert.assertEquals(F.f.and(F.f.literal("a", false), F.f.variable("b"), F.f.literal("c", false), F.f.variable("d")), parser.parse("~a & b & ~c & d"));
    Assert.assertEquals(F.f.or(F.f.variable("a"), F.f.variable("b")), parser.parse("a | b"));
    Assert.assertEquals(F.f.or(F.f.literal("a", false), F.f.literal("b", false)), parser.parse("~a | ~b"));
    Assert.assertEquals(F.f.or(F.f.literal("a", false), F.f.variable("b"), F.f.literal("c", false), F.f.variable("d")), parser.parse("~a | b | ~c | d"));
    Assert.assertEquals(F.f.implication(F.f.variable("a"), F.f.variable("b")), parser.parse("a => b"));
    Assert.assertEquals(F.f.implication(F.f.literal("a", false), F.f.literal("b", false)), parser.parse("~a => ~b"));
    Assert.assertEquals(F.f.equivalence(F.f.variable("a"), F.f.variable("b")), parser.parse("a <=> b"));
    Assert.assertEquals(F.f.equivalence(F.f.literal("a", false), F.f.literal("b", false)), parser.parse("~a <=> ~b"));
  }

  @Test
  public void testParsePrecedences() throws ParserException {
    PropositionalParser parser = new PropositionalParser(F.f);
    Assert.assertEquals(F.f.or(F.f.variable("x"), F.f.and(F.f.variable("y"), F.f.variable("z"))), parser.parse("x | y & z"));
    Assert.assertEquals(F.f.or(F.f.and(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("x & y | z"));
    Assert.assertEquals(F.f.implication(F.f.variable("x"), F.f.and(F.f.variable("y"), F.f.variable("z"))), parser.parse("x => y & z"));
    Assert.assertEquals(F.f.implication(F.f.and(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("x & y => z"));
    Assert.assertEquals(F.f.equivalence(F.f.variable("x"), F.f.and(F.f.variable("y"), F.f.variable("z"))), parser.parse("x <=> y & z"));
    Assert.assertEquals(F.f.equivalence(F.f.and(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("x & y <=> z"));
    Assert.assertEquals(F.f.implication(F.f.variable("x"), F.f.or(F.f.variable("y"), F.f.variable("z"))), parser.parse("x => y | z"));
    Assert.assertEquals(F.f.implication(F.f.or(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("x | y => z"));
    Assert.assertEquals(F.f.equivalence(F.f.variable("x"), F.f.or(F.f.variable("y"), F.f.variable("z"))), parser.parse("x <=> y | z"));
    Assert.assertEquals(F.f.equivalence(F.f.or(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("x | y <=> z"));
    Assert.assertEquals(F.f.implication(F.f.variable("x"), F.f.implication(F.f.variable("y"), F.f.variable("z"))), parser.parse("x => y => z"));
    Assert.assertEquals(F.f.equivalence(F.f.variable("x"), F.f.equivalence(F.f.variable("y"), F.f.variable("z"))), parser.parse("x <=> y <=> z"));
    Assert.assertEquals(F.f.and(F.f.or(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("(x | y) & z"));
    Assert.assertEquals(F.f.and(F.f.variable("x"), F.f.or(F.f.variable("y"), F.f.variable("z"))), parser.parse("x & (y | z)"));
    Assert.assertEquals(F.f.and(F.f.implication(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("(x => y) & z"));
    Assert.assertEquals(F.f.and(F.f.variable("x"), F.f.implication(F.f.variable("y"), F.f.variable("z"))), parser.parse("x & (y => z)"));
    Assert.assertEquals(F.f.or(F.f.implication(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("(x => y) | z"));
    Assert.assertEquals(F.f.or(F.f.variable("x"), F.f.implication(F.f.variable("y"), F.f.variable("z"))), parser.parse("x | (y => z)"));
    Assert.assertEquals(F.f.and(F.f.equivalence(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("(x <=> y) & z"));
    Assert.assertEquals(F.f.and(F.f.variable("x"), F.f.equivalence(F.f.variable("y"), F.f.variable("z"))), parser.parse("x & (y <=> z)"));
    Assert.assertEquals(F.f.or(F.f.equivalence(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("(x <=> y) | z"));
    Assert.assertEquals(F.f.or(F.f.variable("x"), F.f.equivalence(F.f.variable("y"), F.f.variable("z"))), parser.parse("x | (y <=> z)"));
    Assert.assertEquals(F.f.equivalence(F.f.implication(F.f.variable("x"), F.f.variable("y")), F.f.variable("z")), parser.parse("x => y <=> z"));
    Assert.assertEquals(F.f.implication(F.f.variable("x"), F.f.equivalence(F.f.variable("y"), F.f.variable("z"))), parser.parse("x => (y <=> z)"));
  }

  @Test
  public void parseEmptyString() throws ParserException {
    PropositionalParser parser = new PropositionalParser(F.f);
    Assert.assertEquals(F.f.verum(), parser.parse(""));
  }

  @Test
  public void testSkipSymbols() throws ParserException {
    PropositionalParser parser = new PropositionalParser(F.f);
    Assert.assertEquals(F.f.verum(), parser.parse(" "));
    Assert.assertEquals(F.f.verum(), parser.parse("\t"));
    Assert.assertEquals(F.f.verum(), parser.parse("\n"));
    Assert.assertEquals(F.f.verum(), parser.parse("\r"));
    Assert.assertEquals(F.f.verum(), parser.parse(" \r\n\n  \t"));
    Assert.assertEquals(F.AND1, parser.parse("a\n&\tb"));
    Assert.assertEquals(F.IMP1, parser.parse(" a\r=>\t\tb"));
  }

  @Test(expected = ParserException.class)
  public void testIllegalVariable1() throws ParserException {
    new PropositionalParser(F.f).parse("$$%");
  }

  @Test(expected = ParserException.class)
  public void testIllegalVariable3() throws ParserException {
    new PropositionalParser(F.f).parse(";;23");
  }

  @Test(expected = ParserException.class)
  public void testIllegalVariable4() throws ParserException {
    new PropositionalParser(F.f).parse("{0}");
  }

  @Test(expected = ParserException.class)
  public void testIllegalOperator1() throws ParserException {
    new PropositionalParser(F.f).parse("A + B");
  }

  @Test(expected = ParserException.class)
  public void testIllegalOperator2() throws ParserException {
    new PropositionalParser(F.f).parse("A &");
  }

  @Test(expected = ParserException.class)
  public void testIllegalOperator3() throws ParserException {
    new PropositionalParser(F.f).parse("A /");
  }

  @Test(expected = ParserException.class)
  public void testIllegalOperator4() throws ParserException {
    new PropositionalParser(F.f).parse("-A");
  }

  @Test(expected = ParserException.class)
  public void testIllegalOperator5() throws ParserException {
    new PropositionalParser(F.f).parse("A * B");
  }

  @Test(expected = ParserException.class)
  public void testIllegalBrackets1() throws ParserException {
    new PropositionalParser(F.f).parse("(A & B");
  }

  @Test(expected = ParserException.class)
  public void testIllegalFormula1() throws ParserException {
    new PropositionalParser(F.f).parse("((A & B)");
  }

  @Test(expected = ParserException.class)
  public void testIllegalFormula2() throws ParserException {
    new PropositionalParser(F.f).parse("(A & (C & D) B)");
  }

  @Test(expected = ParserException.class)
  public void testIllegalFormula3() throws ParserException {
    new PropositionalParser(F.f).parse("A | A + (C | B + C)");
  }

  @Test(expected = ParserException.class)
  public void testIllegalFormula4() throws ParserException {
    new PropositionalParser(F.f).parse("A | A & (C | B & C");
  }

  @Test(expected = ParserException.class)
  public void testIllegalFormula5() throws ParserException {
    new PropositionalParser(F.f).parse("A & ~B)");
  }

  @Test(expected = ParserException.class)
  public void testIllegalFormula6() throws ParserException {
    new PropositionalParser(F.f).parse("12)");
  }

  @Test(expected = ParserException.class)
  public void testIllegalFormula7() throws ParserException {
    new PropositionalParser(F.f).parse("ab@cd)");
  }

  @Test(expected = ParserException.class)
  public void testIOException() throws ParserException, IOException {
    final InputStream invalidStream = new FileInputStream("README.md");
    invalidStream.close();
    new PropositionalParser(F.f).parse(invalidStream);
  }

  @Test
  public void testToStrings() {
    Assert.assertEquals("PropositionalLexer", new PropositionalLexer(null).toString());
    Assert.assertEquals("PropositionalParser", new PropositionalParser(F.f).toString());
  }
}
