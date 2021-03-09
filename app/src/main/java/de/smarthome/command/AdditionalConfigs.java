package de.smarthome.command;

import de.smarthome.beacons.BeaconLocation;
import de.smarthome.model.configs.BoundariesConfig;
import de.smarthome.model.configs.ChannelConfig;

public enum AdditionalConfigs {

    LOCATION("location_config", BeaconLocation.class),
    CHANNEL("channel_config", ChannelConfig.class),
    BOUNDARIES("boundaries_config", BoundariesConfig.class);

    private final String resource;
    private final Class correspondingPOJO;

    AdditionalConfigs(String resource, Class correspondingPOJO){
        this.resource = resource;
        this.correspondingPOJO = correspondingPOJO;
    }

    public String getResource() {
        return resource;
    }

    public Class getCorrespondingPOJO() {
        return correspondingPOJO;
    }
}
