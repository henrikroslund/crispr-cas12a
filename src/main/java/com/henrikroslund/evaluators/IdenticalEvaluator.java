package com.henrikroslund.evaluators;

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
import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class IdenticalEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    @Getter
    private Sequence match = null;

    private boolean checkPam = true;
    private boolean checkSeed = true;
    private boolean checkAll = true;

    public IdenticalEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    public IdenticalEvaluator(Sequence sequence, boolean checkPam, boolean checkSeed) {
        this.sequence = sequence;
        this.checkPam = checkPam;
        this.checkSeed = checkSeed;
        this.checkAll = checkPam && checkSeed;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        boolean result = true;
        if(checkAll) {
            result = this.sequence.equals(sequence);
        } else {
            if(checkPam) {
                result = this.sequence.equalsPam(sequence);
            }
            if(checkSeed) {
                result = result && this.sequence.equalsSeed(sequence);
            }
        }
        if(result) {
            match = sequence;
            handleEvaluationMatch(log);
        } else {
            match = null;
        }
        return result;
    }

    @Override
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new IdenticalEvaluator(sequence, checkPam, checkSeed);
    }

    @Override
    public String describe() {
        return "IdenticalEvaluator( checkPam="+checkPam + " checkSeed="+checkSeed + " )";
    }

    @Override
    public String toString() {
        return describe() + (match != null ?  " " + match : "");
    }
}
