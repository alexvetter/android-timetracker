package alexvetter.timetrackr.domain;

import java.util.UUID;

/**
 *
 */
public class BeaconModel implements DomainModel<UUID> {
    private UUID uuid;

    private String name;

    private Boolean enabled;

    public UUID getId() {
        return uuid;
    }

    public void setId(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean toggleEnabled() {
        return (enabled = !enabled);
    }

    @Override
    public String toString() {
        return "Beacon{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
