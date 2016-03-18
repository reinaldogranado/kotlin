/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.test.semantics;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("js/js.translator/testData/inlineSizeReduction/cases")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class InlineSizeReductionTestGenerated extends AbstractInlineSizeReductionTest {
    public void testAllFilesPresentInCases() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("js/js.translator/testData/inlineSizeReduction/cases"), Pattern.compile("^(.+)\\.kt$"), true);
    }

    @TestMetadata("inlineOrder.kt")
    public void testInlineOrder() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/inlineOrder.kt");
        doTest(fileName);
    }

    @TestMetadata("lastBreak.kt")
    public void testLastBreak() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/lastBreak.kt");
        doTest(fileName);
    }

    @TestMetadata("oneTopLevelReturn.kt")
    public void testOneTopLevelReturn() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/oneTopLevelReturn.kt");
        doTest(fileName);
    }

    @TestMetadata("propertyAssignment.kt")
    public void testPropertyAssignment() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/propertyAssignment.kt");
        doTest(fileName);
    }

    @TestMetadata("returnInlineCall.kt")
    public void testReturnInlineCall() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/returnInlineCall.kt");
        doTest(fileName);
    }

    @TestMetadata("simpleReturnFunction.kt")
    public void testSimpleReturnFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/simpleReturnFunction.kt");
        doTest(fileName);
    }

    @TestMetadata("ternaryConditional.kt")
    public void testTernaryConditional() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/ternaryConditional.kt");
        doTest(fileName);
    }

    @TestMetadata("this.kt")
    public void testThis() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/this.kt");
        doTest(fileName);
    }

    @TestMetadata("valAssignment.kt")
    public void testValAssignment() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/valAssignment.kt");
        doTest(fileName);
    }

    @TestMetadata("valDeclaration.kt")
    public void testValDeclaration() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("js/js.translator/testData/inlineSizeReduction/cases/valDeclaration.kt");
        doTest(fileName);
    }
}
