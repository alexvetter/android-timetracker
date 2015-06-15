package alexvetter.timetrackr.controller;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import alexvetter.timetrackr.database.BeaconDatabaseHandler;
import alexvetter.timetrackr.domain.BeaconModel;

public class BeaconController implements Controller<BeaconDatabaseHandler> {

    private final BeaconDatabaseHandler handler;

    public BeaconController() {
        handler = new BeaconDatabaseHandler();
    }

    @Override
    public BeaconDatabaseHandler getDatabaseHandler() {
        return handler;
    }

    public void onDelete(String uuid) {
        System.out.println("Delete beacon with UUID " + uuid);

        handler.deleteById(uuid);
    }

    public List<Region> getBeaconsAsRegions() {
        List<Region> regions = new ArrayList<>();
        for (BeaconModel model : handler.getAll()) {
            regions.add(new Region(model.getName() + "-" + model.getId(), Identifier.parse(model.getId().toString()), null, null));
        }
        return regions;
    }

    public void onToggleEnabled(String uuid) {
        System.out.println("Toggle enabled of beacon with UUID " + uuid);

        BeaconModel beaconModel = handler.get(uuid);
        beaconModel.toggleEnabled();

        handler.update(beaconModel);
    }

    public boolean isEnabled(Region region) {
        BeaconModel beacon = getBeacon(region);
        return (beacon != null) && beacon.getEnabled();
    }

    public BeaconModel getBeacon(Region region) {
        return handler.get(region.getId1().toUuidString());
    }

    public boolean isDeviceRegistered(org.altbeacon.beacon.Beacon device) {
        String uuid = device.getId1().toUuidString();

        return (handler.get(uuid) != null);
    }

    public void onDeviceAdd(org.altbeacon.beacon.Beacon device, String name) {
        String uuid = device.getId1().toUuidString();

        System.out.println("Add beacon with UUID: " + uuid);

        BeaconDatabaseHandler handler = new BeaconDatabaseHandler();

        BeaconModel model = handler.get(uuid);

        if (model == null) {
            model = new BeaconModel();

            model.setId(UUID.fromString(uuid));
            model.setName(name);
            model.setEnabled(true);

            handler.add(model);
        }
    }
}
