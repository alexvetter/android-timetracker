package timetrackr.model;

/**
 *
 */
public class BeaconModel {
    private String uuid;

    private String name;

    private Boolean enabled;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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
        return (enabled = enabled ? false : true);
    }

    @Override
    public String toString() {
        return "BeaconModel{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
