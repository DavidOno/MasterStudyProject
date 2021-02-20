package de.smarthome.model.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Function {

    private final String name;
    private final String ID;
    private final String channelType;
    private final String functionType;
    private final List<Datapoint> datapoints;

    public Function(@JsonProperty("displayName") String name,
                    @JsonProperty("uid") String ID,
                    @JsonProperty("channelType") String channelType,
                    @JsonProperty("functionType") String functionType,
                    @JsonProperty("dataPoints") List<Datapoint> datapoints) {
        this.name = name;
        this.ID = ID;
        this.channelType = channelType;
        this.functionType = functionType;
        this.datapoints = datapoints;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }



    public List<Datapoint> getDataPoints() {
        return datapoints;
    }

    public String getChannelType() {
        return channelType;
    }

    public String getFunctionType() {
        return functionType;
    }

    @Override
    public String toString() {
        return "Function{" +
                "\nname='" + name + '\'' +
                "\n, ID='" + ID + '\'' +
                "\n, channelType='" + channelType + '\'' +
                "\n, functionType='" + functionType + '\'' +
                "\n, datapoints=" + datapoints +
                "\n}";
    }
}