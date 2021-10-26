package com.henrikroslund.pcr;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PcrProduct {

    private String genome;
    private Serotype serotype;
    private int distance;
    private List<Integer> primerAPositions;
    private List<Integer> primerBPositions;

}
