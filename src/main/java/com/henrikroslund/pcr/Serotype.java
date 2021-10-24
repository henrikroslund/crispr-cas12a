package com.henrikroslund.pcr;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class Serotype {

    @Getter
    private final String primerA;
    @Getter
    private final String primerB;
    @Getter
    private final String name;

    public Serotype(String primerA, String primerB, String name) {
        this.name = name;
        this.primerA = primerA;
        this.primerB = primerB;
    }

    @Override
    public String toString() {
        return name + " [ " + primerA + "] [ " + primerB +"]";
    }
}
