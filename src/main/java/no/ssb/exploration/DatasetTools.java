package no.ssb.exploration;

import no.ssb.dapla.dataset.api.DatasetState;

public class DatasetTools {

    public static String toTemporality(String temporalityType) {
        //  FROM schema.graphql in exploration
        //
        //  enum TemporalityType {
        //    ACCUMULATED
        //    EVENT
        //    FIXED
        //    STATUS
        //  }

        return "EVENT";
    }

    public static String toExplorationState(DatasetState state) {

        //  FROM schema.graphql in exploration
        //
        //  enum DataSetStateType {
        //    DATA_PRODUCT
        //    INPUT_DATA
        //    OTHER_DATA
        //    OUTPUT_DATA
        //    PROCESSED_DATA
        //    RAW_DATA
        //  }

        switch (state) {
            case INPUT:
                return "INPUT_DATA";
            case RAW:
                return "RAW_DATA";
            case TEMP:
                return "OTHER_DATA";
            case OTHER:
                return "OTHER_DATA";
            case OUTPUT:
                return "OUTPUT_DATA";
            case PRODUCT:
                return "DATA_PRODUCT";
            case PROCESSED:
                return "PROCESSED_DATA";
            case UNRECOGNIZED:
                return "OTHER_DATA";
            default:
                return "OTHER_DATA";
        }
    }
}
