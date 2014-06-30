package com.samsoft.treaviso.background.data;

public interface LocationDAO {
	public Alarm[] getActiveAlarm();
    public Alarm[] getAllAlarm();
    public boolean persistAlarm(Alarm a);
    public void updateAlarm(Alarm alarm);
    public void deleteAlarm(Alarm alarm);
    public Alarm getAlarm(long id);
	public Location[] getAllLocations();
	public boolean persistLocation(Location l);
	public void deleteLocation(Location l);
}
