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
public class CrisprPamEvaluator implements SequenceEvaluator {

    // First three must be Ts and the forth must Not be T for crispr

    // Strict mapping require TTT^T while NOT strict will only check TTTN
    private final boolean strictMatching;

    public CrisprPamEvaluator(boolean strictMatching) {
        this.strictMatching = strictMatching;
    }

    @Getter
    private Sequence match = null;

    @Override
    public boolean evaluate(Sequence sequence) {
        match = null;
        boolean result =
                sequence.getRaw().charAt(0) == 'T' &&
                sequence.getRaw().charAt(1) == 'T' &&
                sequence.getRaw().charAt(2) == 'T' &&
                ( !strictMatching || sequence.getRaw().charAt(3) != 'T');
        if(result) {
            match = sequence;
            handleEvaluationMatch(log);
        }
        return result;
    }

    @Override
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new CrisprPamEvaluator(strictMatching);
    }

    @Override
    public String describe() {
        return "CrisprPamEvaluator( " + (strictMatching ? "TTT^T" : "TTTN") + " )";
    }

    @Override
    public String toString() {
        return describe() + " " + match;
    }
}
