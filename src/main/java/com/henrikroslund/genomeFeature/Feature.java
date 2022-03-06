package com.henrikroslund.genomeFeature;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class Feature {
    private final int startIndex;
    private final int endIndex;
    private final boolean isComplement;
    private final String type;
    private final List<String> features;

    public Feature(int startIndex, int endIndex, String type, List<String> features) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.isComplement = startIndex > endIndex;
        this.type = type;
        this.features = features;
    }

    public boolean isMatch(Sequence sequence, boolean mustMatchStrand) {
        if(mustMatchStrand && sequence.getIsComplement() != isComplement) {
            return false;
        }
        return sequence.getStartIndex() <= endIndex && sequence.getEndIndex() >= startIndex;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(
                startIndex + " " + endIndex + " " + (isComplement ? "-" : "+") + " " + type + " \n");
        for(String feature : features) {
            result.append(feature).append("\n");
        }
        return result.toString();
    }
}
