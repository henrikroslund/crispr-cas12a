package com.henrikroslund.pcr;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PcrProduct {

    private String genome;
    private Serotype serotype;
    private int distance;
}
