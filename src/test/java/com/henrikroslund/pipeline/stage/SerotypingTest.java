package com.henrikroslund.pipeline.stage;

import com.henrikroslund.pcr.Serotype;
import com.henrikroslund.pipeline.Pipeline;
import org.junit.Test;

public class SerotypingTest {

    Serotype serotype = new Serotype("TAATACGACTCACTATAGG", "ACCCTGGAAGTACAGGTTTTC", "");

    @Test
    public void test() throws Exception {
        Pipeline pipeline = new Pipeline("test", "src/test/resources/serotype_stage_test", "target/tmp");
        pipeline.addStage(new Serotyping(), true);
        pipeline.run();
    }
}
