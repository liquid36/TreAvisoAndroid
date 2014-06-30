package com.samsoft.treaviso.background.data;

import android.content.Context;

import com.samsoft.treaviso.background.data.sqlite.SQLiteLocationDAO;

public abstract class DAOFactory {
	public static LocationDAO createLocationDAO(Context context) {
		//Very basic for now
		return new SQLiteLocationDAO(context);
	}
}
