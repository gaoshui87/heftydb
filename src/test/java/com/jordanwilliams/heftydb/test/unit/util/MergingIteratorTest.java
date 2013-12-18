/*
 * Copyright (c) 2013. Jordan Williams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jordanwilliams.heftydb.test.unit.util;

import com.google.common.primitives.Ints;
import com.jordanwilliams.heftydb.util.MergingIterator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class MergingIteratorTest {

    private static final int[] array1 = {1, 2, 3, 4, 5, 6};
    private static final int[] array2 = {1, 4, 5, 7, 9, 10};
    private static final int[] merged = {1, 1, 2, 3, 4, 4, 5, 5, 6, 7, 9, 10};

    @Test
    public void mergeTest() {
        MergingIterator<Integer> mergingIterator = new MergingIterator<Integer>(Ints.asList(array1).iterator(), Ints.asList(array2).iterator());

        Iterator<Integer> mergedIterator = Ints.asList(merged).iterator();

        while (mergedIterator.hasNext()) {
            Assert.assertEquals("Merged values match", mergedIterator.next(), mergingIterator.next());
        }
    }
}